package com.tsgcompany.reviewboard.servcies


import zio.*
import com.auth0.jwt.*
import com.auth0.jwt.JWTVerifier.BaseVerification
import com.auth0.jwt.algorithms.Algorithm
import com.tsgcompany.reviewboard.domain.data.*

import java.time.Instant

trait JWTService {
  def createToken(user: User): Task[UserToken]
  def verifyToken(token: String): Task[UserId]

}

class JWTServiceLive (clock: java.time.Clock) extends JWTService {
  private val SECRET = "secret" // TODO pass this from config
  private val ISSUER = "tsgcompany.com"
  private val TTL = 30 * 24 * 3600 // TODO pass this from config
  private val algorithm = Algorithm.HMAC512(SECRET)
  private val CLAIM_USERNAME = "username"
  private val verifier: JWTVerifier =
    JWT
      .require(algorithm)
      .withIssuer(ISSUER)
      .asInstanceOf[BaseVerification]
      .build(clock)

  override def createToken(user: User): Task[UserToken] =
    for {
      now <- ZIO.attempt(clock.instant())
      expiration <- ZIO.succeed(now.plusSeconds(TTL))
      token <- ZIO.attempt(
        JWT
          .create()
          .withIssuer(ISSUER)
          .withIssuedAt(now)
          .withExpiresAt(expiration)
          .withSubject(user.id.toString) //user identifier
          .withClaim(CLAIM_USERNAME, user.email)
          .sign(algorithm)
      )
    } yield UserToken(user.email, token, expiration.getEpochSecond)

  override def verifyToken(token: String): Task[UserId] =
    for {
      decoded <- ZIO.attempt(verifier.verify(token))
      userId <- ZIO.attempt(
        UserId(
          decoded.getSubject().toLong,
          decoded.getClaim(CLAIM_USERNAME).asString()
        )
      )
    } yield userId

}

//object JWTServiceDemo {
//  def main(args: Array[String]) = {
//    val algorithm = Algorithm.HMAC512("secret")
//    val jwt = JWT
//      .create()
//      .withIssuer("tsgcompany.com")
//      .withIssuedAt(Instant.now())
//      .withExpiresAt(Instant.now().plusSeconds(30*24*3600))
//      .withSubject("1") //user identifier
//      .withClaim("username", "tsg@tsgcompany.com")
//      .sign(algorithm)
//    println(jwt)
//    //eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.   // header
//    // eyJzdWIiOiIxIiwiaXNzIjoidHNnY29tcGFueS5jb20iLCJleHAiOjE3MDc1OTEyNjEsImlhdCI6MTcwNDk5OTI2MSwidXNlcm5hbWUiOiJ0c2dAdHNnY29tcGFueS5jb20ifQ.  // claims
//    // LnxXUsAtsBLUUdhVjz7PcxtSVa2F2rMKSrotlQSD83ypHASKC9Xyn7ipnQIjjLVxDuWil-vexyargQekmwJ-MA // signature
//
//    val verified: JWTVerifier =
//      JWT
//        .require(algorithm)
//        .withIssuer("tsgcompany.com")
//        .asInstanceOf[BaseVerification]
//        .build(java.time.Clock.systemDefaultZone())
//
//    val decoded = verified.verify(jwt)
//    val userId = decoded.getSubject
//    val userEmail = decoded.getClaim("username").asString()
//    println("userId: " + userId)
//    println("userEmail: " + userEmail)
//  }
//}

object JWTServiceLive {
  val layer = ZLayer {
    Clock.javaClock.map(clock =>  new JWTServiceLive(clock))
  }
}
object JWTServiceDemo extends ZIOAppDefault {
  val program = for {
    service <- ZIO.service[JWTService]
    token <- service.createToken(User(1L, "tsg@tsgcompany.com", "unimportant"))
    _ <- Console.printLine(token)
    userId <- service.verifyToken(token.token)
    _ <- Console.printLine(userId.toString)
  } yield ()
  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = program
    .provide(
      JWTServiceLive.layer
    )
}
