package es.codemotion.akkaships.server.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import es.codemotion.akkaships.common.domain.{Position, Shot, Water}

object WaterActor {
  def props(pos: Seq[Position],boardActor:ActorRef): Props = Props(new WaterActor(pos,boardActor))
}

class WaterActor( val pos: Seq[Position],boardActor:ActorRef)  extends Actor with ActorLogging {
  //val boardActor:ActorRef=context.system.actorOf(Props(new BoardActor))
  def receive = {
    case Shot(s) =>
      log.info("Shot received from {}", sender.toString())
      if (pos.contains(s))
        boardActor ! Water(s)

    case x =>log.info(s"Unknown message:     ${x}       --------->" + self.path)
  }


}
