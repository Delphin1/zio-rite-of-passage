package com.tsgcompany.reviewboard.services

import zio.*
import zio.json.{DeriveJsonCodec, JsonCodec}
import com.tsgcompany.reviewboard.domain.data.{Review, ReviewSummary}
import com.tsgcompany.reviewboard.http.requests.CreateReviewRequest
import com.tsgcompany.reviewboard.repositories.ReviewRepository

import java.time.Instant

trait ReviewService {
  def create(request: CreateReviewRequest, userId: Long): Task[Review]
  def getById(id: Long): Task[Option[Review]]
  def getByCompanyId(companyId: Long): Task[List[Review]]
  def getByUserId(userId: Long): Task[List[Review]]
  def getSummary(companyId: Long): Task[Option[ReviewSummary]]
  def makeSummary(companyId: Long): Task[Option[ReviewSummary]]

}

class ReviewServiceLive private (repo: ReviewRepository) extends ReviewService {

  override def create(request: CreateReviewRequest, userId: Long): Task[Review] =
    repo.create(
      Review(
          id = -1L,
          companyId = request.companyId,
          userId = userId,
          management = request.management,
          culture = request.culture,
          salary = request.salary,
          benefits = request.benefits,
          wouldRecommend = request.wouldRecommend,
          review = request.review,
          created = Instant.now(),
          updated = Instant.now(),
      )
    )

  override def getById(id: Long): Task[Option[Review]] =
    repo.getById(id)

  override def getByCompanyId(companyId: Long): Task[List[Review]] =
    repo.getByCompanyId(companyId)

  override def getByUserId(userId: Long): Task[List[Review]] =
    repo.getByUserId(userId)

  override def getSummary(companyId: Long): Task[Option[ReviewSummary]] =
    repo.getSummary(companyId)

  override  def makeSummary(companyId: Long): Task[Option[ReviewSummary]] =
    ZIO.fail(new RuntimeException("Not implemented"))
    //repo.insertSummary()
}

object ReviewServiceLive {
  val layer = ZLayer {
    ZIO.service[ReviewRepository].map(repo => new ReviewServiceLive(repo))
  }
}
