package com.tsgcompany.reviewboard.domain.data

case class UserToken (
    email: String,
    token: String,
    expires: Long
                     )
