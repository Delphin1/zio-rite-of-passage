package com.libertexgroup.reviewboard.http

import com.libertexgroup.reviewboard.http.controllers.{BaseController, CompanyController, HealthController}

object HttpApi {
  def gatherRoutes(controllers: List[BaseController]) =
    controllers.flatMap(_.routes)
  def makeController = for {
    health <- HealthController.makeZIO
    company <- CompanyController.makeZIO
  } yield (List(health, company))
  
  val endpointsZIO = makeController.map(gatherRoutes)

}
