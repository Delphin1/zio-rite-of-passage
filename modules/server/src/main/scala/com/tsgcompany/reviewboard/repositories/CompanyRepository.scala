
package com.tsgcompany.reviewboard.repositories

import com.tsgcompany.reviewboard.domain.data.*
import zio.*
import io.getquill.*
import io.getquill.jdbczio.Quill


trait CompanyRepository {
  def create(company: Company): Task[Company]
  def update(id: Long, op: Company => Company): Task[Company]
  def delete(id: Long): Task[Company]
  def getById(id:Long): Task[Option[Company]]
  def getBySlug(slug:String): Task[Option[Company]]
  def get: Task[List[Company]]
  def uniqueAttributes: Task[CompanyFilter]
  def search(filter:CompanyFilter): Task[List[Company]]
}

class CompanyRepositoryLive private (quill: Quill.Postgres[SnakeCase]) extends CompanyRepository {
  import quill.*

  inline given schema: SchemaMeta[Company] = schemaMeta[Company]("companies")
  inline given insSchema: InsertMeta[Company] = insertMeta[Company](_.id)
  inline given upMeta: UpdateMeta[Company] = updateMeta[Company](_.id)

  override def create(company: Company): Task[Company] = {
    run {
      query[Company]
        .insertValue(lift(company))
        .returning(r => r)
    }
  }

  override def getById(id: Long): Task[Option[Company]] = {
    run {
      query[Company].filter(_.id == lift(id)) // List[Company]
    }.map(_.headOption)
  }

  override def getBySlug(slug: String): Task[Option[Company]] = {
    run {
      query[Company].filter(_.slug == lift(slug))
    }.map(_.headOption)
  }
  override def update(id: Long, op: Company => Company): Task[Company] =
    for {
      current <- getById(id).someOrFail(new RuntimeException(s"Couldn't update: missing id $id"))
      updated <- run{
        query[Company]
          .filter(_.id == lift(id))
          .updateValue(lift(op(current)))
          .returning(r => r)
      }
    } yield updated

  override def delete(id: Long): Task[Company] =
    run {
      query[Company]
        .filter(_.id == lift(id))
        .delete
        .returning(r => r)
    }

  override def get: Task[List[Company]] = run(query[Company])

  override def uniqueAttributes: Task[CompanyFilter] =
    for {
      locations <- run(query[Company].map(_.location).distinct).map(_.flatMap(_.toList))
      countries <- run(query[Company].map(_.country).distinct).map(_.flatMap(_.toList))
      indutries <- run(query[Company].map(_.industry).distinct).map(_.flatMap(_.toList))
      tags <- run(query[Company].map(_.tags).distinct).map(_.flatten.toSet.toList)
    } yield CompanyFilter(locations, countries, indutries, tags)

  override def search(filter: CompanyFilter): Task[List[Company]] =
    // select * from companies wehere location in filter.locations and count in and ..
    if (filter.isEmpty) get
    else
      run{
        query[Company]
          .filter { company =>
            liftQuery(filter.locations.toSet).contains(company.location) ||
              liftQuery(filter.countries.toSet).contains(company.country) ||
              liftQuery(filter.industries.toSet).contains(company.industry) ||
              sql"${company.tags} && ${lift(filter.tags)}".asCondition
//              liftQuery(filter.tags.toSet).filter(t => company.tags.contains(t)).nonEmpty
//            query[Company]
//              .filter(_.id == company.id)
//              .concatMap(_.tags) //List[String
//              .filter(tag => liftQuery(filter.tags.toSet).contains(tag))
//              .nonEmpty
          }
      }
}

object CompanyRepositoryLive {
  val layer: ZLayer[Quill.Postgres[SnakeCase.type], Nothing, CompanyRepositoryLive] = ZLayer {
    ZIO.service[Quill.Postgres[SnakeCase.type]].map(quill => CompanyRepositoryLive(quill))
  }
}

object CompanyRepositoryDemo extends ZIOAppDefault {
  val program: ZIO[CompanyRepository, Throwable, Unit] = for {
    repo <- ZIO.service[CompanyRepository]
    _ <- repo.create(Company(-1L, "test-test", "Test test", "test.com"))
  } yield ()

  override def run: ZIO[Any, Throwable, Unit] = program.provide(
    CompanyRepositoryLive.layer,
    Quill.Postgres.fromNamingStrategy(SnakeCase), // quill instance
    Quill.DataSource.fromPrefix("tsgcompany.db")
  )
}