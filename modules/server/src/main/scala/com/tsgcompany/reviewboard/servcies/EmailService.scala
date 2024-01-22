package com.tsgcompany.reviewboard.servcies

import zio.*
trait EmailService {
  def sendEmail(to: String, subject: String, content: String): Task[Unit]
  def sendPasswordRecovery(to: String, token: String): Task[Unit]
}

class EmailServiceLive private extends EmailService {

  override def sendEmail(to: String, subject: String, content: String): Task[Unit] = ZIO.fail(new RuntimeException("not implemented ... yet"))
  override def sendPasswordRecovery(to: String, token: String): Task[Unit] = ZIO.fail(new RuntimeException("not implemented ... yet"))
}

object EmailServiceLive {
  val layer = ZLayer.succeed(new EmailServiceLive)
}
