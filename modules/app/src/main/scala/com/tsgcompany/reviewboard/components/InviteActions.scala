package com.tsgcompany.reviewboard.components

import com.raquo.laminar.api.L.{p, *, given}
import com.tsgcompany.reviewboard.common.Constants
import zio.*
import org.scalajs.dom
import com.tsgcompany.reviewboard.core.ZJS.*
import com.tsgcompany.reviewboard.domain.data.InviteNamedRecord
import com.tsgcompany.reviewboard.http.requests.InviteRequest
object InviteActions {

  val inviteListBus = EventBus[List[InviteNamedRecord]]()
  def refreshInvitesList() =
    useBackend(_.invite.getByUserIdEndpoint(()))
  def apply() =
    div(
      onMountCallback(_ => refreshInvitesList().emitTo(inviteListBus)),
      cls := "profile-section",
      h3(span("Invites")),
      children <-- inviteListBus.events.map(_.sortBy(_.companyName).map(renderInviteSection))
    )

  def renderInviteSection(record: InviteNamedRecord) = {
    val emailListVar = Var[Array[String]](Array())
    val maybeErrorVar = Var[Option[String]](None)
    val inviteSubmitter = Observer[Unit] { _ =>
      // invite the emails
      // refresh invite list
      val emailList = emailListVar.now().toList
      if (emailList.exists(!_.matches(Constants.emailRegex)))
        maybeErrorVar.set(Some("At least an email is invalid"))
      else {
        val refreshProgram = for {
          _ <- useBackend(_.invite.inviteEndpoint(InviteRequest(record.companyId, emailList)))
          invitesLeft <- refreshInvitesList()
        } yield invitesLeft
        maybeErrorVar.set(None)
        refreshProgram.emitTo(inviteListBus)
      }
    }
    div(
      // name of the company
      cls := "invite-section",
      h5(span(record.companyName)),
      // n of invites left
      p(s"${record.nInvites} invites left"),
      // text area - email addresses one per line
      textArea(
        cls         := "invites-area",
        placeholder := "Enter emails, one per line",
        onInput.mapToValue.map(_.split("\n").map(_.trim).filter(_.nonEmpty)) --> emailListVar.writer
      ),
      // button to send invites
      button(
        `type` := "button",
        cls    := "btn btn-primary",
        "Invite",
        onClick.mapToUnit --> inviteSubmitter
      ),
      child.maybe <-- maybeErrorVar.signal.map(maybeRenderError)
    )
  }

  private def maybeRenderError(maybeError: Option[String]) = maybeError.map { message =>
    div(
      cls := "page-status-error",
      message
    )
  }

}
