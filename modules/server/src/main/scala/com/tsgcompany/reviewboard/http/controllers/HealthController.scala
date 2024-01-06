package com.tsgcompany.reviewboard.http.controllers

import com.tsgcompany.reviewboard.http.endpoints.HealthEndpoint
import sttp.tapir.server.ServerEndpoint
import zio.*

class HealthController private extends BaseController with HealthEndpoint{
  val health = healthEndpoint
    .serverLogicSuccess[Task](_ => ZIO.succeed("All good!"))
  override val routes: List[ServerEndpoint[Any, Task]] = List(health)
}

object HealthController {
  val makeZIO = ZIO.succeed(new HealthController)
}