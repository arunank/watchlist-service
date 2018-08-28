package com.media.listing

import akka.actor.{Actor, ActorLogging, Props}
import com.media.listing.WatchListSourceActor.{AddAssetToUserId, DeleteAsset, GetAssetsByUserId}

import scala.collection.mutable

/**
  * Created by arunankannan on 27/08/2018.
  */

// todo implement 5 digit uniqueness
case class User(userId: String)

case class Asset(assetId: String, assetName: String, assetLink: String) {
  override def equals(asset: Any): Boolean = {
    asset match {
      case p: Asset => asset.equals(p.assetId)
      case _ => false
    }
  }
}

// basic crud operation with source of listing
object WatchListSourceActor {

  final case class GetAssetsByUserId(userId: String)
  final case class AddAssetToUserId(userId: String, asset: Asset)
  final case class DeleteAsset(userId: String, asset: Asset)

  def props: Props = Props[WatchListSourceActor]
}


// main actor for CRUD operation with source - implementation
class WatchListSourceActor extends Actor with ActorLogging {

  // Source of Data - InMemory
  var WatchListSource = Map[User,mutable.MutableList[Asset]]()

  override def receive: Receive = {
      case GetAssetsByUserId(id) => sender() ! WatchListSource.get(User(id))
      case AddAssetToUserId(id, asset) => {
        val maybeAssets = WatchListSource.get(User(id))
        if (maybeAssets.isDefined) {
          WatchListSource = WatchListSource + (User(id) -> (maybeAssets.get :+ asset))
        }  else {
          WatchListSource = WatchListSource + (User(id) -> mutable.MutableList(asset))
        }
        sender() ! WatchListSource.get(User(id))
      }
      case DeleteAsset(id, asset) => {
        val maybeAssets = WatchListSource.get(User(id))
        if (maybeAssets.isDefined) {
          WatchListSource = WatchListSource + (User(id) -> (maybeAssets.get.filter(ast => asset == asset)))
        }
        sender() ! WatchListSource.get(User(id))
      }
  }

}