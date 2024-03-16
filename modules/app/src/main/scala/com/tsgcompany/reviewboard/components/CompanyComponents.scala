package com.tsgcompany.reviewboard.components

import com.raquo.laminar.api.L.{*, given}
import com.tsgcompany.reviewboard.common.*
import com.tsgcompany.reviewboard.domain.data.*
import com.tsgcompany.reviewboard.core.ZJS.*

object CompanyComponents {
  def renderCompanyPicture(company: Company) =
    img(
      cls := "img-fluid",
      src := company.image.getOrElse(Constants.companyLogoPlaceholder),
      alt := company.name
    )

  def renderDetail(icon: String, value: String) =
    div(
      cls := "company-detail",
      i(cls := s"fa fa-$icon company-detail-icon"),
      p(
        cls := "company-detail-value",
        value
      )
    )

  private def fullLocationString(company: Company): String =
    (company.location, company.country) match {
      case (Some(l), Some(c)) => s"$l, $c"
      case (Some(l), None) => l
      case (None, Some(c)) => c
      case (None, None) => "N/A"
    }

  def renderOverview(company: Company) =
    div(
      cls := "company-summary",
      renderDetail("location-dot", fullLocationString(company)),
      renderDetail("tags", company.tags.mkString(", "))
    )

}
