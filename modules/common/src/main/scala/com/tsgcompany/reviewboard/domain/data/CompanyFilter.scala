package com.tsgcompany.reviewboard.domain.data

import zio.json.JsonCodec
case class CompanyFilter (
  locations: List[String] = List(),
  countries: List[String] = List(),
  industries: List[String] = List(),
  tags: List[String] = List()
) derives JsonCodec

object CompanyFilter {
  val empty = CompanyFilter()
}
