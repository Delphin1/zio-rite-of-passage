package com.tsgcompany.reviewboard.repositories

import com.tsgcompany.reviewboard.domain.data.{Company, CompanyFilter}
import com.tsgcompany.reviewboard.services.CompanyServiceSpec.test
import com.tsgcompany.reviewboard.syntax.*
import zio.*
import zio.test.*

import java.sql.SQLException
//import io.getquill.autoQuote

import javax.sql.DataSource

object CompanyRepositorySpec extends ZIOSpecDefault with RepositorySpec {

  private val testCompany = Company(-1L, "test-test", "Test test", "test.com")

  private def genString(): String =
    scala.util.Random.alphanumeric.take(8).mkString

  override val initScript: String = "sql/companies.sql"  

  private def genCompany(): Company =
    Company(
      id = -1L,
      slug = genString(),
      name = genString(),
      url = genString(),
      location = Some(genString()),
      country = Some(genString()),
      industry = Some(genString()),
      tags = (1 to 3).map(_ => genString()).toList
    )

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("CompanyRepositorySpec")(
//      test("create") {
//        val companyZIO = service(_.create(CreateCompanyRequest("Test test", "test.com")))
//        companyZIO.assert { company =>
//          company.name == "Test test" &&
//            company.url ==  "test.com" &&
//            company.slug == "test-test"
//        }
//      },

      test("create a company") {
        val program = for {
          repo    <- ZIO.service[CompanyRepository]
          company <- repo.create(testCompany)
        } yield company

        program.assert {
          case Company(_, "test-test", "Test test", "test.com", _, _, _, _, _) => true
          case _                                                               => false
        }
      },
      test("creating a duplicate should error") {
        val program = for {
          repo <- ZIO.service[CompanyRepository]
          _    <- repo.create(testCompany)
          err  <- repo.create(testCompany).flip
        } yield err

        program.assert(_.isInstanceOf[SQLException])

      },
      test("get by id and slug") {
        val program = for {
          repo      <- ZIO.service[CompanyRepository]
          company   <- repo.create(testCompany)
          fetchById <- repo.getById(company.id)
          fetchSlug <- repo.getBySlug(company.slug)

        } yield (company, fetchById, fetchSlug)

        program.assert { case (company, fetchById, fetchSlug) =>
          fetchById.contains(company) && fetchSlug.contains(company)
        }

      },
      test("update records") {
        val program = for {
          repo      <- ZIO.service[CompanyRepository]
          company   <- repo.create(testCompany)
          updated   <- repo.update(company.id, _.copy(url = "newtest.com"))
          fetchById <- repo.getById(company.id)
        } yield (updated, fetchById)

        program.assert { case (updated, fetchById) =>
          fetchById.contains(updated)
        }
      },
      test("for delete") {
        val program = for {
          repo      <- ZIO.service[CompanyRepository]
          company   <- repo.create(testCompany)
          _         <- repo.delete(company.id)
          fetchById <- repo.getById(company.id)
        } yield fetchById

        program.assert(_.isEmpty)
      },
      test("for get all records") {
        val program = for {
          repo             <- ZIO.service[CompanyRepository]
          companies        <- ZIO.foreach(1 to 10)(_ => repo.create(genCompany()))
          companiesFetched <- repo.get
        } yield (companies, companiesFetched)

        program.assert { case (companies, companiesFetched) =>
          companies.toSet == companiesFetched.toSet
        }
      },
      test("search by tag") {
        val program = for {
          repo             <- ZIO.service[CompanyRepository]
          company        <- repo.create(genCompany())
          fetched   <- repo.search(CompanyFilter(tags = company.tags.headOption.toList))
        } yield (fetched, company)  
        
        program.assert{ case (fetched, company) =>
          fetched.nonEmpty && fetched.tail.isEmpty && fetched.head == company  
        }
      }
    ).provide(
      CompanyRepositoryLive.layer,
      dataSourceLayer,
      Repository.quillLayer,
      Scope.default
    )

}
