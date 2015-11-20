package es.codemotion.akkaships.server

import akka.actor.{ActorSystem, Props}
import akka.routing.BroadcastGroup
import es.codemotion.akkaships.common.domain._
import es.codemotion.akkaships.server.actors.{BoardActor, WaterActor, ShipActor}
import es.codemotion.akkaships.server.config.ServerConfig
import org.apache.commons.daemon.{Daemon, DaemonContext}
import org.apache.log4j.Logger

class Server extends Daemon with ServerConfig {

  override lazy val logger = Logger.getLogger(classOf[Server])

  var system=ActorSystem(clusterName, config)


  override def init(p1: DaemonContext): Unit = ()

  override def start(): Unit = {

    val a1 = system.actorOf(Props(new ShipActor(Ship(Position(1,1),Vertical,4))), "Portaviones")

    val a2 = system.actorOf(Props(new ShipActor(Ship(Position(15,3),Horizontal,2))),"Lancha")

    val aguas= List(Position(1,2),Position(1,3),Position(1,4))
    val a3 = system.actorOf(Props(new WaterActor(aguas)),"Agua")

    val routees = Vector[String]("/user/Portaviones", "/user/Lancha", "/user/Agua")

    system.actorOf(BroadcastGroup(routees).props(),"server")
    system.actorOf(Props(new BoardActor))

    logger.info("Akka Ship Server Started")
  }

  override def stop(): Unit = {
    system.shutdown()
    logger.info("Akka Ship Server Stopped")
  }

  override def destroy(): Unit = ()
}
