package es.codemotion.akkaships.server.actors

import akka.actor.{Props, Actor}
import akka.routing.{BroadcastRoutingLogic, Router, ActorRefRoutee}
import es.codemotion.akkaships.common.domain.{Shot, Orientation, Position, Ship}

//messages
case class checkPosition(pos:Position, orientation:Orientation)

class ShipsRouterActor extends Actor {
  var router = {
    val routees = Vector.fill(7) {
      val r = context.actorOf(Props[Ship])
      context watch r
      ActorRefRoutee(r)
    }
    Router(BroadcastRoutingLogic(), routees)
  }

  def receive = {
    case checkPosition(pos:Position, orientation:Orientation)=>
    case Shot(position:Position) =>
    case _=>

  }
}
