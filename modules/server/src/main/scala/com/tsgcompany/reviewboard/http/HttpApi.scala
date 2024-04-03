package com.tsgcompany.reviewboard.http

import com.tsgcompany.reviewboard.http.controllers.*

object HttpApi {
  def gatherRoutes(controllers: List[BaseController]) =
    controllers.flatMap(_.routes)
  def makeController = for {
    health <- HealthController.makeZIO
    company <- CompanyController.makeZIO
    reviews <- ReviewController.makeZIO
    users <- UserController.makeZIO
    invites <- InviteController.makeZIO
  } yield List(health, company, reviews, users, invites)
  
  val endpointsZIO = makeController.map(gatherRoutes)

}
