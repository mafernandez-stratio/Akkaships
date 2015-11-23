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

    val boardActor=system.actorOf(Props(new BoardActor(2)),"boardActor")
    val a1 = system.actorOf(Props(new ShipActor(Ship(Position(1,1),Vertical,4),boardActor)), "Portaviones")

    val a2 = system.actorOf(Props(new ShipActor(Ship(Position(3,3),Horizontal,2),boardActor)),"Lancha")

    val aguas= List(Position(8,8),Position(8,9),Position(8,12))
    val a3 = system.actorOf(Props(new WaterActor(aguas,boardActor)),"Agua")

    val routees = Vector[String]("/user/Portaviones", "/user/Lancha", "/user/Agua")

    system.actorOf(BroadcastGroup(routees).props(),"server")


    logger.info("Akka Ship Server Started")
  }

  override def stop(): Unit = {
    system.shutdown()
    logger.info("Akka Ship Server Stopped")
  }

  override def destroy(): Unit = ()
}
