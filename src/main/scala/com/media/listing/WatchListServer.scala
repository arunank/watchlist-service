package com.media.listing

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object WatchListServer extends App with WatchListRoute {

  implicit val system: ActorSystem = ActorSystem("WatchListSourceActor")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val watchListSourceActor: ActorRef = system.actorOf(WatchListSourceActor.props, "watchListSourceActor")

  lazy val routes: Route = userRoutes

  Http().bindAndHandle(routes, "localhost", 8080)

  println(s"Server online at http://localhost:8080/")

  Await.result(system.whenTerminated, Duration.Inf)
}
