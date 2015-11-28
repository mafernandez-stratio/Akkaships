package es.codemotion.akkaships.server.actors

import akka.actor._
import es.codemotion.akkaships.common.domain._

object ShipActor {
  def props(ship:Ship, boardActor:ActorRef,statisticsActor:ActorRef): Props = Props(new ShipActor(ship,boardActor,statisticsActor))
}

class ShipActor(val ship:Ship, val boardActor:ActorRef, val statisticsActor:ActorRef)  extends Actor with ActorLogging{
  var touched=0
  var touchedPositions=List[Position]()

  def receive = {
    ???
  }
}
