package com.libertexgroup.reviewboard.repositories

import zio.*
import zio.test.*
import com.libertexgroup.reviewboard.domain.data.Review
import com.libertexgroup.reviewboard.syntax.*

import java.time.Instant

object ReviewRepositorySpec extends ZIOSpecDefault with RepositorySpec {
  val goodReview: Review = Review(
    id = 1L,
    companyId = 1L,
    userId = 1L,
    management = 5,
    culture = 5,
    salary = 5,
    benefits = 5,
    wouldRecommend = 10,
    review = "all good",
    created = Instant.now(),
    updated = Instant.now()
  )

  val badReview: Review = Review(
    id = 2L,
    companyId = 1L,
    userId = 1L,
    management = 1,
    culture = 1,
    salary = 1,
    benefits = 1,
    wouldRecommend = 1,
    review = "BAD BAD",
    created = Instant.now(),
    updated = Instant.now()
  )

  override val initScript: String = "sql/reviews.sql"

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("ReviewRepostorySpec")(
      test("create review"){
        val program = for {
          repo <- ZIO.service[ReviewRepository]
          review <- repo.create(goodReview)
        } yield review

        program.assert{ review =>
          review.management == goodReview.management &&
          review.culture == goodReview.culture &&
          review.salary == goodReview.salary &&
          review.benefits == goodReview.benefits &&
          review.wouldRecommend == goodReview.wouldRecommend &&
          review.review == goodReview.review
        }
      },
      test("get review by id (id, companyId, userId)") {
        for {
          repo <- ZIO.service[ReviewRepository]
          review <- repo.create(goodReview)
          fetchedReview <- repo.getById(review.id)
          fetchedReview2 <- repo.getByCompanyId(review.companyId)
          fetchedReview3 <- repo.getByUserId(review.userId)
        } yield assertTrue(
          fetchedReview.contains(review) &&
            fetchedReview2.contains(review) &&
            fetchedReview3.contains(review)
        )
      },
      test("get all") {
        for {
          repo <- ZIO.service[ReviewRepository]
          review1 <- repo.create(goodReview)
          review2 <- repo.create(badReview)
          reviewsCompany <- repo.getByCompanyId(1L)
          reviewUser <- repo.getByUserId(1L)

        } yield assertTrue(
          reviewsCompany.toSet == Set(review1, review2) &&
            reviewUser.toSet == Set(review1, review2)
        )
      },
      test("edit review") {
        for {
          repo <- ZIO.service[ReviewRepository]
          review <- repo.create(goodReview)
          updated <- repo.update(review.id, _.copy(review = "not too bad"))

        } yield assertTrue(
          review.id == updated.id &&
          review.companyId == updated.companyId &&
          review.userId == updated.userId &&
          review.management == updated.management &&
          review.culture == updated.culture &&
          review.salary == updated.salary &&
          review.benefits == updated.benefits &&
          review.wouldRecommend == updated.wouldRecommend &&
          review.review == "not tooo bad" &&
          review.created == updated.created &&
          review.updated != updated.updated

        )
      },
      test("delet e review") {
        for {
          repo <- ZIO.service[ReviewRepository]
          review <- repo.create(goodReview)
          _ <- repo.delete(review.id)
          maybeReview <- repo.getById(review.id)
        } yield assertTrue(
          maybeReview.isEmpty
        )
      }  
    ).provide(
      ReviewRepositoryLive.layer,
      dataSourceLayer,
      Repository.quillLayer,
      Scope.default
    )
}
