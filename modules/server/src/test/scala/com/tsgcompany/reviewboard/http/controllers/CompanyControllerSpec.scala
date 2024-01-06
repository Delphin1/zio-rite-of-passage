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

import com.tsgcompany.reviewboard.syntax.assert
import com.tsgcompany.reviewboard.domain.data.Company
import com.tsgcompany.reviewboard.http.requests.CreateCompanyRequest
import com.tsgcompany.reviewboard.servcies.CompanyService
object CompanyControllerSpec extends ZIOSpecDefault {

  private given zioME: MonadError[Task] = new RIOMonadError[Any]

  private val testCompany = Company(1, "test-test", "Test test", "test.com")
  private val serviceStab = new CompanyService {
    override def create(req: CreateCompanyRequest): Task[Company] =
      ZIO.succeed(testCompany)

    override def getBySlag(slug: String): Task[Option[Company]] =
      ZIO.succeed{
        if (slug == testCompany.slug) Some(testCompany)
        else None
      }

    override def getAll: Task[List[Company]] = ZIO.succeed(List(testCompany))

    override def getById(id: Long): Task[Option[Company]] =
      ZIO.succeed{
          if (id == 1) Some(testCompany)
          else None
      }
  }

  private def backendStubZIO(endpointFun: CompanyController => ServerEndpoint[Any, Task]) = for {
    // create the controller
    controller <- CompanyController.makeZIO
    // build tapir backend
    backendStub <- ZIO.succeed(TapirStubInterpreter(SttpBackendStub(MonadError[Task]))
      .whenServerEndpointRunLogic(endpointFun(controller))
      .backend()
    )
  } yield backendStub
  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("CompanyControllerSpec")(
      test("post company") {
        val program = for {
          backendStub <- backendStubZIO(_.create)
          // run http request
          response <- basicRequest
            .post(uri"/companies")
            .body(CreateCompanyRequest("Test test", "test.com").toJson)
            .send(backendStub)
        } yield response.body

        // inspect http response
        // program.assert(_ == 2)
        program.assert { respBody =>
          respBody.toOption
            .flatMap(_.fromJson[Company].toOption) //Option[Company]
            .contains(Company(1, "test-test", "Test test", "test.com"))
        }
      },

      test("getAll"){
        val program = for {
          backendStub <- backendStubZIO(_.getAll)
          response <- basicRequest
            .get(uri"/companies")
            .send(backendStub)
        } yield response.body

        program.assert { respBody =>
            respBody.toOption
              .flatMap(_.fromJson[List[Company]].toOption) //Option[Company]
              .contains(List(testCompany))
        }
      },

      test("getById"){
        val program = for {
          backendStub <- backendStubZIO(_.getById)
          response <- basicRequest
            .get(uri"/companies/1")
            .send(backendStub)
        } yield response.body

        program.assert { respBody =>
            respBody.toOption
              .flatMap(_.fromJson[Company].toOption)
              .contains(testCompany)
          }
      },

      test("simple test"){
                  assertZIO(ZIO.succeed(1 + 1))(
                    Assertion.assertion("basic math")(_ == 2)
                  )
      }
    )
      .provide(ZLayer.succeed(serviceStab))
}

