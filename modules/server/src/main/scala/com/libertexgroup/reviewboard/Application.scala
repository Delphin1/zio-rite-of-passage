package com.libertexgroup.reviewboard

import com.libertexgroup.reviewboard.http.HttpApi
import com.libertexgroup.reviewboard.http.controllers.*
import com.libertexgroup.reviewboard.servcies.*
import com.libertexgroup.reviewboard.repositories.*
import com.libertexgroup.reviewboard.repositories.Repository.dataLayer
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
    // services
    CompanyServiceLive.layer,
    //repos
    CompanyRepositoryLive.layer,
    // other requirements
    dataLayer
    //CompanyService.dummyLayer
  ) // Console.printLine("Hello world!")

}
