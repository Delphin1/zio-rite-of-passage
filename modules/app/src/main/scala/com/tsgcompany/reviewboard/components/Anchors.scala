package com.tsgcompany.reviewboard.components

import com.raquo.laminar.api.L.{*, given}
import com.raquo.laminar.codecs.StringAsIsCodec
import frontroute.*
import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.annotation.*

object Anchors {
    def renderNavLink(text: String, location: String, cssClass: String = "") =
      a(
        href := location,
        cls := cssClass,
        text
      )
}


