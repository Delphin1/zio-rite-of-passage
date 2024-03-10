package com.tsgcompany.reviewboard.repositories


import com.tsgcompany.reviewboard.config.{Configs, RecoveryTokensConfig}
import io.getquill.*
import io.getquill.jdbczio.Quill
import zio.*
import com.tsgcompany.reviewboard.domain.data.*


trait RecoveryTokenRepository {
  def getToken(email: String): Task[Option[String]]
  def checkToken(email: String, token: String): Task[Boolean]
}

class RecoveryTokenRepositoryLive private (tokenConfig: RecoveryTokensConfig, quill: Quill.Postgres[SnakeCase], userRepo: UserRepository) extends RecoveryTokenRepository {

  import quill.*


  inline given schema: SchemaMeta[PasswordRecoveryToken] = schemaMeta[PasswordRecoveryToken]("recovery_tokens")

  inline given insMeta: InsertMeta[PasswordRecoveryToken] = insertMeta[PasswordRecoveryToken]()

  inline given upMeta: UpdateMeta[PasswordRecoveryToken] = updateMeta[PasswordRecoveryToken](_.email)

  private val tokenDuration = 600 // TODO: pass this from config

  private def randomUppsercaseString(len: Int): Task[String] =
    ZIO.succeed(scala.util.Random.alphanumeric.take(len).mkString.toUpperCase)

  // AB12CD
  private def findToken(email: String): Task[Option[String]] =
    run(query[PasswordRecoveryToken].filter(_.email == lift(email))).map(_.headOption.map(_.token))

  // select token from recovery_tokens where email = ?
  private def replaceToken(email: String): Task[String] =
    for {
      token <- randomUppsercaseString(8)
      _ <- run(
        query[PasswordRecoveryToken].updateValue(
          lift(PasswordRecoveryToken(email, token, java.lang.System.currentTimeMillis() + tokenDuration))
        ).returning(r => r)
      )
    } yield token

  private def generateToken(email: String): Task[String] =
    for {
      token <- randomUppsercaseString(8)
      _ <- run(
        query[PasswordRecoveryToken].insertValue(
          lift(PasswordRecoveryToken(email, token, java.lang.System.currentTimeMillis() + tokenDuration))
        ).returning(r => r)
      )
    } yield token

  private def makeFreshToken(email: String): Task[String] =
    findToken(email).flatMap {
      case Some(_) => replaceToken(email)
      case None => generateToken(email)
    }
  // find token in the tabl
  // if so, replace
  // if not, create

  def getToken(email: String): Task[Option[String]] =
    userRepo.getByEmail(email).flatMap {
      case None => ZIO.none
      case Some(_) => makeFreshToken(email).map(Some(_))

    }
  // check user in the db
  // if the user exists , make a fresh token

  def checkToken(email: String, token: String): Task[Boolean] =
    for {
      now <- Clock.instant
      checkValid <- run(
        query[PasswordRecoveryToken]
          .filter(r => 
            r.email == lift(email) && r.token == lift(token) && r.expiration > lift(now.toEpochMilli))
        
      ).map(_.nonEmpty)
    } yield checkValid
}

object RecoveryTokenRepositoryLive {
  val layer = ZLayer {
    for {
      config <- ZIO.service[RecoveryTokensConfig]
      quill <- ZIO.service[Quill.Postgres[SnakeCase.type]]
      userRepo <- ZIO.service[UserRepository]
    } yield new RecoveryTokenRepositoryLive(config, quill, userRepo)
  }

  val configuredLayer =
    Configs.makeConfigLayer[RecoveryTokensConfig]("tsgcompany.recoverytokens") >>> layer

}