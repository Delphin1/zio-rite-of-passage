package com.tsgcompany.reviewboard.domain.data

final case class User (
    id: Long,
    email: String,
    hashedPassword: String
                      )
