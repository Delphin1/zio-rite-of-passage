package com.tsgcompany.reviewboard.http.endpoints

import com.tsgcompany.reviewboard.domain.data.Review
import com.tsgcompany.reviewboard.http.requests.CreateReviewRequest
import sttp.tapir.*
import sttp.tapir.json.zio.*
import sttp.tapir.generic.auto.*
trait ReviewEndpoints extends BaseEndpoint {
  // post /review { CreateReviewRequest} - create review
  // return a Review
  val createEnpoint = secureBaseEndpoint
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


}
