package com.tsgcompany.reviewboard.pages

import com.raquo.laminar.api.L.{*, given}
import com.raquo.laminar.nodes.ReactiveHtmlElement
import com.tsgcompany.reviewboard.common.*
import com.tsgcompany.reviewboard.components.Anchors
import com.tsgcompany.reviewboard.pages.SignupPage.{renderInput, stateVar}
import org.scalajs.dom.html.Element
import org.scalajs.dom
import zio.*
import com.tsgcompany.reviewboard.core.ZJS.*
import com.tsgcompany.reviewboard.http.requests.ForgotPasswordRequest
case class ForgotPasswordState(
    email: String ="",
    upstreamStatus: Option[Either[String, String]] = None,
    override val showStatus: Boolean = false
                              ) extends FormState {
  override def errorList: List[Option[String]] =
    List(
      Option.when(!email.matches(Constants.emailRegex))("Email is invalid")
    ) ++ upstreamStatus.map(_.left.toOption).toList

  override def maybeSuccess: Option[String] = upstreamStatus.flatMap(_.toOption)

}

object ForgotPasswordPage extends FormPage[ForgotPasswordState]("Forgot Password"){

  //override val stateVar: Var[ForgotPasswordState] = Var(ForgotPasswordState())
  override def basicState = ForgotPasswordState()

  override def renderChildren(): List[ReactiveHtmlElement[Element]] = List(
    renderInput("Email", "email-input", "text", true, "Your email", (s,e) => s.copy(email = e, showStatus = false, upstreamStatus = None)),
    button(
      `type` := "button",
      "Recover Passoword",
      onClick.preventDefault.mapTo(stateVar.now()) --> submitter
    ),
    Anchors.renderNavLink("Have a password recovery token?", "/recover", "auth-link")
  )

  val submitter = Observer[ForgotPasswordState] { state =>
    if (state.hasErrors) {
      stateVar.update(_.copy(showStatus = true))
    }
    else {
      useBackend(_.user.forgotPasswordEndpoint(ForgotPasswordRequest(state.email)))
        .map { userResponse =>
          stateVar.update(_.copy(showStatus = true, upstreamStatus = Some(Right("Check you email!"))))
        }
        .tapError { e =>
          ZIO.succeed {
            stateVar.update(_.copy(showStatus = true, upstreamStatus = Some(Left(e.getMessage))))
          }
        }
        .runJs
    }
  }


}
