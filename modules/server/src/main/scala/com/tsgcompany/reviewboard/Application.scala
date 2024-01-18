package com.tsgcompany.reviewboard

import com.tsgcompany.reviewboard.config.{Configs, JWTConfig}
import com.tsgcompany.reviewboard.http.HttpApi
import com.tsgcompany.reviewboard.http.controllers.*
import com.tsgcompany.reviewboard.servcies.*
import com.tsgcompany.reviewboard.repositories.*
import com.tsgcompany.reviewboard.repositories.Repository.dataLayer
import sttp.tapir.*
import sttp.tapir.server.ziohttp.*
import zio.*
import zio.http.Server
object Application extends ZIOAppDefault {

  val serverProgram = for {
    endpoints <- HttpApi.endpointsZIO
    _  <- Server.serve(
        ZioHttpInterpreter(
          ZioHttpServerOptions.default
        ).toHttp(endpoints)
    )
    _ <- Console.printLine("Server Started")
  } yield ()

  override def run  = serverProgram.provide(
    Server.default,
    // configs
    Configs.makeConfigLayer[JWTConfig]("tsgcompany.jwt"),
    // services
    CompanyServiceLive.layer,
    ReviewServiceLive.layer,
    UserServiceLive.layer,
    JWTServiceLive.layer,
    //repos
    CompanyRepositoryLive.layer,
    ReviewRepositoryLive.layer,
    UserRepositoryLive.layer,
    // other requirements
    dataLayer
    //CompanyService.dummyLayer
  ) // Console.printLine("Hello world!")

}
