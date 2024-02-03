package com.tsgcompany.reviewboard.components

import com.raquo.laminar.api.L.{*, given}
import com.raquo.laminar.codecs.StringAsIsCodec
import frontroute.*
import org.scalajs.dom

import com.tsgcompany.reviewboard.common.*

object Header {
  def apply() =
      div(
        cls := "container-fluid p-0",
        div(
          cls := "jvm-nav",
          div(
            cls := "container",
            navTag(
              cls := "navbar navbar-expand-lg navbar-light JVM-nav",
              div(
                cls := "container",
                renderLogo(),
                button(
                  cls := "navbar-toggler",
                  `type` := "button",
                  htmlAttr("data-bs-toggle", StringAsIsCodec) := "collapse",
                  htmlAttr("data-bs-target", StringAsIsCodec) := "#navbarNav",
                  htmlAttr("aria-controls", StringAsIsCodec) := "navbarNav",
                  htmlAttr("aria-expanded", StringAsIsCodec) := "false",
                  htmlAttr("aria-label", StringAsIsCodec) := "Toggle navigation",
                  span(cls := "navbar-toggler-icon")
                ),
                div(
                  cls := "collapse navbar-collapse",
                  idAttr := "navbarNav",
                  ul(
                    cls := "navbar-nav ms-auto menu align-center expanded text-center SMN_effect-3",
                   renderNavLinks()
                  )
                )
              )
            )
          )
        )
      )

  private def renderLogo() =
    a(
      href := "/",
      cls := "navbar-brand",
      img(
        cls := "home-logo",
        src := Constants.logoImage,
        alt := "Rock the JVM"
      )


    )
  private def renderNavLinks() = List(
    renderNavLink("Companies", "/companies"),
    renderNavLink("Log In", "/login"),
    renderNavLink("Sign up", "/signup")
  )
  private def renderNavLink(text: String, location: String) =
    li(
      cls := "nav-item",
      Anchors.renderNavLink(text, location, "nav-link jvm-item")
    )

}

