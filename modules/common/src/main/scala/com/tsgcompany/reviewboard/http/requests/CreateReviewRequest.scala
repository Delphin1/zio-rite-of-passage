package com.tsgcompany.reviewboard.http.requests

import com.tsgcompany.reviewboard.domain.data.Review
import zio.json.{DeriveJsonCodec, JsonCodec}


case class CreateReviewRequest (
                                 companyId: Long,
                                 userId: Long, // FK
                                 management: Int, //1-5
                                 culture: Int,
                                 salary: Int, //
                                 benefits: Int,
                                 wouldRecommend: Int,
                                 review: String
                               )

object CreateReviewRequest{
  given codec: JsonCodec[CreateReviewRequest] = DeriveJsonCodec.gen[CreateReviewRequest]
  def fromReview(review: Review) = CreateReviewRequest(
      companyId = review.companyId,
      userId = review.userId,
      management = review.management,
      culture = review.culture,
      salary = review.salary,
      benefits = review.benefits,
      wouldRecommend = review.wouldRecommend,
      review = review.review
  )
}