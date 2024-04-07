package com.tsgcompany.reviewboard.components

import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom
import frontroute.*
import com.tsgcompany.reviewboard.pages.*
object Router {
  val externalUrlBus = EventBus[String]()
  def apply() =
    mainTag(
      onMountCallback(ctx => externalUrlBus.events.foreach(url => dom.window.location.href = url)(ctx.owner)),
      routes(
        div(
          cls := "container-fluid",
          // potential childredn
          (pathEnd |  path("companies")) { // localhost:1234 or localhost:1234/ or localhost:1234/companies
            CompaniesPage()
          },
          path("login") {
            LoginPage()
          },
          path("signup") {
            SignupPage()
          },
          path("changepassword") {
            ChangePasswordPage()
          },
          path("forgot") {
            ForgotPasswordPage()
          },
          path("recover") {
            RecoverPasswordPage()
          },
          path("logout") {
            LogoutPage()
          },
          path("profile") {
            ProfilePage()
          },
          path("post") {
            CreateCompanyPage()
          },
          path("company" / long) { companyId =>
            CompanyPage(companyId)
          },
          noneMatched {
            NotFoundPage()
          }
        )
      )
    )

}
