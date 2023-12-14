package com.libertexgroup.reviewboard.http.endpoints

import com.libertexgroup.reviewboard.domain.data.*
import com.libertexgroup.reviewboard.http.requests.*
import sttp.tapir.*
import sttp.tapir.json.zio.*
import sttp.tapir.generic.auto.*
trait CompanyEndpoints {
    val createEndpoint =
      endpoint
        .tag("companies")
        .name("create")
        .description("Create a listg for a company")
        .in("companies")
        .post
        .in(jsonBody[CreateCompanyRequest])
        .out(jsonBody[Company])
    val getAllEndpoint =
      endpoint
        .tag("companies")
        .name("getAll")
        .description("get all company listings")
        .in("companies")
        .get
        .out(jsonBody[List[Company]])

    val getByIdEndpoint =
      endpoint
        .tag("companies")
        .name("getById")
        .description("get company by its id or more slag")
        .in("companies" / path[String]("id"))
        .get
        .out(jsonBody[Option[Company]])

}
