package com.github.ajablonski.components

import com.github.ajablonski.facades
import com.github.ajablonski.facades._
import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveElement
import org.scalajs.dom.experimental.{HttpMethod, Request, RequestInit}
import org.scalajs.dom.html

import java.nio.charset.StandardCharsets
import java.util.Base64
import scala.scalajs.js
import scala.scalajs.js.UndefOr


class LeafletMapManager(routeStream: Signal[String]) {
  private val mapId = "mapid"
  private var realtimeIcons: Option[Realtime] = None

  def render(): ReactiveElement[html.Div] = {
    div(
      idAttr := mapId,

      onMountCallback(ctx => {
        val map = initMap()

        routeStream.addObserver(Observer(route => {
          realtimeIcons.foreach(_.removeFrom(map))
          realtimeIcons = Some(updateRealtimeRefresh(map, route))
          realtimeIcons.map { icons => map.fitBounds(icons.getBounds()) }
        }))(ctx.owner)
      })
    )
  }

  private def initMap(): facades.Map = {
    val map = Leaflet.map(mapId, js.Dictionary(
      "center" -> js.Array(41.8781, -87.6298),
      "zoom" -> 13
    ))

    Leaflet.tileLayer("https://stamen-tiles-{s}.a.ssl.fastly.net/toner-lite/{z}/{x}/{y}{r}.{ext}", js.Dictionary(
      "attribution" -> """Map tiles by <a href="http://stamen.com">Stamen Design</a>, <a href="http://creativecommons.org/licenses/by/3.0">CC BY 3.0</a> &mdash; Map data &copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors""",
      "minZoom" -> 0,
      "maxZoom" -> 20,
      "ext" -> "png"
    )).addTo(map)

    map
  }

  private def updateRealtimeRefresh(map: facades.Map, route: String): Realtime = {
    val request = new Request(f"/routes/$route", new RequestInit() {
      method = HttpMethod.GET
      headers = js.Dictionary[String]("Accept" -> "application/geo+json")
    })
    new Realtime(request, js.Dictionary(
      "pointToLayer" -> pointToLayerFn,
      "interval" -> 10_000,
      "getFeatureId" -> {
        (_: js.Dynamic).properties.blockId.asInstanceOf[String]
      },
      "onlyRunWhenAdded" -> true,
      "updateFeature" -> {
        (feature: js.Dynamic, oldLayer: UndefOr[FeatureGroup]) => {
          if (oldLayer.isDefined) {
            oldLayer.get.remove()
            val latLng = Leaflet.GeoJSON.coordsToLatLng(feature.geometry.coordinates.asInstanceOf[js.Array[Double]])
            pointToLayerFn(feature, latLng)
          } else {
            oldLayer
          }
        }
      }
    )).addTo(map)
  }

  private val pointToLayerFn: (js.Dynamic, LatLng) => FeatureGroup = (geoJsonPoint: js.Dynamic, latLon: LatLng) => {
    val group = Leaflet.featureGroup()
    val marker = Leaflet
      .marker(latLon.asInstanceOf[js.Dictionary[Double]], js.Dictionary("icon" -> busIcon))
    val arrow = Leaflet
      .marker(latLon.asInstanceOf[js.Dictionary[Double]], js.Dictionary("icon" -> buildArrow(geoJsonPoint.properties.heading.asInstanceOf[String].toInt)))
    group.addLayer(marker)
    group.addLayer(arrow)
    group
  }

  private val busIcon: Icon = {
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

  private def buildArrow(rotation: Int): Icon = {
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