package com.tsgcompany.reviewboard.http

import com.tsgcompany.reviewboard.http.controllers.{BaseController, CompanyController, HealthController, ReviewController}

object HttpApi {
  def gatherRoutes(controllers: List[BaseController]) =
    controllers.flatMap(_.routes)
  def makeController = for {
    health <- HealthController.makeZIO
    company <- CompanyController.makeZIO
    reviews <- ReviewController.makeZIO
  } yield List(health, company)
  
  val endpointsZIO = makeController.map(gatherRoutes)

}
