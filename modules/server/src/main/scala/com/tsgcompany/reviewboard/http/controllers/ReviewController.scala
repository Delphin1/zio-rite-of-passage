package com.tsgcompany.reviewboard.http.controllers

import com.tsgcompany.reviewboard.domain.data.UserId
import sttp.tapir.server.ServerEndpoint
import zio.*
import com.tsgcompany.reviewboard.services.{JWTService, ReviewService}
import com.tsgcompany.reviewboard.http.endpoints.*


class ReviewController private(reviewService: ReviewService, jwtService: JWTService) extends BaseController with ReviewEndpoints {

  val create: ServerEndpoint[Any, Task] =
    createEnpoint
      .serverSecurityLogic[UserId, Task](token => jwtService.verifyToken(token).either)
      .serverLogic(userId => req => reviewService.create(req, userId.id).either) /* TODO add user id */

  val getById: ServerEndpoint[Any, Task] =
    getByIdEnpoint.serverLogic(id => reviewService.getById(id).either)

  val getByCompanyId: ServerEndpoint[Any, Task] =
    getByCompanyIdEnpoint.serverLogic(companyId => reviewService.getByCompanyId(companyId).either)

  override val routes: List[ServerEndpoint[Any, Task]] =
    List(create, getById, getByCompanyId)
}

object ReviewController {
  val makeZIO = for {
    service <- ZIO.service[ReviewService]
    jwtService <- ZIO.service[JWTService]
  } yield new ReviewController(service, jwtService)
}
