package es.codemotion.akkaships.integration

import akka.actor.{Actor, ActorSystem, Props}
import es.codemotion.akkaships.client.UserActor
import es.codemotion.akkaships.client.UserActor.ShowTextMessage
import es.codemotion.akkaships.common.domain._

class DummyServer extends Actor {
  override def receive: Receive = {
    case Shot(pos) => sender ! ShowTextMessage(s"Last shot: (${pos.i}, ${pos.j})")
  }
}

object SampleApp extends App {

  val system = ActorSystem("BattleShipGUISample")

  val server = system.actorOf(Props(new DummyServer))
  val gui = system.actorOf(UserActor.props(server))

  val elements = Ship(Position(10,10), Horizontal, 10)::
    Ship(Position(9,3), Vertical, 10, true)::Shot(Position(10,11))::
    Shot(Position(0,0))::Shot(Position(0,1))::
    Shot(Position(1,0))::Shot(Position(1,1))::Nil

  gui ! BoardUpdate(elements)

  system.wait()

}
