package com.libertexgroup.reviewboard.domain.data

import java.time.Instant
import zio.json.{DeriveJsonCodec, JsonCodec}

case class Review (
    id: Long,   // PK
    companyId: Long,
    userId: Long, // FK
    management: Int, //1-5
    culture: Int,
    salary: Int, //
    benefits: Int,
    wouldRecommend: Int,
    review: String,
    created: Instant,
    updated: Instant
                  )


object Review{
  given codec: JsonCodec[Review] = DeriveJsonCodec.gen[Review]
}


