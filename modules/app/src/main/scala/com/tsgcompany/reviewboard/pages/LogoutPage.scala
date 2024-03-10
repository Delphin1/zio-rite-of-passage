package com.tsgcompany.reviewboard.pages

import com.raquo.laminar.api.L.{*, given}
import com.raquo.laminar.nodes.ReactiveHtmlElement
import com.tsgcompany.reviewboard.core.*
import org.scalajs.dom
import frontroute.*
import zio.*

case class LogoutPageState() extends FormState {
  override def errorList: List[Option[String]] = List()

  override def maybeSuccess: Option[String] = None

  override def showStatus: Boolean = false


}
object LogoutPage extends FormPage[LogoutPageState]("Log Out") {
  //override val stateVar: Var[LogoutPageState] = Var(LogoutPageState())
  override def basicState = LogoutPageState()

  def renderChildren(): List[ReactiveHtmlElement[dom.html.Element]] = List(
    div(
      onMountCallback(_ => Session.clearUserState()),
      cls := "centered-text",
      "You've just been successfully logged out"
    )
  )


}
