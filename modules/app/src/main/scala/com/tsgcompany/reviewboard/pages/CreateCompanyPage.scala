package com.tsgcompany.reviewboard.pages

import com.raquo.laminar.api.L.{*, given}
import com.raquo.laminar.nodes.ReactiveHtmlElement
import com.tsgcompany.reviewboard.common.Constants
import com.tsgcompany.reviewboard.core.ZJS.*
import com.tsgcompany.reviewboard.http.requests.CreateCompanyRequest
import org.scalajs.dom.{File, FileReader}
import org.scalajs.dom.html.Element
import zio.*

case class CreateCompanyState (
                                name: String = "",
                                url: String= "",
                                location: Option[String] = None,
                                country: Option[String] = None,
                                industry: Option[String] = None,
                                image: Option[String] = None,
                                tags: List[String] = List(),
                                upstreamStatus: Option[Either[String, String]] = None,
                                override val showStatus: Boolean = false
                              ) extends FormState {

  override def errorList: List[Option[String]] =
    List(
      Option.when(name.isEmpty)("The name cannot be empty"),
      Option.when(!url.matches(Constants.urlRegex))("The URL is invalid"),
    ) ++ upstreamStatus.map(_.left.toOption).toList

  override def maybeSuccess: Option[String] =
    upstreamStatus.flatMap(_.toOption)

  def toRequest: CreateCompanyRequest =
    CreateCompanyRequest(
      name, url, location, country, industry, image, Option(tags).filter(_.nonEmpty)
    )
}

object CreateCompanyPage extends FormPage[CreateCompanyState]("Post New Company") {
  override def basicState: CreateCompanyState = CreateCompanyState()
  override def renderChildren(): List[ReactiveHtmlElement[Element]] = List(
    renderInput("Company name", "name", "text", true, "ACME Inc", (s,v) => s.copy(name =v)),
    renderInput("Company URL", "url", "text", true, "https://acme.com", (s,v) => s.copy(url =v)),
    renderLogoUpload("Company logo", "logo"),
    renderInput("Location", "location", "text", false, "Somewhere", (s,v) => s.copy(location =Some(v))),
    renderInput("Country", "country", "text", false, "Some Country", (s,v) => s.copy(country =Some(v))),
    renderInput("Industry", "industry", "text", false, "FP", (s,v) => s.copy(industry =Some(v))),
    renderInput("Tags - separate by ,", "tags", "text", false, "Scala, zio", (s,v) => s.copy(tags =v.split(",").map(_.trim).toList)),
    //log - file upload - TODO
    button(
      `type` := "button",
      "Post company",
      onClick.preventDefault.mapTo(stateVar.now()) --> submitter
    )
  )

  private def renderLogoUpload(name: String, uid: String, isRequired: Boolean = false) =
    div(
      cls := "row",
      div(
        cls := "col-md-12",
        div(
          cls := "form-input",
          label(
            forId := uid,
            cls := "form-label",
            if (isRequired) span("*") else span(),
            name
          ),
          input(
            `type` := "file",
            cls := "form-control",
            idAttr := uid,
            accept := "image/*",
            onChange.mapToFiles --> (files => ())
          )
        )
      )
    )

  val fileUploader = (files: List[File]) => {
    val maybeFile = files.headOption.filter(_.size > 0)
    maybeFile.foreach { file =>
      val reader = new FileReader
      reader.onload = _ => {
        stateVar.update(_.copy(image = Some(reader.result.toString)))
      }
      reader.readAsDataURL(file)
    }
  }

  val submitter = Observer[CreateCompanyState] { state =>
    if (state.hasErrors) {
      stateVar.update(_.copy(showStatus = true))
    }
    else {
      useBackend(_.company.createEndpoint(state.toRequest))
        .map { company =>
          stateVar.update(_.copy(showStatus = true, upstreamStatus = Some(Right("Company posted! Check it out in the company list"))))
        }
        .tapError { e =>
          ZIO.succeed {
            stateVar.update(_.copy(showStatus = true, upstreamStatus = Some(Left(e.getMessage))))
          }
        }
        .runJs
      //dom.console.log(s"Current state is: $state")
    }
  }

}
