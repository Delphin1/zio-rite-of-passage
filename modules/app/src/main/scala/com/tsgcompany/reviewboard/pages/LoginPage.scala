package com.tsgcompany.reviewboard.pages

import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom
import zio.*
import com.tsgcompany.reviewboard.common.*
import com.tsgcompany.reviewboard.components.Anchors
import com.tsgcompany.reviewboard.core.ZJS.*
import com.tsgcompany.reviewboard.core.*
import com.tsgcompany.reviewboard.http.requests.LoginRequest
import frontroute.BrowserNavigation

case class LoginFormState(email: String = "", 
                          password: String = "", 
                          upstreamError: Option[String] = None,
                          override val showStatus: Boolean = false) extends FormState {
  private val userEmailError: Option[String] =
    Option.when(!email.matches(Constants.emailRegex))("User email is invalid")
  private val passwordError: Option[String] =
    Option.when(password.isEmpty)("Password can't be empty")
  
  override val errorList = List(userEmailError, passwordError, upstreamError)
  override val maybeSuccess: Option[String] = None

}
object LoginPage extends FormPage[LoginFormState]("Log In") {

  //override val stateVar = Var(LoginFormState())
  override def basicState = LoginFormState()

  val submitter = Observer[LoginFormState] { state =>
    if (state.hasErrors) {
      stateVar.update(_.copy(showStatus = true))
    }
    else {
      useBackend(_.user.loginEndpoint(LoginRequest(state.email, state.password)))
        .map { userToken =>
            Session.setUserState(userToken)
            stateVar.set(LoginFormState())
            BrowserNavigation.replaceState("/")
        }
        .tapError { e =>
          ZIO.succeed {
            stateVar.update(_.copy(showStatus = true, upstreamError = Some(e.getMessage)))
          }
        }
        .runJs
      //dom.console.log(s"Current state is: $state")
    }
  }

  def renderChildren() = List(
    // an input of type text
    renderInput("Email", "email-input", "text", true, "Your email", (s,e) => s.copy(email = e, showStatus = false, upstreamError = None)),
    // an input of type password
    renderInput("Password", "password-input", "password", true, "Your password", (s,p) => s.copy(password = p, showStatus = false, upstreamError = None)),
    button(
      `type` := "button",
      "Log In",
      onClick.preventDefault.mapTo(stateVar.now()) --> submitter
    ),
    Anchors.renderNavLink("Forgot Password?", "/forgot", "auth-link")
  )

}
