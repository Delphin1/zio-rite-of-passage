package com.tsgcompany.reviewboard.http.controllers


import sttp.tapir.*
import sttp.model.StatusCode
import sttp.tapir.server.ServerEndpoint
import zio.*

import com.tsgcompany.reviewboard.http.endpoints.*

import com.tsgcompany.reviewboard.domain.errors.HttpError


class HealthController private extends BaseController with HealthEndpoint{
  val health = healthEndpoint
    .serverLogicSuccess[Task](_ => ZIO.succeed("All good!"))

  val errorRoute = errorEndpoint
    .serverLogic[Task](_ => ZIO.fail(new RuntimeException("Boom!")).either
    ) //Task[Either[Trowable,Sting]];

  override val routes: List[ServerEndpoint[Any, Task]] = List(health, errorRoute)
}

object HealthController {
  val makeZIO = ZIO.succeed(new HealthController)
}
