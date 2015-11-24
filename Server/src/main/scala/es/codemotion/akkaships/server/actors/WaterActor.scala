package es.codemotion.akkaships.server.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import es.codemotion.akkaships.common.domain.{Position, Shot, Water}

object WaterActor {
  def props(pos: Seq[Position],boardActor:ActorRef,statisticsActor:ActorRef): Props = Props(new WaterActor(pos,
    boardActor,statisticsActor))
}

class WaterActor( val pos: Seq[Position],boardActor:ActorRef,val statisticsActor:ActorRef)  extends Actor with
ActorLogging {

  def receive = {
    case Shot(s) =>
      log.info("Shot received from {}", sender.toString())
      if (pos.contains(s))
        boardActor ! Water(s)

    case x =>log.info(s"Unknown message:     ${x}       --------->" + self.path)
  }


}
