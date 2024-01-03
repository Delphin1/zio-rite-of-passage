package com.tsgcompany.reviewboard.services

import com.tsgcompany.reviewboard.domain.data.Company
import com.tsgcompany.reviewboard.http.requests.CreateCompanyRequest
import com.tsgcompany.reviewboard.repositories.CompanyRepository
import com.tsgcompany.reviewboard.servcies.{CompanyService, CompanyServiceLive}
import com.tsgcompany.reviewboard.syntax.*
import zio.*
import zio.test.*

import scala.collection.mutable
object CompanyServiceSpec extends ZIOSpecDefault {

  val service: ZIO.ServiceWithZIOPartiallyApplied[CompanyService] = ZIO.serviceWithZIO[CompanyService]
  //val companyZIO: ZIO[CompanyService, Throwable, Company] = service(_.create(???))
  val stubRepoLayer: ULayer[CompanyRepository] = ZLayer.succeed(
    new CompanyRepository {
      val db: mutable.Map[Long, Company] = collection.mutable.Map[Long, Company]()

      override def create(company: Company): Task[Company] =
        ZIO.succeed{
          val nextId = db.keys.maxOption.getOrElse(0L) + 1
          val newCompany = company.copy(id = nextId)
          db += (nextId -> newCompany)
          newCompany
        }

      override def update(id: Long, op: Company => Company): Task[Company] =
        ZIO.attempt{
          val company = db(id)
          db += (id -> op(company))
          company
        }
      override def delete(id: Long): Task[Company] =
        ZIO.attempt{
          val company = db(id)
          db -= id
          company
        }
      override def getById(id:Long): Task[Option[Company]] =
        ZIO.succeed(db.get(id))

      override def getBySlug(slug:String): Task[Option[Company]] =
        ZIO.succeed(db.values.find(_.slug == slug))

      override def get: Task[List[Company]] =
        ZIO.succeed(db.values.toList)
    }
  )

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("CompanyServiceTest")(
      test("create") {
        val companyZIO = service(_.create(CreateCompanyRequest("Test test", "test.com")))
        companyZIO.assert { company =>
          company.name == "Test test" &&
          company.url ==  "test.com" &&
          company.slug == "test-test"
        }
      },

      test("get by id") {
        // create a company
        // fetch a company by its id
        val program = for {
          company <- service(_.create(CreateCompanyRequest("Test test", "test.com")))
          companyOpt <- service(_.getById(company.id))
        } yield (company, companyOpt)

        program.assert{
          case (company, Some(companyRes)) =>
            company.name == "Test test" &&
              company.url ==  "test.com" &&
              company.slug == "test-test" &&
            company == companyRes
          case _ => false
        }
      },

      test("get by slag") {
        val program = for {
          company <- service(_.create(CreateCompanyRequest("Test test", "test.com")))
          companyOpt <- service(_.getBySlag(company.slug))
        } yield (company, companyOpt)

        program.assert{
          case (company, Some(companyRes)) =>
            company.name == "Test test" &&
              company.url ==  "test.com" &&
              company.slug == "test-test" &&
              company == companyRes
          case _ => false
        }
      },

      test("get all") {

        val program = for {
          company <- service(_.create(CreateCompanyRequest("Test test", "test.com")))
          company2 <- service(_.create(CreateCompanyRequest("Google", "google.com")))
          companies <- service(_.getAll)
        } yield (company, company2, companies)

        program.assert {
          case (company, company2, companies) =>
            companies.toSet == Set(company, company2)
          case _ => false
        }
      }
    ).provide(CompanyServiceLive.layer,
      stubRepoLayer)
}
