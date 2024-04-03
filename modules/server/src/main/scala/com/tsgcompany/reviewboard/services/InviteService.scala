package com.tsgcompany.reviewboard.services


import com.tsgcompany.reviewboard.config.{Configs, InvitePackConfig}
import zio.*
import com.tsgcompany.reviewboard.domain.data.*
import com.tsgcompany.reviewboard.repositories.{CompanyRepository, InviteRepository}
trait InviteService {
  def getByUserName(userName: String): Task[List[InviteNamedRecord]]
  def sendInvites(userName: String, companyId: Long, receivers: List[String]): Task[Int]
  def addInvitePack(userName: String, companyId: Long): Task[Long]
}

class InviteServiceLive private (
    inviteRepo: InviteRepository,
    companyRepo: CompanyRepository,
    emailService: EmailService,
    config: InvitePackConfig
                                ) extends InviteService {

  override def getByUserName(userName: String): Task[List[InviteNamedRecord]] =
    inviteRepo.getByUserName(userName)

  // invariant: only one pack per user per company
  override def addInvitePack(userName: String, companyId: Long): Task[Long] =
    for {
      company <- companyRepo
        .getById(companyId)
        .someOrFail(new RuntimeException(s"Cannot invite to review company $companyId doesn't exist"))
      currentPack <- inviteRepo.getInvitePack(userName, companyId)
      newPackId <- currentPack match {
        case None => inviteRepo.addInvitePack(userName, companyId, config.nInvites)
        case Some(_) => ZIO.fail(new RuntimeException("You already have an active pack for this company"))
      }
      // TODO Remove after implementing payment process
      _ <- inviteRepo.activatePack(newPackId)
    } yield newPackId


  override def sendInvites(userName: String, companyId: Long, receivers: List[String]): Task[RuntimeFlags] =
    for {
      company <- companyRepo
        .getById(companyId)
        .someOrFail(new RuntimeException(s"Cannot send invites: company $companyId doesn't exist"))
      nInvitesMarked <- inviteRepo.markInvites(userName, companyId, receivers.size)
      _ <- ZIO.collectAllPar(
        receivers.take(nInvitesMarked)
          .map(receiver => emailService.sendReviewInvite(userName, receiver, company))
      )
    } yield 0


}

object InviteServiceLive {
  val layer = ZLayer {
    for {
      inviteRepo <- ZIO.service[InviteRepository]
      companyRepo <- ZIO.service[CompanyRepository]
      emailService <- ZIO.service[EmailService]
      config <- ZIO.service[InvitePackConfig]
    } yield new InviteServiceLive(inviteRepo, companyRepo, emailService, config)
  }

  val configredLayer =
    Configs.makeConfigLayer[InvitePackConfig]("tsgcompany.invites") >>> layer



}


