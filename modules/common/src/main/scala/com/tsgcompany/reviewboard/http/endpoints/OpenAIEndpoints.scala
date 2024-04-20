package com.tsgcompany.reviewboard.http.endpoints

import zio.*
import sttp.tapir.*
import sttp.client3.*
import sttp.tapir.json.zio.*
import sttp.tapir.generic.auto.*

import com.tsgcompany.reviewboard.domain.errors.HttpError
import com.tsgcompany.reviewboard.http.requests.*
import com.tsgcompany.reviewboard.http.responses.*

trait OpenAIEndpoints extends BaseEndpoint {
  val completionEndpoint = endpoint
    .errorOut(statusCode and plainBody[String])
    .mapErrorOut[Throwable](HttpError.decode)(HttpError.encode)
    .securityIn(auth.bearer[String]())
    .in("v1" / "chat" / "completions")
    .post
    .in(jsonBody[CompletionRequest])
    .out(jsonBody[CompletionResponse])

}
