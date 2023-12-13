package com.libertexgroup.reviewboard.http.controllers

import com.libertexgroup.reviewboard.http.endpoints.HealthEndpoint
import zio.*

class HealthController private extends HealthEndpoint{
  val health = healthEndpoint
    .serverLogicSuccess[Task](_ => ZIO.succeed("All good!"))
}

object HealthController {
  val makeZIO = ZIO.succeed(new HealthController)
}
