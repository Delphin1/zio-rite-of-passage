package com.tsgcompany.reviewboard.integration

import com.tsgcompany.reviewboard.config.{JWTConfig, RecoveryTokensConfig}
import com.tsgcompany.reviewboard.domain.data.UserToken
import com.tsgcompany.reviewboard.http.controllers.*
import com.tsgcompany.reviewboard.http.requests.{DeleteAccountRequest, ForgotPasswordRequest, LoginRequest, RecoverPasswordRequest, RegisterUserAccount, UpdatePasswordRequest}
import com.tsgcompany.reviewboard.http.responses.UserResponse
import com.tsgcompany.reviewboard.repositories.Repository.dataSourceLayer
import com.tsgcompany.reviewboard.repositories.*
import com.tsgcompany.reviewboard.servcies.*
import sttp.client3.{SttpBackend, UriContext, basicRequest}
import sttp.client3.testing.SttpBackendStub
import sttp.model.Method
import sttp.monad.MonadError
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.stub.TapirStubInterpreter
import sttp.tapir.ztapir.RIOMonadError
import zio.*
import zio.test.*
import zio.json.*

object UserFlowSpec extends ZIOSpecDefault with RepositorySpec {
  // http controller
  // service
  // repository
  // test container
  private given zioME: MonadError[Task] = new RIOMonadError[Any]

  override val initScript: String = "sql/integration.sql"

  val userEmail = "tsg@tsgcompany.com"

  private def backendStubZIO = for {
    controller <- UserController.makeZIO
    backendStub <- ZIO.succeed(
      TapirStubInterpreter(SttpBackendStub(MonadError[Task]))
        .whenServerEndpointsRunLogic(controller.routes)
        .backend()
    )
  } yield backendStub

  extension [A: JsonCodec](backend: SttpBackend[Task, Nothing]) {
    def sendRequest[B: JsonCodec](
        method: Method,
        path: String,
        payload: A,
        maybeToken: Option[String] = None
    ): Task[Option[B]] =
      basicRequest
        .method(method, uri"$path")
        .body(payload.toJson)
        .auth
        .bearer(
          maybeToken.getOrElse("")
        )
        .send(backend)
        .map(_.body)
        .map(_.toOption.flatMap(payload => payload.fromJson[B].toOption))

    def postRequest[B: JsonCodec](path: String, payload: A): Task[Option[B]] =
      sendRequest(Method.POST, path, payload, None)
    def postAuthRequest[B: JsonCodec](path: String, payload: A, token: String): Task[Option[B]] =
      sendRequest(Method.POST, path, payload, Some(token))
    def postNoResponse(path: String, payload: A): Task[Unit] =
      basicRequest
        .method(Method.POST, uri"$path")
        .body(payload.toJson)
        .send(backend).unit
    def putRequest[B: JsonCodec](path: String, payload: A): Task[Option[B]] =
      sendRequest(Method.PUT, path, payload, None)
    def putAuthRequest[B: JsonCodec](path: String, payload: A, token: String): Task[Option[B]] =
      sendRequest(Method.PUT, path, payload, Some(token))
    def deleteRequest[B: JsonCodec](path: String, payload: A): Task[Option[B]] =
      sendRequest(Method.DELETE, path, payload, None)
    def deleteAuthRequest[B: JsonCodec](path: String, payload: A, token: String): Task[Option[B]] =
      sendRequest(Method.DELETE, path, payload, Some(token))
  }

  class EmailServiceProbe extends EmailService {
    val db = collection.mutable.Map[String, String]()
    override def sendEmail(to: String, subject: String, content: String): Task[Unit] = ZIO.unit
    override def sendPasswordRecovery(to: String, token: String): Task[Unit] =
      ZIO.succeed(db += (to -> token))
    // specific to the test
    def probeToken(email: String): Task[Option[String]] = ZIO.succeed(db.get(email))

  }

  val emailServiceLayer: ZLayer[Any, Nothing, EmailServiceProbe] = ZLayer.succeed(new EmailServiceProbe)

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("UserFlowSpec")(
      test("create user") {
        for {
          backendStub <- backendStubZIO
          maybeResponse <- backendStub.postRequest[UserResponse](
            "/users",
            RegisterUserAccount(userEmail, "test")
          )
        } yield assertTrue(maybeResponse.contains(UserResponse(userEmail)))
      },
      test("create and login") {
        for {
          backendStub <- backendStubZIO
          maybeResponse <- backendStub.postRequest[RegisterUserAccount]("/users", RegisterUserAccount(userEmail, "test"))
          maybeToken <- backendStub.postRequest[UserToken]("/users/login", LoginRequest(userEmail, "test"))
        } yield assertTrue(
          maybeToken.exists(_.email == userEmail)
        )
      },
      test("change password") {
        for {
          backendStub <- backendStubZIO
          maybeResponse <- backendStub.postRequest[RegisterUserAccount]("/users", RegisterUserAccount(userEmail, "test"))
          userToken <- backendStub
            .postRequest[UserToken]("/users/login", LoginRequest(userEmail, "test"))
            .someOrFail(new RuntimeException("Authentication failed"))
          _ <- backendStub
            .putAuthRequest[UserResponse]("/users/password", UpdatePasswordRequest(userEmail, "test", "newtest"), userToken.token)
          maybeOldToken <- backendStub
            .postRequest[UserToken]("/users/login", LoginRequest(userEmail, "test"))
          maybeNewToken <- backendStub
            .postRequest[UserToken]("/users/login", LoginRequest(userEmail, "newtest"))
        } yield assertTrue(
          maybeOldToken.isEmpty && maybeNewToken.nonEmpty
        )
      },
      test("delete user") {
        for {
          backendStub <- backendStubZIO
          userRepo <- ZIO.service[UserRepository]
          maybeResponse <- backendStub.postRequest[RegisterUserAccount]("/users", RegisterUserAccount(userEmail, "test"))
          maybeOldUser <- userRepo.getByEmail(userEmail)
          userToken <- backendStub
            .postRequest[UserToken]("/users/login", LoginRequest(userEmail, "test"))
            .someOrFail(new RuntimeException("Authentication failed"))
          _ <- backendStub
            .deleteAuthRequest[UserResponse]("/users", DeleteAccountRequest(userEmail, "test"),userToken.token)
          maybeUser <- userRepo.getByEmail(userEmail)
        } yield assertTrue(
          maybeOldUser.filter(_.email == userEmail).nonEmpty && maybeUser.isEmpty
        )
      },
      test("recovey password flow")(
        for {
          backendStub <- backendStubZIO
          // register a user
          _ <- backendStub.postRequest[RegisterUserAccount]("/users", RegisterUserAccount(userEmail, "test"))
          // trigger recovery password flow
          _ <- backendStub.postNoResponse("/users/forgot", ForgotPasswordRequest(userEmail))
          emailServiceProbe <- ZIO.service[EmailServiceProbe]
          token <- emailServiceProbe.probeToken(userEmail)
            .someOrFail(new RuntimeException("token was NOT emailed"))
          _ <- backendStub.postNoResponse("users/recover", RecoverPasswordRequest(userEmail, token, "newtest"))
          maybeOldToken <- backendStub
            .postRequest[UserToken]("/users/login", LoginRequest(userEmail, "test"))
          maybeNewToken <- backendStub
            .postRequest[UserToken]("/users/login", LoginRequest(userEmail, "newtest"))
        } yield assertTrue(
          maybeOldToken.isEmpty && maybeNewToken.nonEmpty
        )
      )
    ).provide(
      UserServiceLive.layer,
      JWTServiceLive.layer,
      UserRepositoryLive.layer,
      RecoveryTokenRepositoryLive.layer,
      emailServiceLayer,
      dataSourceLayer,
      Repository.quillLayer,
      ZLayer.succeed(JWTConfig("secret", 3600)),
      ZLayer.succeed(RecoveryTokensConfig(24 * 3600)),
      Scope.default
    )

}
