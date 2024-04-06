package com.tsgcompany.reviewboard.config

case class StripeConfig (
    key: String,
    price: String,
    successUrl: String,
    cancelUrl: String
                        )
