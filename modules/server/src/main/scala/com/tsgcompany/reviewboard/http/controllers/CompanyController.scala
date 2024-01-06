package com.tsgcompany.reviewboard.http.controllers

import com.tsgcompany.reviewboard.domain.data.Company
import com.tsgcompany.reviewboard.http.endpoints.CompanyEndpoints
import com.tsgcompany.reviewboard.servcies.CompanyService
import sttp.tapir.server.ServerEndpoint
import zio.*

import collection.mutable

class CompanyController private (service: CompanyService) extends BaseController with CompanyEndpoints {


  //create
  val create: ServerEndpoint[Any, Task]= createEndpoint.serverLogic { req =>
    service.create(req).either
  }

  val getAll: ServerEndpoint[Any, Task] =
    getAllEndpoint.serverLogic { _ =>
      service.getAll.either
    }

  val getById: ServerEndpoint[Any, Task] = getByIdEndpoint.serverLogic { id =>
    ZIO
      .attempt(id.toLong)
      .flatMap(service.getById)
      .catchSome {
        case _: NumberFormatException =>
          service.getBySlag(id)
      }.either
  }


  override val routes: List[ServerEndpoint[Any, Task]] = List(create, getAll, getById)
}


object CompanyController {
  val makeZIO = for {
    service <- ZIO.service[CompanyService]
  } yield new CompanyController(service)
}