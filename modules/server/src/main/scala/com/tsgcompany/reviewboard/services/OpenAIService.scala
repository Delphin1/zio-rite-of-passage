package com.tsgcompany.reviewboard.services

import zio.*
trait OpenAIService {
  def getCompletion(prompt: String): Task[Option[String]]

}

class OpenAIServiceLive private extends OpenAIService {

  override def getCompletion(prompt: String): Task[Option[String]] = ZIO.fail(new RuntimeException("Not implemented"))
}

object OpenAIServiceLive {
  val layer = ZLayer{
    ZIO.succeed(new OpenAIServiceLive)
  }

}
