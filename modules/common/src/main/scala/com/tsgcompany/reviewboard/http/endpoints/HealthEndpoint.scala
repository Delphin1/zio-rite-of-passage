package com.tsgcompany.reviewboard.http.endpoints

import sttp.tapir.*

trait HealthEndpoint extends BaseEndpoint {
  val healthEndpoint = baseEndpoint
    .tag("health")
    .name("health")
    .description("health check")
    .get
    .in("health")
    .out(plainBody[String])

  val errorEndpoint = baseEndpoint
    .tag("health")
    .name("error health")
    .description("health check - should error")
    .get
    .in("health" / "error")
    .out(plainBody[String])
}
 