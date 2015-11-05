package es.codemotion.akkaships.integration

import akka.actor.ActorSystem
import es.codemotion.akkaships.client.UserActor
import es.codemotion.akkaships.client.UserActor.BoardUpdate
import es.codemotion.akkaships.common.domain._

object SampleApp extends App {

  val system = ActorSystem("BattleShipGUISample")

  val gui = system.actorOf(UserActor.props)

  val elements = Ship(Position(10,10), Horizontal, 10)::
    Ship(Position(10,3), Vertical, 10, true)::
    Shot(Position(10,11))::Nil

  gui ! BoardUpdate(elements)

  system.wait()

}
