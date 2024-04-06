package com.tsgcompany.reviewboard.services

import com.stripe.model.checkout.*
import com.stripe.Stripe as TheStripe
import com.stripe.net.Webhook
import com.stripe.param.checkout.SessionCreateParams
import com.tsgcompany.reviewboard.config.{Configs, StripeConfig}
import scala.jdk.OptionConverters.*
import zio.*

trait PaymentService {
  // create session
  def createCheckoutSession(invitePackId: Long, userName: String): Task[Option[Session]]
  // handle a webhook event
  def handleWebhookEvent[A](signature: String, payload:String, action: String => Task[A]): Task[Option[A]]


}

class PaymentServiceLive private (config: StripeConfig) extends PaymentService {

  override def createCheckoutSession(invitePackId: Long, userName: String): Task[Option[Session]] =
    ZIO.attempt{
      SessionCreateParams.builder()
        .setMode(SessionCreateParams.Mode.PAYMENT)
        .setSuccessUrl(config.successUrl)
        .setCancelUrl(config.cancelUrl)
        .setCustomerEmail(userName)
        .setClientReferenceId(invitePackId.toString) // my own payload - will be used on the webhook
        .setInvoiceCreation(
          SessionCreateParams.InvoiceCreation
            .builder()
            .setEnabled(true)
            .build()
        )
        .setPaymentIntentData(
          SessionCreateParams.PaymentIntentData
            .builder()
            .setReceiptEmail(userName)
            .build()
        )
      // need to add a product
      .addLineItem(
        SessionCreateParams.LineItem
          .builder()
          .setPrice(config.price) // unique id of your Stripe product
          .setQuantity(1L)
          .build()
      )
        .build()
    }
      .map(params => Session.create(params))
      .map(Option(_))
      .logError("Stripe session createin FAILED")
      .catchSome{
        case _ => ZIO.none
      }
  override def handleWebhookEvent[A](signature: String, payload:String, action: String => Task[A]): Task[Option[A]] =
    ZIO.attempt {
      /**
       * Build the webhook event
       * check the event type
       * if event type is successful
       *    parse the event
       *    then activate the pack
       */
      Webhook.constructEvent(payload, signature, config.secret)
    }
    .flatMap { event =>
        event.getType() match {
          case "checkout.session.completed" =>
            ZIO.foreach(
              event
                .getDataObjectDeserializer()
                .getObject()
                .toScala
                .map(_.asInstanceOf[Session])
                .map(_.getClientReferenceId())
            ) (action)
            //    parse the event
            //    then activate the pack
          case _ =>
            ZIO.none // discard the event
        }
      }
}



object  PaymentServiceLive {
  val layer = ZLayer {
    for {
      config <- ZIO.service[StripeConfig]
      _ <- ZIO.attempt(TheStripe.apiKey = config.key)
    } yield new PaymentServiceLive(config)
  }

  val configuredLayer =
    Configs.makeConfigLayer[StripeConfig]("tsgcompany.stripe") >>> layer

}
