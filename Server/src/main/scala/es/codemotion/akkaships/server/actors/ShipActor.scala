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
    case Shot(p) =>
      log.info("Shot received from {}", sender().toString())
      if (ship.hit(p)) {
        if (!touchedPositions.contains(p)) {
          touched += 1
          touchedPositions:::=p::Nil
          boardActor ! Shot(p)
          if (touched == ship.length) {
            sender ! SunkMessage("HUNDIDO")
            boardActor ! Ship(ship.pos, ship.orientation, ship.length, true)
            statisticsActor ! Statistics(sender.path.address.toString,true,true)
          }else{
            statisticsActor ! Statistics(sender.path.address.toString,true,false)
          }
        }else{
          statisticsActor ! Statistics(sender.toString(),false,false)
        }
      }

  }
}
