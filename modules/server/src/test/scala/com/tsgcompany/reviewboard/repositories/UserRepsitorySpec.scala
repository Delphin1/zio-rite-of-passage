package com.tsgcompany.reviewboard.repositories


import zio.*
import zio.test.*
import java.security.MessageDigest

import com.tsgcompany.reviewboard.domain.data.User
import com.tsgcompany.reviewboard.repositories.{Repository, RepositorySpec, UserRepository, UserRepositoryLive}

object UserRepositorySpec extends ZIOSpecDefault with RepositorySpec {
  def md5(usPassword: String): String = {
    import java.math.BigInteger
    import java.security.MessageDigest
    val md = MessageDigest.getInstance("MD5")
    val digest: Array[Byte] = md.digest(usPassword.getBytes)
    val bigInt = new BigInteger(1, digest)
    val hashedPassword = bigInt.toString(16).trim
    prependWithZeros(hashedPassword)
  }

  /**
   * This uses a little magic in that the string I start with is a
   * “format specifier,” and it states that the string it returns
   * should be prepended with blank spaces as needed to make the
   * string length equal to 32. Then I replace those blank spaces
   * with the character `0`.
   */
  private def prependWithZeros(pwd: String): String =
    "%1$32s".format(pwd).replace(' ', '0')

  val rightUser= User(
    id = 1L,
    email = "test@tsgcompany.com",
    hashedPassword = md5("password")
  )

  override val initScript: String = "sql/users.sql"
  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("UserRepositorySpec")(
      test("create user"){
        for {
          repo <- ZIO.service[UserRepository]
          user <- repo.create(rightUser)
        } yield assertTrue(
          user.id == rightUser.id &&
            user.email == rightUser.email &&
            user.hashedPassword == rightUser.hashedPassword
        )

      },
      test("update user"){
        for {
          repo <- ZIO.service[UserRepository]
          user <- repo.create(rightUser)
          updated <- repo.update(user.id, _.copy(email = "test2@tsgcompany.com", hashedPassword=md5("password2")))
        } yield assertTrue(
          updated.id == 1L &&
          updated.email == "test2@tsgcompany.com" &&
            updated.hashedPassword == md5("password2")
        )

      },
      test("get user by id, by email"){
        for {
          repo <- ZIO.service[UserRepository]
          user <- repo.create(rightUser)
          userById <- repo.getById(user.id)
          userByEmail <- repo.getByEmail(user.email)
        } yield assertTrue(
          userById.contains(user) &&
            userByEmail.contains(user)
        )
      },
      test("delete user"){
        for {
          repo <- ZIO.service[UserRepository]
          user <- repo.create(rightUser)
          _ <- repo.delete(user.id)
          maybeUser <- repo.getById(user.id)
        } yield assertTrue(
          maybeUser.isEmpty
        )
      }
    ).provide(
      UserRepositoryLive.layer,
      dataSourceLayer,
      Repository.quillLayer,
      Scope.default
    )


}
