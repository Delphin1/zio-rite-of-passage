package com.tsgcompany.reviewboard.pages

import com.raquo.laminar.api.L.{*, given}
import com.tsgcompany.reviewboard.common.*
import com.tsgcompany.reviewboard.components.Anchors
import com.tsgcompany.reviewboard.core.Session
import zio.*
import org.scalajs.dom

object ProfilePage {
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
          child <-- Session.userState.signal.map {
            case None => renderInvalid()
            case Some(_) => renderContent()
          }
          )
      )
    )
  private def renderInvalid() =
    div(cls := "top-section",
      h1(span("Oops!")),
      div("It seems you are not logged in")
    )

  private def renderContent() =
    div(cls := "top-section",
      h1(span("Profile")),
      // change password section
      div(
        cls := "profile-section",
        h3(span("Account Settings")),
        Anchors.renderNavLink("Change Password", "/changepassword")
      )
      // actions section - send invite3s for every they have invites for
    )

}
