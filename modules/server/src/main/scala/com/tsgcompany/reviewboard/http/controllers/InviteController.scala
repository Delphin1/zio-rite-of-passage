package com.tsgcompany.reviewboard.http.controllers


import sttp.tapir.server.ServerEndpoint
import zio.*
import com.tsgcompany.reviewboard.domain.data.*
import com.tsgcompany.reviewboard.http.endpoints.*
import com.tsgcompany.reviewboard.services.{InviteService, JWTService, PaymentService}
import com.tsgcompany.reviewboard.http.requests.*
import com.tsgcompany.reviewboard.http.responses.*
import com.tsgcompany.reviewboard.repositories.{InviteRepository, InviteRepositoryLive, Repository}


class InviteController private (inviteService: InviteService, jwtService: JWTService, paymentService: PaymentService) extends BaseController with InviteEndpoints {
  val addPack: ServerEndpoint[Any, Task] =
    addPackEndpoint
      .serverSecurityLogic[UserId, Task](token => jwtService.verifyToken(token).either)
      .serverLogic { token => req =>
        inviteService
          .addInvitePack(token.email, req.companyId)
          .map(_.toString)
          .either
      }

  val invite: ServerEndpoint[Any, Task] =
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

  val getByUserId: ServerEndpoint[Any, Task] =
    getByUserIdEndpoint
      .serverSecurityLogic[UserId, Task](token => jwtService.verifyToken(token).either)
      .serverLogic { token =>
        _ =>
          inviteService.getByUserName(token.email).either
      }

  val addPackPromoted: ServerEndpoint[Any, Task] =
    addPackPromotedEndpoint
      .serverSecurityLogic[UserId, Task](token => jwtService.verifyToken(token).either)
      .serverLogic{ token => req =>
      inviteService
        .addInvitePack(token.email, req.companyId)
        .flatMap{ packId =>
          paymentService.createCheckoutSession(packId, token.email)
        } // Option[Session]
        .someOrFail(new RuntimeException("Can't create payment checkout session"))
        .map(_.getUrl()) // the checkout session RUL = the desired payload
        .either
      }

  val webhook: ServerEndpoint[Any, Task] =
    webhookEndpoint
      .serverLogic{ (signature, payload) =>
        paymentService.handleWebhookEvent(signature, payload, packId => inviteService.activatePack(packId.toLong)).unit.either
      }
  override val routes: List[ServerEndpoint[Any, Task]] = List(addPack, addPackPromoted, webhook, getByUserId, invite)
}

object InviteController {
  val makeZIO = for {
    inviteService <- ZIO.service[InviteService]
    jwtService <- ZIO.service[JWTService]
    paymentService <- ZIO.service[PaymentService]
  } yield new InviteController(inviteService, jwtService, paymentService)
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
