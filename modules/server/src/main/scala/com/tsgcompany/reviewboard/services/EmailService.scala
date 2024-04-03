package com.tsgcompany.reviewboard.services

import com.tsgcompany.reviewboard.config.{Configs, EmailServiceConfig}
import com.tsgcompany.reviewboard.domain.data.Company
import zio.*

import java.util.Properties
import javax.mail.internet.MimeMessage
import javax.mail.{Authenticator, Message, PasswordAuthentication, Session, Transport}
trait EmailService {
  def sendEmail(to: String, subject: String, content: String): Task[Unit]
  def sendPasswordRecovery(to: String, token: String): Task[Unit]
  def sendReviewInvite(from: String, to: String, company: Company): Task[Unit]
}

class EmailServiceLive private (config: EmailServiceConfig) extends EmailService {
  private val host: String = config.host
  private val port: Int = config.port
  private val user: String = config.user
  private val pass: String = config.pass

  override def sendEmail(to: String, subject: String, content: String): Task[Unit] =
    val messageZIO = for {
      props <- propsResource
      session <- createSession(props)
      message <- createMessage(session)("delphin1@mail.ru", to, subject, content)

    } yield message
    messageZIO.map(message => Transport.send(message))
  override def sendPasswordRecovery(to: String, token: String): Task[Unit] = {
    val subject = "This is password recovery"
    val content =
      s"""
         |<div style="
         |border: 1px solid black;
         |padding: 20px;
         |font-family: sans-serif;
         |line-height: 2;
         |font-size: 20px;
         |">
         |<h1>TSG Company password recovery</h1>
         |<p>Your password recovery token is:<strong>$token</strong></p>
         |</div>
         |""".stripMargin
    sendEmail(to, subject, content)
  }

  private val propsResource: Task[Properties] = {
    val props = new Properties
    props.put("mail.smtp.auth", true)
    props.put("mail.smtp.starttls.enable", "true")
    props.put("mail.smtp.host", host)
    props.put("mail.smtp.port", port)
    props.put("mail.smtp.ssl.trust", host)
    ZIO.succeed(props)
  }

  private def createSession(prop: Properties): Task[Session] =
    ZIO.attempt {
      Session.getInstance(prop, new Authenticator {
        override def getPasswordAuthentication: PasswordAuthentication =
          new PasswordAuthentication(user, pass)
      })
    }

  private def createMessage(session: Session)(from: String, to: String, subject: String, content: String): Task[MimeMessage] = {
    val message = new MimeMessage(session)
    message.setFrom(from)
    message.setRecipients(Message.RecipientType.TO, to)
    message.setSubject(subject)
    message.setContent(content, "text/html; charseet=UTF-8")
    ZIO.succeed(message)
  }
  
  override def sendReviewInvite(from: String, to: String, company: Company): Task[Unit] = {
    val subject = s"Invitation to Review ${company.name}"
    val content: String =
      s"""
        |<div style="
        |border: 1px solid black;
        |padding: 20px;
        |font-family: sans-serif;
        |line-height: 2;
        |font-size: 20px;
        |">
        |<h1>You are invited to review ${company.name}</h1>
        |<p>
        |Go to
        |<a href="http://localhost:1234/company/${company.id}">this link</a>
        |to add your thoughts on the app. Should take just a minute.
        |</p>
        |</div>
        |""".stripMargin
    sendEmail(to, subject, content)
  }


}

object EmailServiceLive {
  val layer = //ZLayer.succeed(new EmailServiceLive)
    ZLayer {
      ZIO.service[EmailServiceConfig].map(config => new EmailServiceLive(config))
    }
  val configuredLayer = Configs.makeConfigLayer[EmailServiceConfig]("tsgcompany.email") >>> layer
}

object EmailServiceDemo extends ZIOAppDefault {
  val program = for {
    emailService <- ZIO.service[EmailService]
//    _ <- emailService.sendEmail("spiderman@tsgcompany.com", "Hi from ZIO", "This is a email test")
    _ <- emailService.sendPasswordRecovery("spiderman@tsgcompany.com", "ABCDF12")
    _ <- Console.printLine("Email sent")
  } yield ()

  override def run = program.provide(EmailServiceLive.configuredLayer)
}
