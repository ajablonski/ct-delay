package com.github.ajablonski.shared.serialization

import com.github.ajablonski.shared.model.{BusRouteType, Point, Route, RouteType, Shape, TrainRouteType}
import play.api.libs.json.{JsError, JsObject, JsSuccess, Json, OFormat, OWrites, Reads}

object RouteSerializers {
  implicit val routeTypeFormat: OFormat[RouteType] = Json.format[RouteType]

  implicit val busRouteFormat: OFormat[BusRouteType.type] = OFormat[BusRouteType.type](Reads[BusRouteType.type] {
    case JsObject(_) => JsSuccess(BusRouteType)
    case _           => JsError("Empty object expected")
  }, OWrites[BusRouteType.type] { _ =>
    Json.obj()
  })

  implicit val trainRouteFormat: OFormat[TrainRouteType.type] = OFormat[TrainRouteType.type](Reads[TrainRouteType.type] {
    case JsObject(_) => JsSuccess(TrainRouteType)
    case _           => JsError("Empty object expected")
  }, OWrites[TrainRouteType.type] { _ =>
    Json.obj()
  })

  implicit val routeFormat: OFormat[Route] = Json.format[Route]

  implicit val pointFormat: OFormat[Point] = Json.format[Point]
  implicit val shapeFormat: OFormat[Shape] = Json.format[Shape]


}
