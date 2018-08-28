package com.media.listing

import akka.actor.ActorRef
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{ContentTypes, HttpRequest, MessageEntity, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{Matchers, WordSpec}
import org.scalatest.concurrent.ScalaFutures

/**
  * Created by arunankannan on 27/08/2018.
  */
class WatchListServiceTest  extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest with WatchListRoute {


  val watchListSourceActor: ActorRef = system.actorOf(WatchListSourceActor.props, "watchListSourceActor")

  lazy val routes: Route = userRoutes

  "watch listing Route" should {

    "return nothing for user that does not exit (GET /listings/id)" in {

      val request = HttpRequest(uri = "/watchlist/123")

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)

        contentType should ===(ContentTypes.NoContentType)

        entityAs[String] should ===("")
      }
    }


    "return single asset for the asset added to new user and add cont to same user" in {

      val asset1 = Asset("cont1", "aname1", "link")
      val assetEntity1 = Marshal(asset1).to[MessageEntity].futureValue
      val request = Post("/watchlist/123").withEntity(assetEntity1)

      request ~> routes ~> check {
        status should ===(StatusCodes.Created)
        contentType should ===(ContentTypes.`application/json`)
        entityAs[String] should be ("""[{"assetId":"cont1","assetName":"aname1","assetLink":"link"}]""")
      }


      val asset2 = Asset("cont2", "aname2", "link")
      val assetEntity2 = Marshal(asset2).to[MessageEntity].futureValue
      val request2 = Post("/watchlist/123").withEntity(assetEntity2)

      request2 ~> routes ~> check {
        status should ===(StatusCodes.Created)
        contentType should ===(ContentTypes.`application/json`)
        entityAs[String] should be ("""[{"assetId":"cont1","assetName":"aname1","assetLink":"link"},{"assetId":"cont2","assetName":"aname2","assetLink":"link"}]""")
      }
    }

    "return single asset for the asset added to new user and no confusion between two unique user" in {

      val asset1 = Asset("cont1", "aname1", "link")
      val assetEntity1 = Marshal(asset1).to[MessageEntity].futureValue
      val request = Post("/watchlist/1234").withEntity(assetEntity1)

      request ~> routes ~> check {
        status should ===(StatusCodes.Created)
        contentType should ===(ContentTypes.`application/json`)
        entityAs[String] should be ("""[{"assetId":"cont1","assetName":"aname1","assetLink":"link"}]""")
      }

      // new user
      val asset2 = Asset("cont3", "aname3", "link")
      val assetEntity2 = Marshal(asset2).to[MessageEntity].futureValue
      val request2 = Post("/watchlist/4567").withEntity(assetEntity2)

      request2 ~> routes ~> check {
        status should ===(StatusCodes.Created)
        contentType should ===(ContentTypes.`application/json`)
        entityAs[String] should be ("""[{"assetId":"cont3","assetName":"aname3","assetLink":"link"}]""")
      }
    }


    "delete a cont from user" in {
      val asset1 = Asset("cont1", "aname1", "link")
      val assetEntity1 = Marshal(asset1).to[MessageEntity].futureValue

      val request = Delete("/watchlist/123").withEntity(assetEntity1)

      request ~> routes ~> check {
        status should ===(StatusCodes.Created)
        contentType should ===(ContentTypes.`application/json`)
        entityAs[String] should be ("""[]""")
      }
    }

  }
}