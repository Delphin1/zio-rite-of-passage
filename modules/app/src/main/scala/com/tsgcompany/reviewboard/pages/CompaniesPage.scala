package com.tsgcompany.reviewboard.pages

import com.raquo.laminar.api.L.{*, given}
import com.tsgcompany.reviewboard.common.*
import com.tsgcompany.reviewboard.components.*
import com.tsgcompany.reviewboard.domain.data.*
import com.tsgcompany.reviewboard.core.ZJS.*

object CompaniesPage {
//  val simpleCompany = Company(
//    1L,
//    "Simple Company",
//    "Simple-company",
//    "http://dummy.com",
//    Some("Anywhere"),
//    Some("On Mars"),
//    Some("space travel"),
//    None,
//    List("space", "scala")
//  )
  // components
  val filterPanel = new FilterPanel

  val firstBatch = EventBus[List[Company]]()
  val companyEvents: EventStream[List[Company]] =
    firstBatch.events.mergeWith {
    filterPanel.triggerFilters.flatMap { newFilter =>
      useBackend(_.company.searchEndpoint(newFilter)).toEventStream
    }
  }
//  def performBackendCall(): Unit = {
//      // fetch API
//      // AJAX
//      // ZIO endpoint
//      import com.tsgcompany.reviewboard.core.ZJS.*
//      val companiesZIO = useBackend(_.company.getAllEndpoint(()))
//      companiesZIO.emitTo(companiesBus)
//  }


  def apply() =
    sectionTag(
      onMountCallback(_ => useBackend(_.company.getAllEndpoint(())).emitTo(firstBatch)),
      cls := "section-1",
      div(
        cls := "container company-list-hero",
        h1(
          cls := "company-list-title",
          "Rock the JVM Companies Board"
        )
      ),
      div(
        cls := "container",
        div(
          cls := "row jvm-recent-companies-body",
          div(
            cls := "col-lg-4",
            //FilterPanel()
            filterPanel()
          ),
          div(
            cls := "col-lg-8",
            children <-- companyEvents.map(_.map(renderCompany))
          )
        )
      )
    )

  private def renderCompanyPicture(company: Company) =
    img(
      cls := "img-fluid",
      src := company.image.getOrElse(Constants.companyLogoPlaceholder),
      alt := company.name
    )

  private def renderDetail(icon: String, value: String) =
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
  private def  renderOverview(company: Company) =
    div(
      cls := "company-summary",
      renderDetail("location-dot", fullLocationString(company)),
      renderDetail("tags", company.tags.mkString(", "))
    )
  private def renderAction(company: Company) =
    div(
      cls := "jvm-recent-companies-card-btn-apply",
      a(
        href := company.url,
        target := "blank",
        button(
          `type` := "button",
          cls := "btn btn-danger rock-action-btn",
          "Website"
        )
      )
    )

  def renderCompany(company: Company) =
    div(
      cls := "jvm-recent-companies-cards",
      div(
        cls := "jvm-recent-companies-card-img",
        renderCompanyPicture(company)

      ),
      div(
        cls := "jvm-recent-companies-card-contents",
        h5(
          Anchors.renderNavLink(
            company.name,
            s"/company/${company.id}",
            "company-title-link"
          )
        ),
        renderOverview(company)
      ),
      renderAction(company)
    )
}