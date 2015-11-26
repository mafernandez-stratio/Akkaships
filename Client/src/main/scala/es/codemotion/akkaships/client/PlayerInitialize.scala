package es.codemotion.akkaships.client

import akka.actor.ActorSystem
import com.typesafe.config.{ConfigValue, ConfigValueFactory}
import es.codemotion.akkaships.client.config.PlayerConfig
import org.apache.log4j.Logger

import scala.collection.JavaConversions._
import scala.language.postfixOps

object PlayerInitialize extends PlayerConfig{
  override lazy val logger = Logger.getLogger(getClass)

 def apply(seedNodes: java.util.List[String]) =
    new PlayerInitialize(seedNodes)

  def apply() = new PlayerInitialize()

}

class PlayerInitialize(properties: java.util.Map[String, ConfigValue]) {

  def this(serverHosts: java.util.List[String]) =
    this(Map(PlayerConfig.PlayerConfigHosts -> ConfigValueFactory.fromAnyRef(serverHosts)))

  def this() = this(Map.empty[String, ConfigValue])

  def initPlayer():Unit={
    lazy val logger = PlayerInitialize.logger
    val finalConfig = properties.foldLeft(PlayerInitialize.config) { case (previousConfig, keyValue) =>
      previousConfig.withValue(keyValue._1, keyValue._2)
    }

    // Inicializar sistema de actores en cluster
    val system = ActorSystem("ShipsServerCluster", finalConfig)

    if (logger.isDebugEnabled) {
      system.logConfiguration()
    }

    //Obtenemos el seed node del server
    val serverNode=s"${PlayerInitialize.config.getStringList(PlayerConfig.ServerNode)(0)}/user/server"

    //Obtenemos el ActorRef del server
    val serverActor = system.actorSelection(serverNode)

    //Inicializamos el player Actor
    system.actorOf(PlayerActor.props(serverActor), "playerActor")


  }

}