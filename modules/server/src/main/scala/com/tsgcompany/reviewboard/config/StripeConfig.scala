package com.tsgcompany.reviewboard.config

case class StripeConfig (
    key: String,
    secret: String, // webhook secret
    price: String,
    successUrl: String,
    cancelUrl: String
                        )
