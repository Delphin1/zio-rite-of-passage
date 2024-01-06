package com.tsgcompany.reviewboard.http.endpoints

import com.tsgcompany.reviewboard.domain.data.*
import com.tsgcompany.reviewboard.http.requests.*
import sttp.tapir.*
import sttp.tapir.json.zio.*
import sttp.tapir.generic.auto.*
trait CompanyEndpoints extends BaseEndpoint {
    val createEndpoint =
      baseEndpoint
        .tag("companies")
        .name("create")
        .description("Create a listg for a company")
        .in("companies")
        .post
        .in(jsonBody[CreateCompanyRequest])
        .out(jsonBody[Company])
    val getAllEndpoint =
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

}
