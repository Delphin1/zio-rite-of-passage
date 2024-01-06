package com.tsgcompany.reviewboard.http.controllers

import sttp.tapir.server.ServerEndpoint
import zio.*
import com.tsgcompany.reviewboard.http.endpoints.ReviewEndpoints
import com.tsgcompany.reviewboard.servcies.ReviewService


class ReviewController private(reviewService: ReviewService) extends BaseController with ReviewEndpoints {

  val create: ServerEndpoint[Any, Task] =
    createEnpoint.serverLogic(req => reviewService.create(req, -1L).either) /* TODO add user id */

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
  } yield new ReviewController(service)
}
