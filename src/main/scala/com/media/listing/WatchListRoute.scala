package com.media.listing

import akka.actor.{ActorRef, ActorSystem}
import akka.event.Logging
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.util.Timeout
import akka.pattern.ask

import scala.concurrent.Future
import scala.concurrent.duration._
import com.media.listing.WatchListSourceActor._

import scala.collection.mutable

/**
  * Created by arunankannan on 27/08/2018.
  */


import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val assetListFormat: JsonFormat[Asset] = jsonFormat3(Asset)
  implicit val assetFormat  = jsonFormat3(Asset)
  implicit def mutableListFormat[T :JsonFormat] = new RootJsonFormat[mutable.MutableList[T]] {
    def write(listBuffer: mutable.MutableList[T]) = JsArray(listBuffer.map(_.toJson).toVector)
    def read(value: JsValue): mutable.MutableList[T] = value match {
      case JsArray(elements) => elements.map(_.convertTo[T])(collection.breakOut)
      case x => deserializationError("Expected MutableList as JsArray, but got " + x)
    }
  }
}


trait WatchListRoute extends JsonSupport {

  implicit def system: ActorSystem

  lazy val log = Logging(system, classOf[WatchListRoute])

  def watchListSourceActor: ActorRef

  implicit lazy val timeout = Timeout(5.seconds)

  lazy val userRoutes: Route =
    pathPrefix("watchlist") {
      concat(
        pathEnd {
          concat(
            get {
              complete("hi")
            }
          )
        },
        path(Segment) { id =>
          concat(
            get {
              val maybeAssets: Future[Option[mutable.MutableList[Asset]]] = (watchListSourceActor ? GetAssetsByUserId(id)).mapTo[Option[mutable.MutableList[Asset]]]
              onSuccess(maybeAssets) { assets =>
                log.info("listing [{}]: {}", assets)
                complete((StatusCodes.OK, assets))
              }
            },
            post {
              entity(as[Asset]) { asset: Asset =>
                val assetCreated: Future[Option[mutable.MutableList[Asset]]] =
                  (watchListSourceActor ? WatchListSourceActor.AddAssetToUserId(id, asset)).mapTo[Option[mutable.MutableList[Asset]]]
                onSuccess(assetCreated) { assets =>
                  log.info("Created listing [{}]: {}", assets)
                  complete((StatusCodes.Created, assets))
                }
              }
            },
            delete {
              entity(as[Asset]) { asset: Asset =>
                val assetDeleted: Future[Option[mutable.MutableList[Asset]]] =
                  (watchListSourceActor ? WatchListSourceActor.DeleteAsset(id, asset)).mapTo[Option[mutable.MutableList[Asset]]]
                onSuccess(assetDeleted) { assets =>
                  log.info("Deleted. listing [{}]: {}", assets)
                  complete((StatusCodes.Created, assets))
                }
              }
            }
          )
        }
      )
    }

}
