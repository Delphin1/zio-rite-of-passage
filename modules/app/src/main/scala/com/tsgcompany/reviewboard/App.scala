package com.tsgcompany.reviewboard

import com.raquo.laminar.api.L.{*, given}
import com.tsgcompany.reviewboard.components.*
import com.tsgcompany.reviewboard.core.*
import frontroute.LinkHandler
import org.scalajs.dom

object App {

  val app = div(
    onMountCallback(_ => Session.loadUserState()),
    Header(),
    Router(),
    Footer()
  ).amend(LinkHandler.bind) // for internal links
  def main(args: Array[String]): Unit = {
    val containerNode = dom.document.querySelector("#app")
    render(
      containerNode,
      app
    )
  }

}