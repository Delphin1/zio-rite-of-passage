package com.tsgcompany.reviewboard.http.controllers

import sttp.client3.testing.SttpBackendStub
import sttp.monad.MonadError
import sttp.client3.*
import sttp.tapir.server.stub.TapirStubInterpreter
import sttp.tapir.ztapir.RIOMonadError
import sttp.tapir.generic.auto.*
import sttp.tapir.server.ServerEndpoint
import zio.*
import zio.test.*
import zio.json.*

import java.time.Instant
import com.tsgcompany.reviewboard.domain.data.{Review, User, UserId, UserToken}
import com.tsgcompany.reviewboard.http.requests.CreateReviewRequest
import com.tsgcompany.reviewboard.services.{JWTService, ReviewService}
import com.tsgcompany.reviewboard.services.UserServiceSpec.tsgUser
import com.tsgcompany.reviewboard.syntax.assert



object ReviewControllerSpec extends ZIOSpecDefault {
  private given zioME: MonadError[Task] = new RIOMonadError[Any]

  private val goodReview: Review = Review(
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

  private val jwtServiceStub = new JWTService {
    override def createToken(user: User): Task[UserToken] =
      ZIO.succeed(UserToken(user.id, user.email, "BIG ACCESS", Long.MaxValue))

    override def verifyToken(token: String): Task[UserId] =
      ZIO.succeed(UserId(tsgUser.id, tsgUser.email))
  }

  private def backendStubZIO(endpointFun: ReviewController => ServerEndpoint[Any, Task]) = for {
    // create the controller
    controller <- ReviewController.makeZIO
    // build tapir backend
    backendStub <- ZIO.succeed(TapirStubInterpreter(SttpBackendStub(MonadError[Task]))
      .whenServerEndpointRunLogic(endpointFun(controller))
      .backend()
    )
  } yield backendStub

  private val serviceStub = new ReviewService:
    override def create(request: CreateReviewRequest, userId: Long): Task[Review] =
      ZIO.succeed(goodReview)
    override def getById(id: Long): Task[Option[Review]] =
      ZIO.succeed{
        if (id == 1L) Some(goodReview)
        else None
      }

    override def getByCompanyId(companyId: Long): Task[List[Review]] =
      ZIO.succeed {
        if (companyId == 1L) List(goodReview)
        else List()
      }

    override def getByUserId(userId: Long): Task[List[Review]] =
      ZIO.succeed {
        if (userId == 1L) List(goodReview)
        else List()
      }

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("ReviewControllerSpec")(
      test("post review"){
        for {
          backendStub <- backendStubZIO(_.create)
          response <- basicRequest
            .post(uri"reviews")
            .body(CreateReviewRequest(
              companyId = 1L,
              userId = 1L,
              management = 5,
              culture = 5,
              salary = 5,
              benefits = 5,
              wouldRecommend = 10,
              review = "all good"
            ).toJson)
            .header("Authorization","Bearer BIG ACCESS")
            .send(backendStub)
        } yield assertTrue(
          response.body.toOption.flatMap(_.fromJson[Review].toOption).contains(goodReview)
        )
      },
          //response.body

//        program.assert { respBody =>
//          respBody.toOption
//            .flatMap(_.fromJson[Review].toOption) //Option[Review]
//            .contains(goodReview)
//        }
      test("get by id") {
        for {
          backendStub <- backendStubZIO(_.getById)
          response <- basicRequest
            .get(uri"reviews/1")
            .send(backendStub)
          responseNotFound <- basicRequest
            .get(uri"reviews/999")
            .send(backendStub)
        } yield assertTrue(
          response.body.toOption.flatMap(_.fromJson[Review].toOption)
            .contains(goodReview) &&
            responseNotFound.body.toOption.flatMap(_.fromJson[Review].toOption).isEmpty

        )
      },
      test("get by company id") {
        for {
          backendStub <- backendStubZIO(_.getByCompanyId)
          response <- basicRequest
            .get(uri"reviews/company/1")  // returns List(goodReview)
            .send(backendStub)
          responseNotFound <- basicRequest
            .get(uri"reviews/company/999") // return List()
            .send(backendStub)
        } yield assertTrue(
          response.body.toOption.flatMap(_.fromJson[List[Review]].toOption)
              .contains(List(goodReview)) &&
            responseNotFound.body.toOption.flatMap(_.fromJson[List[Review]].toOption)
              .contains(List())
        )
      }

    )
      .provide(
        ZLayer.succeed(serviceStub),
        ZLayer.succeed(jwtServiceStub)
      )

}
