package com.tsgcompany.reviewboard.components

import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom
import frontroute.*

import com.tsgcompany.reviewboard.pages.*
object Router {
  def apply() =
    mainTag(
      routes(
        div(
          cls := "container-fluid",
          // potential childredn
          (pathEnd |  path("companies")) { // localhost:1234 or localhost:1234/ or localhost:1234/companies
            CompaniesPage()
          },
          path("logout") {
            LogoutPage()
          },
          path("login") {
            LoginPage()
          },
          path("signup") {
            SignupPage()
          },
          path("profile") {
            ProfilePage()
          },
          noneMatched {
            NotFoundPage()
          }
        )
      )
    )

}
