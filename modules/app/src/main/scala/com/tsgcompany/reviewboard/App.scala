package com.tsgcompany.reviewboard

import com.raquo.laminar.api.L.{*, given}
import com.tsgcompany.reviewboard.components.*
import frontroute.LinkHandler
import org.scalajs.dom

object App {

  val app = div(
    Header(),
    Router()
  ).amend(LinkHandler.bind) // for internal links
  def main(args: Array[String]): Unit = {
    val containerNode = dom.document.querySelector("#app")
    render(
      containerNode,
      app
    )
  }

}