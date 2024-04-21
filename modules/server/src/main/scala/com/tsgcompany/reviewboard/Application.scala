package com.tsgcompany.reviewboard

import com.tsgcompany.reviewboard.http.HttpApi
import com.tsgcompany.reviewboard.repositories.*
import com.tsgcompany.reviewboard.repositories.Repository.dataLayer
import com.tsgcompany.reviewboard.services.*
import sttp.tapir.*
import sttp.tapir.server.interceptor.cors.CORSInterceptor
import sttp.tapir.server.ziohttp.*
import zio.*
import zio.http.Server
object Application extends ZIOAppDefault {

  val serverProgram = for {
    endpoints <- HttpApi.endpointsZIO
    _  <- Server.serve(
        ZioHttpInterpreter(
          ZioHttpServerOptions.default.appendInterceptor(
            CORSInterceptor.default
          )
        ).toHttp(endpoints)
    )
    _ <- Console.printLine("Server Started")
  } yield ()

  override def run  = serverProgram.provide(
    Server.default,
    // configs
    // services
    CompanyServiceLive.layer,
    ReviewServiceLive.configuredLayer,
    UserServiceLive.layer,
    JWTServiceLive.configuredLayer,
    EmailServiceLive.configuredLayer,
    InviteServiceLive.configredLayer,
    PaymentServiceLive.configuredLayer,
    OpenAIServiceLive.configuredLayer,
    //repos
    CompanyRepositoryLive.layer,
    ReviewRepositoryLive.layer,
    UserRepositoryLive.layer,
    RecoveryTokenRepositoryLive.configuredLayer,
    InviteRepositoryLive.layer,
    // other requirements
    dataLayer
    //CompanyService.dummyLayer
  ) // Console.printLine("Hello world!")

}

/**
 * - ReviewEndpoints - get/se (summaries)
 * - controller
 * - ReviewService
 *    - get - fetch it from the repo
 *    - set - invoke the OpenAI API + store in th repo
 * - repo
 *
 * Frontend
 * - modify the current "TODO" card
*/

/**
 * - tests work
 * - reviews are set int future (TZ diff)
 * - invalid pages
 * - footer
 * - some images won't upload in company pages
*/
