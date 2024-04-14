package com.tsgcompany.reviewboard.http.controllers

import com.tsgcompany.reviewboard.domain.data.*
import com.tsgcompany.reviewboard.http.endpoints.ReviewEndpoints
import com.tsgcompany.reviewboard.services.*
import sttp.tapir.server.*
import zio.*



class ReviewController (reviewService: ReviewService, jwtService: JWTService) extends BaseController with ReviewEndpoints {
  
  val create: ServerEndpoint[Any, Task] =
    createEnpoint
      .serverSecurityLogic[UserId, Task](token => jwtService.verifyToken(token).either)
      .serverLogic(userId => req => reviewService.create(req, userId.id).either) /* TODO add user id */

  val getById: ServerEndpoint[Any, Task] =
    getByIdEnpoint.serverLogic(id => reviewService.getById(id).either)

  val getByCompanyId: ServerEndpoint[Any, Task] =
    getByCompanyIdEnpoint.serverLogic(companyId => reviewService.getByCompanyId(companyId).either)
    
  val getSummary: ServerEndpoint[Any, Task] = 
    getSummaryEndpoint.serverLogic(companyId => reviewService.getSummary(companyId).either)

  val makeSummary: ServerEndpoint[Any, Task] = 
    makeSummaryEndpoint.serverLogic(companyId => reviewService.makeSummary(companyId).either)

  override val routes: List[ServerEndpoint[Any, Task]] =
    List(getSummary, makeSummary, create,  getById, getByCompanyId)
}

object ReviewController {
  val makeZIO = for {
    service <- ZIO.service[ReviewService]
    jwtService <- ZIO.service[JWTService]
  } yield new ReviewController(service, jwtService)
}
