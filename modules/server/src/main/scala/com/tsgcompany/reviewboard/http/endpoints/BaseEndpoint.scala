package com.tsgcompany.reviewboard.http.endpoints

import com.tsgcompany.reviewboard.domain.errors.HttpError
import sttp.tapir.*

trait BaseEndpoint {
  val baseEndpoint = endpoint
    .errorOut(statusCode and plainBody[String]) // (StatusCode, String)
    //    .mapErrorOut(/* (StatusCode, Sting) => MyHttpError */)(/* MyHttpError => (StatusCode, String) */)
    .mapErrorOut[Throwable](HttpError.decode)(HttpError.encode)

  val secureBaseEndpoint =
    baseEndpoint
      .securityIn(auth.bearer[String]())

}
