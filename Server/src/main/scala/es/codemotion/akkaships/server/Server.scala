package es.codemotion.akkaships.server

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.routing.{RoundRobinPool, BroadcastGroup}
import com.sun.corba.se.spi.monitoring.StatisticsAccumulator
import es.codemotion.akkaships.common.domain._
import es.codemotion.akkaships.server.actors.{StatisticsActor, BoardActor, WaterActor, ShipActor}
import es.codemotion.akkaships.server.config.ServerConfig
import org.apache.commons.daemon.{Daemon, DaemonContext}
import org.apache.log4j.Logger

class Server extends Daemon with ServerConfig {

  override lazy val logger = Logger.getLogger(classOf[Server])

  var system=ActorSystem(clusterName, config)


  override def init(p1: DaemonContext): Unit = ()

  override def start(): Unit = {


    val statisticsActor=system.actorOf(Props(new StatisticsActor()))
    val boardActor=system.actorOf(Props(new BoardActor(2,statisticsActor)),"boardActor")
    val statisticsRouter: ActorRef =
      system.actorOf(RoundRobinPool(5).props(Props[StatisticsActor]), "statisticRouter")

    val a1 = system.actorOf(Props(new ShipActor(Ship(Position(1,1),Vertical,4),boardActor,statisticsRouter)),
      "Portaviones")

    val a2 = system.actorOf(Props(new ShipActor(Ship(Position(3,3),Horizontal,2),boardActor,statisticsRouter)),"Lancha")

    val aguas= List(Position(8,8),Position(8,9),Position(8,12))
    val a3 = system.actorOf(Props(new WaterActor(aguas,boardActor,statisticsRouter)),"Agua")

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
