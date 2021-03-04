package com.github.ajablonski

import com.github.ajablonski.shared.SharedMessages
import com.github.ajablonski.shared.model._
import com.github.ajablonski.shared.serialization.RouteSerializers
import org.scalajs.dom
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.raw.Event
import play.api.libs.json._

import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.util.Base64
import scala.scalajs.js


object Main {
  implicit val routeReads = RouteSerializers.routeFormat
  implicit val localDateTimeReads = Reads[LocalDateTime] {
    case JsString(value) => JsSuccess(LocalDateTime.parse(value))
    case _ => JsError("String expected")
  }
  implicit val busReads = Json.reads[Bus]

  private val busIcon = buildBus()

  val defaultRoute = "22"

  var icons: FeatureGroup = _

  def main(args: Array[String]): Unit = {

    dom.document.getElementById("scalajsShoutOut").textContent = SharedMessages.itWorks

    val map = initMap()
    initSelect(onRouteChange(map))
    icons = updateMap(map, defaultRoute)
  }

  def onRouteChange(map: Map): Event => Unit = { (event: Event) =>
    icons.remove()
    icons = updateMap(map, event.target.asInstanceOf[dom.html.Select].value)
  }

  def initSelect(routeChangeFunction: js.Function1[Event, _]): Unit = {
    import scala.concurrent.ExecutionContext.Implicits.global

    val routesSelect: dom.html.Select = dom.document.getElementById("routes").asInstanceOf[dom.html.Select]

    routesSelect.onchange = routeChangeFunction
    Ajax
      .get("/routes")
      .foreach(xhr => {

        Json.parse(xhr.responseText)
          .as[List[Route]]
          .foreach {
            route =>
              val option = dom.document.createElement("option").asInstanceOf[dom.html.Option]
              option.label = f"${route.routeId}: ${route.name}"
              option.innerText = f"${route.routeId}: ${route.name}"
              option.value = route.routeId

              routesSelect.appendChild(option)
          }
        routesSelect.value = defaultRoute
      })
  }

  def initMap(): Map = {
    val map = Leaflet.map("mapid", js.Dictionary(
      "center" -> js.Array(41.8781, -87.6298),
      "zoom" -> 13
    ))

    Leaflet.tileLayer("https://stamen-tiles-{s}.a.ssl.fastly.net/toner-lite/{z}/{x}/{y}{r}.{ext}", js.Dictionary(
      "attribution" -> """Map tiles by <a href="http://stamen.com">Stamen Design</a>, <a href="http://creativecommons.org/licenses/by/3.0">CC BY 3.0</a> &mdash; Map data &copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors""",
      "subdomains" -> "abcd",
      "minZoom" -> 0,
      "maxZoom" -> 20,
      "ext" -> "png"
    )).addTo(map)

    map
  }

  def updateMap(map: Map, route: String): FeatureGroup = {
    import scala.concurrent.ExecutionContext.Implicits.global

    val markerGroup = Leaflet.featureGroup()

    Ajax
      .get(f"/routes/$route")
      .foreach {
        xhr =>
          Json.parse(xhr.responseText)
            .as[List[Bus]]
            .foreach { bus =>
              val marker = Leaflet
                .marker(js.Array(bus.latitude, bus.longitude), js.Dictionary("icon" -> busIcon))
              val arrow = Leaflet
                .marker(js.Array(bus.latitude, bus.longitude), js.Dictionary("icon" -> buildArrow(bus.heading)))
              markerGroup.addLayer(marker)
              markerGroup.addLayer(arrow)
            }

          markerGroup.addTo(map)
          map.fitBounds(markerGroup.getBounds())
      }

    markerGroup
  }

  def buildBus(): Icon = {
    val size = 30
    val busSvg =
      f"""
         |<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="black" width="${size}px" height="${size}px">
         |  <path d="M0 0h24v24H0z" fill="none"/>
         |  <path d="M4 16c0 .88.39 1.67 1 2.22V20c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-1h8v1c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-1.78c.61-.55 1-1.34 1-2.22V6c0-3.5-3.58-4-8-4s-8 .5-8 4v10zm3.5 1c-.83 0-1.5-.67-1.5-1.5S6.67 14 7.5 14s1.5.67 1.5 1.5S8.33 17 7.5 17zm9 0c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5zm1.5-6H6V6h12v5z"/>
         |</svg>""".stripMargin
    Leaflet.icon(js.Dictionary(
      "iconUrl" -> f"data:image/svg+xml;base64,${Base64.getEncoder.encodeToString(busSvg.getBytes(StandardCharsets.UTF_8))}",
      "iconSize" -> js.Array(size, size),
      "iconAnchor" -> js.Array(size / 2, size / 2)
    ))
  }

  def buildArrow(rotation: Int): Icon = {
    val size = 60
    val arrowWidth = 8
    val arrowHeight = 12
    val arrowSvg =
      f"""<svg xmlns="http://www.w3.org/2000/svg" width="$size" height="$size">
         |  <g transform="rotate($rotation ${size / 2} ${size / 2})">
         |    <polyline stroke="#303f9f" fill="#303f9f" points="${size / 2 - arrowWidth / 2}, $arrowHeight
         |                                                      ${size / 2}, 0
         |                                                      ${size / 2 + arrowWidth / 2}, $arrowHeight"/>
         |  </g>
         |</svg>
         |""".stripMargin
    Leaflet.icon(js.Dictionary(
      "iconUrl" -> f"data:image/svg+xml;base64,${Base64.getEncoder.encodeToString(arrowSvg.getBytes(StandardCharsets.UTF_8))}",
      "iconSize" -> js.Array(size, size),
      "iconAnchor" -> js.Array(size / 2, size / 2)
    ))
  }
}