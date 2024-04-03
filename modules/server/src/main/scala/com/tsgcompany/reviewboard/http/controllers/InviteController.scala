package com.tsgcompany.reviewboard.http.controllers


import sttp.tapir.server.ServerEndpoint
import zio.*
import com.tsgcompany.reviewboard.domain.data.*
import com.tsgcompany.reviewboard.http.endpoints.*
import com.tsgcompany.reviewboard.services.{InviteService, JWTService}
import com.tsgcompany.reviewboard.http.requests.*
import com.tsgcompany.reviewboard.http.responses.*
import com.tsgcompany.reviewboard.repositories.{InviteRepository, InviteRepositoryLive, Repository}


class InviteController private (inviteService: InviteService, jwtService: JWTService) extends BaseController with InviteEndpoints {
  val addPack =
    addPackEndpoint
      .serverSecurityLogic[UserId, Task](token => jwtService.verifyToken(token).either)
      .serverLogic { token => req =>
        inviteService
          .addInvitePack(token.email, req.companyId)
          .map(_.toString)
          .either
      }

  val invite =
    inviteEndpoint
      .serverSecurityLogic[UserId, Task](token => jwtService.verifyToken(token).either)
      .serverLogic { token =>
        req =>
          inviteService
            .sendInvites(token.email, req.companyId, req.emails)
            .map{ nInvitesSent =>
              if (nInvitesSent == req.emails.size) InviteResponse("ok", nInvitesSent)
              else InviteResponse("partial success", nInvites = nInvitesSent)
            }
            .either
      }

  val getByUserId =
    getByUserIdEndpoint
      .serverSecurityLogic[UserId, Task](token => jwtService.verifyToken(token).either)
      .serverLogic { token =>
        _ =>
          inviteService.getByUserName(token.email).either
      }

  override val routes: List[ServerEndpoint[Any, Task]] = List(addPack, getByUserId, invite)
}

object InviteController {
  val makeZIO = for {
    inviteService <- ZIO.service[InviteService]
    jwtService <- ZIO.service[JWTService]
  } yield new InviteController(inviteService, jwtService)
}

object InviteRepositoryDemo extends ZIOAppDefault {
  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = {
    val program = for {
      repo <- ZIO.service[InviteRepository]
      records <- repo.getByUserName("tsg@tsgcompany.com")
      _ <- Console.printLine(s"Records: ${records}")
    } yield ()
    
    program.provide(
      InviteRepositoryLive.layer,
      Repository.dataLayer
    )
  }

}
