package com.tsgcompany.reviewboard.http.controllers

import sttp.tapir.server.ServerEndpoint
import zio.*
import com.tsgcompany.reviewboard.http.endpoints.ReviewEndpoints
import com.tsgcompany.reviewboard.servcies.ReviewService


class ReviewController private(reviewService: ReviewService) extends BaseController with ReviewEndpoints {

  val create: ServerEndpoint[Any, Task] =
    createEnpoint.serverLogicSuccess(req => reviewService.create(req, -1L)) /* TODO add user id */
    
  val getById: ServerEndpoint[Any, Task] =
    getByIdEnpoint.serverLogicSuccess(id => reviewService.getById(id))  
    
  val getByCompanyId: ServerEndpoint[Any, Task] =
    getByCompanyIdEnpoint.serverLogicSuccess(companyId => reviewService.getByCompanyId(companyId))

  override val routes: List[ServerEndpoint[Any, Task]] = 
    List(create, getById, getByCompanyId)
}

object ReviewController {
  val makeZIO = for {
    service <- ZIO.service[ReviewService]
  } yield new ReviewController(service)
}
