package es.codemotion.akkaships.server

import akka.actor.{Props, ActorSystem}
import com.typesafe.config.ConfigFactory

object ServerApp extends App {

  val config = ConfigFactory.load()

  // Create an Akka system
  val system = ActorSystem("AkkashipsSystem", config)

  // Create an actor that handles cluster domain events
  val server = system.actorOf(Props[Server], name = "AkkashipsServer")

}
