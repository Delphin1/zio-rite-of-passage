package com.tsgcompany.reviewboard.services

import com.tsgcompany.reviewboard.domain.data.{User, UserId, UserToken}
import com.tsgcompany.reviewboard.repositories.{RecoveryTokenRepository, UserRepository}
import com.tsgcompany.reviewboard.servcies.{EmailService, JWTService, UserService, UserServiceLive}
import zio.*
import zio.test.*

object UserServiceSpec extends ZIOSpecDefault{
  val tsgUser = User(
    1L,
    "tsg@tsgcompany.com",
    "1000:62BD15BA8ADA14DE2B67795CFF3BCACD33DD54CC11527319:BA8FDE09E4C836A854AFC890A25486A0F45241F741D1E4F9"
  )

  val stubRepoLayer = ZLayer.succeed{
    new UserRepository {
      val db = collection.mutable.Map[Long, User](1L -> tsgUser)
      override def create(user: User): Task[User] = ZIO.succeed {
        db += (user.id -> user)
        user
      }
      override def update(id: Long, op: User => User): Task[User] =
        ZIO.attempt{
          val newUser = op(db(id))
          db += (newUser.id -> newUser)
          newUser
        }

      override def getById(id: Long): Task[Option[User]] =
        ZIO.succeed(db.get(id))

      override def getByEmail(email: String): Task[Option[User]] =
        ZIO.succeed(db.values.find(_.email == email))

      override def delete(id: Long): Task[User] =
        ZIO.attempt {
          val user = db(id)
          db -= id
          user
        }

    }
  }

  val stubTokenRepoLayer = ZLayer.succeed {
    new RecoveryTokenRepository {
      val db = collection.mutable.Map[String, String]()

      override def getToken(email: String): Task[Option[String]] =
        ZIO.attempt{
          val token = util.Random.alphanumeric.take(8).mkString.toUpperCase()
          db += (email -> token)
          Some(token)
        }
        
      override def checkToken(email: String, token: String): Task[Boolean] =
        ZIO.succeed(db.get(email).filter(_ == token).nonEmpty)
    }
  }

  val stubEmailLayer = ZLayer.succeed {
    new EmailService {
      override def sendEmail(to: String, subject: String, content: String): Task[Unit] = ZIO.unit

      override def sendPasswordRecovery(to: String, token: String): Task[Unit] = ZIO.unit
    }
  }

  val stubJwtLayer = ZLayer.succeed{
    new JWTService {
      override def createToken(user: User): Task[UserToken] =
        ZIO.succeed(UserToken(user.id, user.email,"BIG ACCESS", Long.MaxValue))
      override def verifyToken(token: String): Task[UserId] =
        ZIO.succeed(UserId(tsgUser.id, tsgUser.email))
    }
  }
  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("UserServiceSpec")(
      test("create and validata a user") {
        for {
          service <- ZIO.service[UserService]
          user <- service.registerUser(tsgUser.email, "test")
          valid <- service.verifyPassword(tsgUser.email, "test")
        } yield assertTrue(valid && user.email == tsgUser.email)

      },
      test("validate correct credentials") {
        for {
          service <- ZIO.service[UserService]
          valid <- service.verifyPassword(tsgUser.email, "test")
        } yield assertTrue(valid)
      },
      test("invalidate incorrect credentials") {
        for {
          service <- ZIO.service[UserService]
          valid <- service.verifyPassword(tsgUser.email, "somethingelse")
        } yield assertTrue(!valid)
      },
      test("invalidate non existing user") {
        for {
          service <- ZIO.service[UserService]
          valid <- service.verifyPassword("someone@mail.ru", "test")
        } yield assertTrue(!valid)
      },
      test("update password") {
        for {
          service <- ZIO.service[UserService]
          newUser <- service.updatePassword(tsgUser.email, "test", "newpass" )
          oldValid <- service.verifyPassword(tsgUser.email, "test")
          newValid <- service.verifyPassword(tsgUser.email, "newpass")
        } yield assertTrue(newValid && !oldValid)
      },
      test("delete not-existent user should fail") {
        for {
          service <- ZIO.service[UserService]
          err <- service.deleteUser("someone@mail.ru", "somepassword").flip
        } yield assertTrue(err.isInstanceOf[RuntimeException])
      },
      test("delete with incorrect credentials should fail") {
        for {
          service <- ZIO.service[UserService]
          err <- service.deleteUser(tsgUser.email, "somepassword").flip
        } yield assertTrue(err.isInstanceOf[RuntimeException])
      },
      test("delete user") {
        for {
          service <- ZIO.service[UserService]
          user <- service.deleteUser(tsgUser.email, "test")
        } yield assertTrue(user.email == tsgUser.email)
      }
    ).provide(
      stubRepoLayer,
      stubJwtLayer,
      stubEmailLayer,
      stubTokenRepoLayer,
      UserServiceLive.layer
    )
}
