package com.tsgcompany.reviewboard.http.endpoints

import sttp.tapir.*
import sttp.tapir.json.zio.*
import sttp.tapir.generic.auto.*
import com.tsgcompany.reviewboard.http.requests.*
import com.tsgcompany.reviewboard.http.responses.*
import com.tsgcompany.reviewboard.domain.data.*

trait InviteEndpoints extends BaseEndpoint {
  /**
   * POST /invite/add
   * input { companyId } - 200 email to invite people to leave reviews
   * output packId as string
   */

  val addPackEndpoint =
    secureBaseEndpoint
      .tag("Invites")
      .name("add invites")
      .description("Get invite tokens")
      .in ("invite" / "add")
      .post
      .in(jsonBody[InvitePackRequest])
      .out(stringBody)
  /**
   * POST /invite
   * input { [emails], companyId }
   * output { nInvites, status }
   *
   * will send emails to users
   */

  val inviteEndpoint =
    secureBaseEndpoint
      .tag("Invites")
      .name("invite")
      .description("Send people emails inviting them to leave a review")
      .in("invite")
      .post
      .in(jsonBody[InviteRequest])
      .out(jsonBody[InviteResponse])

  /**
   * GET /invite/all
   * output [ {companyId, companyName, nInvites } ]
   */

  val getByUserIdEndpoint =
    secureBaseEndpoint
      .tag("Invites")
      .name("get by user id")
      .description("Get all active invite packs for a user")
      .get
      .in("invite" / "all")
      .out(jsonBody[List[InviteNamedRecord]])

  // TODO - paid endpoints
  val addPackPromotedEndpoint =
    secureBaseEndpoint
      .tag("Invites")
      .name("add invites (promoted)")
      .description("Get invite tokens (paid via Stripe)")
      .in("invite" / "promoted")
      .post
      .in(jsonBody[InvitePackRequest])
      .out(stringBody) // this is the Stripe checkout URL

  // webhook - will be called automatically by Stripe
  val webhookEndpoint =
    baseEndpoint
      .tag("Invites")
      .name("invite webhook")
      .description("Confirm the purchase of an invite pack")
      .in("invite" / "webhook")
      .post
      .in(header[String]("Stripe-signature"))
      .in(stringBody)


  /**
   * hit /invite/promoted -> Stipe checkout URL
   * go to the URL, fill in the details, hit Pay
   * after a while, Stripe will call the webhook -> activate the pack
   */

}
