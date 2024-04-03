package com.tsgcompany.reviewboard.repositories

import com.tsgcompany.reviewboard.domain.data.*
import zio.*
import io.getquill.*
import io.getquill.jdbczio.Quill

trait InviteRepository {
  def getByUserName(userName: String): Task[List[InviteNamedRecord]]
  def getInvitePack(userName: String, companyId: Long): Task[Option[InviteRecord]]
  def addInvitePack(userName: String, companyId: Long, nInvites: Int): Task[Long]
  def activatePack(id: Long): Task[Boolean]
  def markInvites(userName: String, companyId: Long, nInvites: Int): Task[Int]

}

class InviteRepositoryLive private (quill: Quill.Postgres[SnakeCase]) extends InviteRepository {
  import quill.*

  inline given schema: SchemaMeta[InviteRecord] = schemaMeta[InviteRecord]("invites")
  inline given insSchema: InsertMeta[InviteRecord] = insertMeta[InviteRecord](_.id)
  inline given upMeta: UpdateMeta[InviteRecord] = updateMeta[InviteRecord](_.id) 
//company
  inline given companySchema: SchemaMeta[Company] = schemaMeta[Company]("companies")
  inline given companyInsSchema: InsertMeta[Company] = insertMeta[Company](_.id)
  inline given companyUpMeta: UpdateMeta[Company] = updateMeta[Company](_.id)

  /**
   * select companyId, companyName, nInvites
   * from invites, companies
   * where invites.userName == ?
   * and invites.active
   * and invites.nInvites > 0
   * and invites.companyId = companies.id
   *
   * @param userName
   * @return
   */
  override def getByUserName(userName: String): Task[List[InviteNamedRecord]] =
    run (
      for {
        record <- query[InviteRecord]
          .filter(_. userName == lift(userName))
          .filter(_.nInvites > 0)
          .filter(_.active)
        company <- query[Company] if company.id == record.companyId // join condition

      } yield InviteNamedRecord(company.id, company.name, record.nInvites)
    )

  override def getInvitePack(userName: String, companyId: Long): Task[Option[InviteRecord]]  =
    run(
      query[InviteRecord]
        .filter(_.companyId == lift(companyId))
        .filter(_.userName == lift(userName))
        .filter(_.active)
    ).map(_.headOption)

  override def addInvitePack(userName: String, companyId: Long, nInvites: Int): Task[Long] =
    run(
      query[InviteRecord]
        .insertValue(lift(InviteRecord(-1, userName, companyId, nInvites, false)))
        .returning(_.id)
    )

  override def activatePack(id: Long): Task[Boolean] =
    for {
      current <- run(query[InviteRecord].filter(_.id == lift(id)))
        .map(_.headOption)
        .someOrFail(new RuntimeException(s"Unable to activate pack $id"))
      result <- run(
        query[InviteRecord]
          .filter(_.id == lift(id))
          .updateValue(lift(current.copy(active = true)))
          .returning(_ => true)
      )
    } yield(result)

  override def markInvites(userName: String, companyId: Long, nInvites: Int): Task[Int] =
    for {
      currentRecord <- getInvitePack(userName, companyId)
        .someOrFail(new RuntimeException(s"user $userName cannot send invites for company $companyId"))
      nInvitesMarked <- ZIO.succeed(Math.min(nInvites, currentRecord.nInvites))
      _ <- run (
        query[InviteRecord]
          .filter(_.id == lift(currentRecord.id))
          .updateValue(lift(currentRecord.copy(nInvites = currentRecord.nInvites - nInvitesMarked)))
          .returning(r => r)

      )
    } yield nInvitesMarked

}

object InviteRepositoryLive {
  val layer = ZLayer {
    for {
      quill <- ZIO.service[Quill.Postgres[SnakeCase.type]]
    } yield new InviteRepositoryLive(quill)
  }

}
