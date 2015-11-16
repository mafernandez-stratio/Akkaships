package es.codemotion.akkaships.server

import akka.actor.{ActorRef, Props, ActorSystem}
import akka.cluster.Cluster
import akka.contrib.pattern.ClusterReceptionistExtension
import akka.routing.{BroadcastGroup, RoundRobinPool}
import es.codemotion.akkaships.common.domain._
import es.codemotion.akkaships.server.config.ShipsServerConfig

import org.apache.commons.daemon.{Daemon, DaemonContext}
import org.apache.log4j.Logger

import scala.util.Random

class ShipServer extends Daemon with ShipsServerConfig {

 lazy val logger = Logger.getLogger(classOf[ShipServer])

  var system: Option[ActorSystem] = None


  override def init(p1: DaemonContext): Unit = ()

  override def start(): Unit = {

    system = Some(ActorSystem(clusterName, config))
    system.fold(throw new RuntimeException("Actor system cannot be started")) { actorSystem =>
      val serverActor = actorSystem.actorOf(
        RoundRobinPool(serverActorInstances).props(
          Props(classOf[Ship], Cluster(actorSystem))),actorName)
      ClusterReceptionistExtension(actorSystem).registerService(serverActor)
    }



    logger.info("Akka Ships Server started")
  }

  def placeShips(): Unit ={

      //generate 7 ship actors placed in the board
      val posx=Random
      val posy=Random
      val orientation=Random
      /*if(orientation.nextBoolean()){
        ship!checkPosition(Position(posx.nextInt(100), posy.nextInt(100)),Vertical)
      }else{
        ship!checkPosition(Position(posx.nextInt(100), posy.nextInt(100)),Horizontal)

      }
      val ship=Ship(Position(posx.nextInt(100), posy.nextInt(100)),4,false)
*/

  }

  override def stop(): Unit = {
    system.foreach(_.shutdown())
    logger.info("Akka Ship Server stopped")
  }

  override def destroy(): Unit = ()


  

}
