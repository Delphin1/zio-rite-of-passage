package com.libertexgroup.reviewboard.http.endpoints

import sttp.tapir.*

trait HealthEndpoint {
  val healthEndpoint = endpoint
    .tag("health")
    .name("health")
    .description("health check")
    .get
    .in("health")
    .out(plainBody[String])
}
 