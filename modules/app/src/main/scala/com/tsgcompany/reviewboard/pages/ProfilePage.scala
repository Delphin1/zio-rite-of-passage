package com.tsgcompany.reviewboard.pages

import com.raquo.laminar.api.L.{*, given}
import com.raquo.laminar.nodes.ReactiveHtmlElement
import frontroute.*
import org.scalajs.dom
import org.scalajs.dom.html
import zio.ZIO
import com.tsgcompany.reviewboard.core.ZJS.*
import com.tsgcompany.reviewboard.core.*
import com.tsgcompany.reviewboard.http.requests.*


case class ChangePasswordState(
  password: String = "",
  newPassword: String = "",
  confirmPassword: String = "",
  override val showStatus: Boolean = false,
  upstreamStatus: Option[Either[String, String]] = None,

) extends FormState {

  override def errorList: List[Option[String]] = List(
    Option.when(password.isEmpty)("Password cannot be empty"),
    Option.when(newPassword.isEmpty)("New password cannot be empty"),
    Option.when(newPassword != confirmPassword)("Passwords must match")
  ) ++ upstreamStatus.map(_.left.toOption).toList
  override def maybeSuccess: Option[String] =
    upstreamStatus.flatMap(_.toOption)
}

object ProfilePage extends FormPage[ChangePasswordState]("Profile") {
  override val stateVar: Var[ChangePasswordState] = Var(ChangePasswordState())

  override def renderChildren(): List[ReactiveHtmlElement[html.Element]] =
    //if (Session.isActive)
    Session.getUserState.map(_.email).map(email =>
      List(
        renderInput("Password", "password-input", "password", true, "Your password", (s,p) => s.copy(password = p, showStatus = false, upstreamStatus = None)),
        renderInput("New Password", "new-password-input", "password", true, "New password", (s,p) => s.copy(newPassword = p, showStatus = false, upstreamStatus = None)),
        renderInput("Confirm New Password", "confirm-password-input", "password", true, "Confirm password", (s,p) => s.copy(confirmPassword = p, showStatus = false, upstreamStatus = None)),
        button(
          `type` := "button",
          "Change password",
          onClick.preventDefault.mapTo(stateVar.now()) --> submitter(email)
        )
      )
    )
      .getOrElse(
      List(
        div(
          cls := "centered-text",
          "Outch! It seems your are not login yet"
        )
      )
    )
  def submitter(email: String) = Observer[ChangePasswordState] { state =>
    if (state.hasErrors) {
      stateVar.update(_.copy(showStatus = true))
    }
    else {
      useBackend(_.user
        .updataPasswordEndpoint(
          UpdatePasswordRequest(email, state.password, state.newPassword)
        )
      )
        .map { userResponse =>
          stateVar.update(_.copy(showStatus = true, upstreamStatus = Some(Right("Password successfully changed"))))
        }
        .tapError { e =>
          ZIO.succeed {
            stateVar.update(_.copy(showStatus = true, upstreamStatus = Some(Left(e.getMessage))))
          }
        }
        .runJs
      //dom.console.log(s"Current state is: $state")
    }
  }
}
