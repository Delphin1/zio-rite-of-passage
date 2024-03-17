package com.tsgcompany.reviewboard.domain.data

import zio.json.JsonCodec

case class UserToken (
    id: Long,
    email: String,
    token: String,
    expires: Long
                     ) derives JsonCodec
