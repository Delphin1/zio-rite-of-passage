package com.tsgcompany.reviewboard.http.requests

import zio.json.JsonCodec

final case class InvitePackRequest (companyId: Long) derives JsonCodec
