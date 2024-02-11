package com.tsgcompany.reviewboard.http.controllers

import com.tsgcompany.reviewboard.domain.data.*
import com.tsgcompany.reviewboard.servcies.{CompanyService, JWTService}
import com.tsgcompany.reviewboard.http.endpoints.*
import sttp.tapir.server.ServerEndpoint
import zio.*

import collection.mutable

class CompanyController private (service: CompanyService, jwtService: JWTService) extends BaseController with CompanyEndpoints {


  //create
  val create: ServerEndpoint[Any, Task]= createEndpoint
    .serverSecurityLogic[UserId, Task](token => jwtService.verifyToken(token).either)
    .serverLogic (_ => req => service.create(req).either)

  val getAll: ServerEndpoint[Any, Task] =
    getAllEndpoint.serverLogic { _ =>
      service.getAll.either
    }


  val getById: ServerEndpoint[Any, Task] =
    getByIdEndpoint.serverLogic { id =>
    ZIO
      .attempt(id.toLong)
      .flatMap(service.getById)
      .catchSome {
        case _: NumberFormatException =>
          service.getBySlag(id)
      }
      .either
  }

  val allFilters: ServerEndpoint[Any, Task] =
    allFilterEndpoint.serverLogic { _ =>
      service.allFilters.either

    }



  override val routes: List[ServerEndpoint[Any, Task]] = List(create, getAll, allFilters, getById)
}


object CompanyController {
  val makeZIO = for {
    service <- ZIO.service[CompanyService]
    jwtService <- ZIO.service[JWTService]
  } yield new CompanyController(service, jwtService)
}