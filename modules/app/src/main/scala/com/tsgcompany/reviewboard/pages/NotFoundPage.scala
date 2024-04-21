package com.tsgcompany.reviewboard.pages

import com.raquo.laminar.api.L.{*, given}
import frontroute.*
import org.scalajs.dom

object NotFoundPage {
  def apply() =
    div(
      cls := "simple-titled-page",
      h1("Oops!"),
      h2("This page does not exist"),
      div("You lost, friend!")
    )

}
