package com.tsgcompany.reviewboard.core

import sttp.capabilities.WebSockets
import sttp.capabilities.zio.ZioStreams
import sttp.client3.*
import sttp.client3.impl.zio.FetchZioBackend
import sttp.tapir.Endpoint
import sttp.tapir.client.sttp.SttpClientInterpreter
import zio.*

import com.tsgcompany.reviewboard.config.*
import com.tsgcompany.reviewboard.http.endpoints.*

case class RestrictedEndpointException(msg: String) extends RuntimeException(msg)

trait BackendClient {
  val company: CompanyEndpoints
  val user: UserEndpoints
  def endpointRequestZIO[I,E <: Throwable,O](endpoint: Endpoint[Unit, I, E, O, Any])(payload: I): Task[O]
  def secureEndpointRequestZIO[I,E <: Throwable,O](endpoint: Endpoint[String, I, E, O, Any])(payload: I): Task[O]

}

class BackendClientLive(
                         backend: SttpBackend[Task, ZioStreams & WebSockets],
                         interpreter: SttpClientInterpreter,
                         config: BackendClientConfig
                       ) extends BackendClient {
  override val company: CompanyEndpoints = new CompanyEndpoints {}
  private def endpointRequest[I,E,O](endpoint: Endpoint[Unit, I, E, O, Any]): I => Request[Either[E, O], Any] =
    interpreter
      .toRequestThrowDecodeFailures(endpoint, config.uri)

  private def secureEndpointRequest[S, I, E, O](endpoint: Endpoint[S, I, E, O, Any]): S => I => Request[Either[E, O], Any] =
    interpreter
      .toSecureRequestThrowDecodeFailures(endpoint, config.uri)

  private def tokenOrFail =
    ZIO
      .fromOption(Session.getUserState)
      .orElseFail(RestrictedEndpointException("You need to login"))
      .map(_.token)
    
  override val user: UserEndpoints = new UserEndpoints {}
  override def endpointRequestZIO[I,E <: Throwable,O](endpoint: Endpoint[Unit, I, E, O, Any])(payload: I): Task[O] =
    backend.send(endpointRequest(endpoint)(payload)).map(_.body).absolve

  override def secureEndpointRequestZIO[I, E <: Throwable, O](endpoint: Endpoint[String, I, E, O, Any])(payload: I): Task[O] =
    for {
      token <- tokenOrFail
      response <- backend.send(secureEndpointRequest(endpoint)(token)(payload)).map(_.body).absolve
    } yield response

}

object BackendClientLive {
  val layer = ZLayer {
    for {
      backend <- ZIO.service[SttpBackend[Task, ZioStreams & WebSockets]]
      interpreter <- ZIO.service[SttpClientInterpreter]
      config <- ZIO.service[BackendClientConfig]
    } yield new BackendClientLive(backend, interpreter, config)
  }

  val configuredLayer = {
    val backend = FetchZioBackend()
    val interpreter: SttpClientInterpreter = SttpClientInterpreter()
    val config = BackendClientConfig(Some(uri"http://localhost:8080"))
    ZLayer.succeed(backend) ++ 
      ZLayer.succeed(interpreter) ++ 
      ZLayer.succeed(config) >>> layer
  }
}  
  