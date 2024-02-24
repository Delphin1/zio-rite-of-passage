package com.tsgcompany.reviewboard.http.endpoints


import sttp.tapir.*
import sttp.tapir.json.zio.*
import sttp.tapir.generic.auto.*
import zio.*

import com.tsgcompany.reviewboard.domain.data.*
import com.tsgcompany.reviewboard.http.requests.*
trait CompanyEndpoints extends BaseEndpoint {
    val createEndpoint =
      secureBaseEndpoint
        .tag("companies")
        .name("create")
        .description("Create a listg for a company")
        .in("companies")
        .post
        .in(jsonBody[CreateCompanyRequest])
        .out(jsonBody[Company])
      
    val getAllEndpoint: Endpoint[Unit, Unit, Throwable, List[Company], Any] =
      baseEndpoint
        .tag("companies")
        .name("getAll")
        .description("get all company listings")
        .in("companies")
        .get
        .out(jsonBody[List[Company]])

    val getByIdEndpoint =
      baseEndpoint
        .tag("companies")
        .name("getById")
        .description("get company by its id or more slag")
        .in("companies" / path[String]("id"))
        .get
        .out(jsonBody[Option[Company]])

    val allFiltersEndpoint: Endpoint[Unit, Unit, Throwable, CompanyFilter, Any] =
        baseEndpoint
          .tag("companies")
          .name("allFilters")
          .description("Get all possible search filters")
          .in("companies" / "filters")
          .get
          .out(jsonBody[CompanyFilter])

    val searchEndpoint = 
      baseEndpoint
        .tag("companies")
        .name("search")
        .description("Get companies based on filter")
        .in("companies" / "search")
        .post
        .in(jsonBody[CompanyFilter])
        .out(jsonBody[List[Company]])

}
