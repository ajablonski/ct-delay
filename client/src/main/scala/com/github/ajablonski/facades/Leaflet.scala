package com.github.ajablonski.facades

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport
import scala.scalajs.js.{UndefOr, |}


@JSImport("leaflet", JSImport.Namespace)
@js.native
object Leaflet extends js.Object {
  def map(id: String, config: js.Dictionary[js.Any]): Map = js.native

  def tileLayer(url: String, config: js.Dictionary[js.Any]): TileLayer = js.native

  def icon(config: js.Dictionary[js.Any]): Icon = js.native

  def marker(coords: js.Dictionary[Double], config: js.Dictionary[js.Any]): Marker = js.native

  def featureGroup(layers: Layer*): FeatureGroup = js.native

  def geoJSON(data: js.Array[js.Dynamic], config: js.Dictionary[js.Any]): GeoJSON = js.native

  def polyline(latlngs: js.Array[Double] | js.Array[js.Array[Double]], config: UndefOr[js.Dictionary[js.Any]]): Polyline = js.native

  val GeoJSON: GeoJSON = js.native

  val LineUtil: LineUtil = js.native
}

@JSImport("leaflet", "Map")
@js.native
class Map extends js.Object {
  def fitBounds(bounds: LatLngBounds): Map = js.native

  def eachLayer(layerFunction: js.Function1[Layer, Unit]): Map = js.native
}


@JSImport("leaflet", "Layer")
@js.native
class Layer extends js.Object {
  def removeFrom(map: Map): this.type = js.native

  def remove(): Layer = js.native

  def addTo(map: Map): this.type = js.native
}

@JSImport("leaflet", "TileLayer")
@js.native
class TileLayer extends Layer {}


@JSImport("leaflet", "Marker")
@js.native
class Marker extends Layer {}

@JSImport("leaflet", "Icon")
@js.native
class Icon extends js.Object {}

@JSImport("leaflet", "FeatureGroup")
@js.native
class FeatureGroup extends Layer {
  def addLayer(layer: Layer): FeatureGroup = js.native

  def getBounds(): LatLngBounds = js.native

  def invoke(method: String, args: js.Any*): Nothing = js.native
}

@JSImport("leaflet", "GeoJSON")
@js.native
class GeoJSON extends FeatureGroup {
  def addData(data: js.Array[js.Dynamic]): GeoJSON = js.native

  def coordsToLatLng(coords: js.Array[Double]): LatLng = js.native
}

@JSImport("leaflet", "LatLngBounds")
@js.native
class LatLngBounds extends js.Object {}

@JSImport("leaflet", "LatLng")
@js.native
class LatLng extends js.Object {}

@JSImport("leaflet", "Polyline")
@js.native
class Polyline extends Layer {}

@JSImport("leaflet", "LineUtil")
@js.native
class LineUtil extends js.Object {
  def simplify(points: js.Array[js.Array[Double]], tolerance: js.UndefOr[Double]): js.Array[js.Array[Double]] = js.native
}