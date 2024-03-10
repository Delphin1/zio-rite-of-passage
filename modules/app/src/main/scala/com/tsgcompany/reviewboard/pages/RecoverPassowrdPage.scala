package com.tsgcompany.reviewboard.pages

import com.raquo.laminar.api.L.{*, given}
import com.raquo.laminar.nodes.ReactiveHtmlElement
import com.tsgcompany.reviewboard.common.Constants
import com.tsgcompany.reviewboard.components.Anchors
import com.tsgcompany.reviewboard.core.ZJS.*
import com.tsgcompany.reviewboard.http.requests.RecoverPasswordRequest
import com.tsgcompany.reviewboard.pages.SignupPage.{renderInput, stateVar}
import org.scalajs.dom.html.Element
import org.scalajs.dom
import zio.*
case class RecoverPasswordState(
                                 email: String = "",
                                 token: String = "",
                                 newPassword: String = "",
                                 confirmPassword: String = "",
                                 upstreamStatus: Option[Either[String, String]] = None,
                                 override val showStatus: Boolean = false
                              ) extends FormState {
  override def errorList: List[Option[String]] = List(
    Option.when(!email.matches(Constants.emailRegex))("Email is invalid"),
    Option.when(token.isEmpty)("Token cannot be empty"),
    Option.when(newPassword.isEmpty)("New password cannot be empty"),
    Option.when(newPassword != confirmPassword)("Passwords must match")
  ) ++ upstreamStatus.map(_.left.toOption).toList
  override def maybeSuccess: Option[String] = upstreamStatus.flatMap(_.toOption)
}

object RecoverPasswordPage extends FormPage[RecoverPasswordState]("Reset Password") {

  //override val stateVar: Var[RecoverPasswordState] = Var(RecoverPasswordState())
  override def basicState = RecoverPasswordState()

  val submitter = Observer[RecoverPasswordState] { state =>
    if (state.hasErrors) {
      stateVar.update(_.copy(showStatus = true))
    }
    else {
      useBackend(_.user.recoverPasswordEndpoint(RecoverPasswordRequest(state.email, state.token, state.newPassword)))
        .map { _ =>
          stateVar.update(_.copy(showStatus = true, upstreamStatus = Some(Right("Success! You can log in now"))))
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
  override def renderChildren(): List[ReactiveHtmlElement[Element]] = List(
    renderInput("Email", "email-input", "text", true, "Your email", (s,e) => s.copy(email = e, showStatus = false, upstreamStatus = None)),
    renderInput("Recovery token from email", "token-input", "password", true, "The token", (s,t) => s.copy(token = t, showStatus = false, upstreamStatus = None)),
    renderInput("New Password", "password-input", "password", true, "Your new password", (s,p) => s.copy(newPassword = p, showStatus = false, upstreamStatus = None)),
    renderInput("Confirm Password", "confirm-password-input", "password", true, "Confirm password", (s,p) => s.copy(confirmPassword = p, showStatus = false, upstreamStatus = None)),
    button(
      `type` := "button",
      "Reset Password",
      onClick.preventDefault.mapTo(stateVar.now()) --> submitter
    ),
    Anchors.renderNavLink("Need a password recovery token?", "/forgot", "auth-link")
  )
}
