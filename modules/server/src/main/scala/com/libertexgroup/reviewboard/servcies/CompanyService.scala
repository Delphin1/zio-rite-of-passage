package com.libertexgroup.reviewboard.servcies

import zio.*

import collection.mutable
import com.libertexgroup.reviewboard.domain.data.*
import com.libertexgroup.reviewboard.http.requests.CreateCompanyRequest

// Business logic
trait CompanyService {
  def create(req: CreateCompanyRequest) : Task[Company]
  def getAll: Task[List[Company]]
  def getById(id: Long): Task[Option[Company]]
  def getBySlag(slug: String): Task[Option[Company]]
}

object CompanyService {
  val dummyLayer = ZLayer.succeed(new CompanyServiceDummy)
}

class CompanyServiceDummy extends CompanyService {
  // TODO implementation
  val db: mutable.Map[Long, Company] = mutable.Map[Long, Company]()

  override def create(req: CreateCompanyRequest): Task[Company] =
    ZIO.succeed {
      val newId = db.keys.maxOption.getOrElse(0L) + 1
      val newCompany = req.toCompany(newId)
      db += (newId -> newCompany)
      newCompany
    }

  override def getAll: Task[List[Company]] = ZIO.succeed(db.values.toList)

  override def getById(id: Long): Task[Option[Company]] = ZIO
    .attempt(id)
    .map(db.get)

  override def getBySlag(slug: String): Task[Option[Company]] = ZIO.succeed(
    db.values.find(_.slug == slug)
  )

}


