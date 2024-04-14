package com.tsgcompany.reviewboard.http.endpoints

import com.tsgcompany.reviewboard.domain.data.*
import com.tsgcompany.reviewboard.http.requests.*
import sttp.tapir.*
import sttp.tapir.json.zio.*
import sttp.tapir.generic.auto.*
import sttp.tapir.server.*
import zio.*

trait ReviewEndpoints extends BaseEndpoint {

  // post /review { CreateReviewRequest} - create review
  // return a Review
  val createEnpoint: Endpoint[String, CreateReviewRequest, Throwable, Review, Any] = secureBaseEndpoint
    .tag("Reviews")
    .name("create")
    .description("Add a review for a company")
    .in("reviews")
    .post
    .in(jsonBody[CreateReviewRequest])
    .out(jsonBody[Review])

  //get /reviews/id - get review by id
  // return Option[Review]
  val getByIdEnpoint = baseEndpoint
    .tag("Reviews")
    .name("getById")
    .description("Get a review by its id")
    .in("reviews" / path[Long]("id"))
    .get
    .out(jsonBody[Option[Review]])

  // get /reviews/company/id - get review by company id
  // return List[Review]

  val getByCompanyIdEnpoint = baseEndpoint
    .tag("Reviews")
    .name("getByCompanyId")
    .description("Get a review for a company")
    .in("reviews" / "company" /  path[Long]("id"))
    .get
    .out(jsonBody[List[Review]])
  
  // summary endpoints
  val getSummaryEndpoint = baseEndpoint
      .tag("Reviews")
      .name("get summa by company Id")
      .description("Get current review summary for a company Id")
      .in("reviews" / "company" / path[Long]("id") / "summary")
      .get
      .out(jsonBody[Option[ReviewSummary]])
//
  val makeSummaryEndpoint = baseEndpoint
      .tag("Reviews")
      .name("generateSummaByCompanyId")
      .description("Trigger review summary creation for a company Id")
      .in("reviews" / "company" / path[Long]("id") / "summary")
      .post
      .out(jsonBody[Option[ReviewSummary]])

}
