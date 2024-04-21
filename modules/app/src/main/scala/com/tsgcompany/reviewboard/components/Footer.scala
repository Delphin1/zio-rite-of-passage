package com.tsgcompany.reviewboard.components

import com.raquo.laminar.api.L.{*, given}
import com.raquo.laminar.codecs.StringAsIsCodec
import com.tsgcompany.reviewboard.core.*
import frontroute.*
import org.scalajs.dom

import scala.scalajs.js.Date

object Footer {
  def apply() =
    div(
     cls := "main-footer",
      div("-= Written in Scala with love =-"),
      div(s"© ${new Date().getFullYear()} all rights reserved ")
    )

}
