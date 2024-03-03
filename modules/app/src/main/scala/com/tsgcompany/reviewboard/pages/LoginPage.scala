package com.tsgcompany.reviewboard.pages

import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom
import frontroute.*
import zio.*

import com.tsgcompany.reviewboard.common.*
import com.tsgcompany.reviewboard.core.ZJS.*
import com.tsgcompany.reviewboard.core.*
import com.tsgcompany.reviewboard.http.requests.LoginRequest

object LoginPage {
  case class State(email: String ="", password: String ="", upstreamError: Option[String] = None, showStatus: Boolean = false) {
    val userError: Option[String] =
      Option.when(email.isEmpty)("User can't be empty")
    val userEmailError: Option[String] =
      Option.when(!email.matches(Constants.emailRegex))("User email is invalid")
    val passwordError: Option[String] =
      Option.when(password.isEmpty)("Password can't be empty")
    val errorList = List(userEmailError, passwordError, upstreamError)
    val maybeErrors = errorList.find(_.isDefined).flatten.filter(_ => showStatus)
    val hasErrors = errorList.exists(_.isDefined)


  }
  val stateVar = Var(State())

  val submitter = Observer[State] { state =>
    if (state.hasErrors) {
      stateVar.update(_.copy(showStatus = true))
    }
    else {
      useBackend(_.user.loginEndpoint(LoginRequest(state.email, state.password)))
        .map { userToken =>
            Session.setUserState(userToken)
            stateVar.set(State())
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
  def apply() =
      div(
        cls := "row",
        div(
          cls := "col-md-5 p-0",
          div(
            cls := "logo",
            img(
              cls := "home-logo",
              src := Constants.logoImage,
              alt := "Rock the JVM"
            )
          )
        ),
        div(
          cls := "col-md-7",
          // right
          div(
            cls := "form-section",
            div(cls := "top-section", h1(span("Log In"))),
            children <-- stateVar.signal.map(_.maybeErrors)
              .map(_.map(renderError))
              .map(_.toList),
            maybeRenderSuccess(),
            form(
              nameAttr := "signin",
              cls      := "form",
              idAttr   := "form",
              // an input of type text
              renderInput("Email", "email-input", "text", true, "Your email", (s,e) => s.copy(email = e, showStatus = false, upstreamError = None)),
              // an input of type password
              renderInput("Password", "password-input", "password", true, "Your password", (s,p) => s.copy(password = p, showStatus = false, upstreamError = None)),
              button(
                `type` := "button",
                "Log In",
                onClick.preventDefault.mapTo(stateVar.now()) --> submitter
              )
            )
          )
        )
      )
  def maybeRenderSuccess(shouldShow: Boolean = false) =
    if (shouldShow)
      div(
                cls := "page-status-success",
                child.text <-- stateVar.signal.map(_.toString)
      )
    else
      div ()
  def renderError(error: String) =
    div(
      cls := "page-status-error",
      error
    )
  def renderInput(name:String, uid: String, kind: String, isRequired: Boolean, plcHolder: String, updateFn:(State, String) => State) =
    div(
      cls := "row",
      div(
        cls := "col-md-12",
        div(
          cls := "form-input",
          label(
            forId := uid,
            cls := "form-label",
            if (isRequired) span("*") else span(),
            name
          ),
          input(
            `type` := kind,
            cls := "form-control",
            idAttr := uid,
            placeholder := plcHolder,
            onInput.mapToValue --> stateVar.updater(updateFn)
          )
        )
      )
    )
}
