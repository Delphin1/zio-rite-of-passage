package com.tsgcompany.reviewboard.components

import com.raquo.laminar.api.L.{*, given}
import com.raquo.laminar.codecs.StringAsIsCodec
import com.tsgcompany.reviewboard.core.ZJS.*
import com.tsgcompany.reviewboard.domain.data.CompanyFilter
import sttp.client3.*


class FilterPanel {
  case class CheckValueEvents(groupName: String, value: String, checked: Boolean)

  private val GROUP_LOCATION = "Location"
  private val GROUP_COUNTRIES = "Countries"
  private val GROUP_INDUSTRIES = "Industries"
  private val GROUP_TAGS = "Tags"

  private val possibleFilter  = EventBus[CompanyFilter]()
  private val checkEvents = EventBus[CheckValueEvents]()
  // Map[String, Set[String]] => CompanyFilter
  private val clicks = EventBus[Unit]() // clicks on the "apply" button
  private val dirty = clicks.events.mapTo(false).mergeWith(checkEvents.events.mapTo(true)) // emit either true or false depending to show "apply" or not
  private val state: Signal[CompanyFilter] = checkEvents.events
    .scanLeft(Map[String, Set[String]]()) { (currentMap, event) =>
      event match
        case  CheckValueEvents(groupName, value, checked) =>
          if (checked) currentMap + (groupName -> (currentMap.getOrElse(groupName, Set()) + value))
          else currentMap + (groupName -> (currentMap.getOrElse(groupName, Set()) - value))
    } // Singnal[Map[String, Set[String]]]
    .map { checkMap =>
      CompanyFilter(
        locations = checkMap.getOrElse(GROUP_LOCATION, Set()).toList,
        countries = checkMap.getOrElse(GROUP_COUNTRIES,Set()).toList,
        industries = checkMap.getOrElse(GROUP_INDUSTRIES, Set()).toList,
        tags = checkMap.getOrElse(GROUP_TAGS, Set()).toList
      )

    }
  // informs CompanyPage to rerender
  val triggerFilters: EventStream[CompanyFilter] = clicks.events.withCurrentValueOf(state)
  def apply() =
    div(
      onMountCallback(_ => useBackend(_.company.allFiltersEndpoint(())).emitTo(possibleFilter)),
      //child.text <-- triggerFilters.map(_.toString),
      cls := "accordion accordion-flush",
      idAttr := "accordionFlushExample",
      div(
        cls := "accordion-item",
        h2(
          cls := "accordion-header",
          idAttr := "flush-headingOne",
          button(
            cls := "accordion-button",
            idAttr := "accordion-search-filter",
            `type` := "button",
            htmlAttr("data-bs-toggle", StringAsIsCodec) := "collapse",
            htmlAttr("data-bs-target", StringAsIsCodec) := "#flush-collapseOne",
            htmlAttr("aria-expanded", StringAsIsCodec) := "true",
            htmlAttr("aria-controls", StringAsIsCodec) := "flush-collapseOne",
            div(
              cls := "jvm-recent-companies-accordion-body-heading",
              h3(
                span("Search"),
                " Filters"
              )
            )
          )
        ),
        div(
          cls := "accordion-collapse collapse show",
          idAttr := "flush-collapseOne",
          htmlAttr("aria-labelledby", StringAsIsCodec) := "flush-headingOne",
          htmlAttr("data-bs-parent", StringAsIsCodec) := "#accordionFlushExample",
          div(
            cls := "accordion-body p-0",
            renderFilterOptions(GROUP_LOCATION, _.locations),
            renderFilterOptions(GROUP_COUNTRIES, _.countries),
            renderFilterOptions(GROUP_INDUSTRIES, _.industries),
            renderFilterOptions(GROUP_TAGS, _.tags),
            renderApplyButton()
          )
        )
      )
    )

  def renderFilterOptions(groupName: String, optionsFn: CompanyFilter => List[String]) =
    div(
      cls := "accordion-item",
      h2(
        cls := "accordion-header",
        idAttr := s"heading$groupName",
        button(
          cls := "accordion-button collapsed",
          `type` := "button",
          htmlAttr("data-bs-toggle", StringAsIsCodec) := "collapse",
          htmlAttr("data-bs-target", StringAsIsCodec) := s"#collapse$groupName",
          htmlAttr("aria-expanded", StringAsIsCodec) := "false",
          htmlAttr("aria-controls", StringAsIsCodec) := s"collapse$groupName",
          groupName
        )
      ),
      div(
        cls := "accordion-collapse collapse",
        idAttr := s"collapse$groupName",
        htmlAttr("aria-labelledby", StringAsIsCodec) := "headingOne",
        htmlAttr("data-bs-parent", StringAsIsCodec) := "#accordionExample",
        div(
          cls := "accordion-body",
          div(
            cls := "mb-3",
            // statefull Singnals Vars
            children <-- possibleFilter.events.toSignal(CompanyFilter.empty)
              .map(filter =>
                optionsFn(filter).map(value => renderCheckbox(groupName, value))
            )

          )
        )
      )
    )

  private def renderCheckbox(groupName: String, value: String) =
      div(
        cls := "form-check",
        label(
          cls := "form-check-label",
          forId := s"filter-$groupName-$value",
          value
        ),
        input(
          cls := "form-check-input",
          `type` := "checkbox",
          idAttr := s"filter-$groupName-$value",
          onChange.mapToChecked.map(CheckValueEvents(groupName, value, _)) --> checkEvents
        )
      )

  private def renderApplyButton() =
    div(
      cls := "jvm-accordion-search-btn",
      button(
        disabled <-- dirty.toSignal(false).map(v => !v),
        onClick.mapTo(()) --> clicks,
        cls := "btn btn-primary",
        `type` := "button",
        "Apply Filters"
      )
    )

}
