package com.libertexgroup.reviewboard

import com.libertexgroup.reviewboard.http.controllers.HealthController
import zio.*
import sttp.tapir.*
import sttp.tapir.server.ziohttp.*
import zio.http.Server
object Application extends ZIOAppDefault {

  val serverProgram = for {
    controller <- HealthController.makeZIO
    _  <- Server.serve(
        ZioHttpInterpreter(
          ZioHttpServerOptions.default
        ).toHttp(controller.health)
    )
    _ <- Console.printLine("Server Started")
  } yield ()

  override def run  = serverProgram.provide(
    Server.default
  ) // Console.printLine("Hello world!")

}
