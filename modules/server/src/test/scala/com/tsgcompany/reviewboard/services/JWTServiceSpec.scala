package com.tsgcompany.reviewboard.services



import zio.*
import zio.test.*

import com.tsgcompany.reviewboard.services.*
import com.tsgcompany.reviewboard.domain.data.*
import com.tsgcompany.reviewboard.config.JWTConfig

object JWTServiceSpec extends ZIOSpecDefault {


  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("JWTServiceSpec")(
      test("create and validate token") {
        for {
          service <- ZIO.service[JWTService]
          token <- service.createToken(User(1L, "tsg@tsgcompany.com", "unimportant")) // string
          user <- service.verifyToken(token.token)
        } yield assertTrue(
          user.id == 1L &&
            user.email == "tsg@tsgcompany.com"
        )
      }
    ).provide(
      JWTServiceLive.layer,
      ZLayer.succeed(JWTConfig("secret", 3600))
    )
}
