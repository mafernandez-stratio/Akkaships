package es.codemotion.akkaships.client

import akka.actor.ActorSystem
import com.typesafe.config.{ConfigValue, ConfigValueFactory}
import es.codemotion.akkaships.client.config.PlayerConfig
import org.apache.log4j.Logger

import scala.collection.JavaConversions._
import scala.language.postfixOps

object Player extends PlayerConfig{
  override lazy val logger = Logger.getLogger(getClass)

 def apply(seedNodes: java.util.List[String]) =
    new Player(seedNodes)

  def apply() = new Player()

}

class Player(properties: java.util.Map[String, ConfigValue]) {

  def this(serverHosts: java.util.List[String]) =
    this(Map(PlayerConfig.PlayerConfigHosts -> ConfigValueFactory.fromAnyRef(serverHosts)))

  def this() = this(Map.empty[String, ConfigValue])

  def initPlayer():Unit={
    lazy val logger = Player.logger
    val finalConfig = properties.foldLeft(Player.config) { case (previousConfig, keyValue) =>
      previousConfig.withValue(keyValue._1, keyValue._2)
    }

    val system = ActorSystem("ShipsServerCluster", finalConfig)

    if (logger.isDebugEnabled) {
      system.logConfiguration()
    }

    val serverNode=s"${Player.config.getStringList(PlayerConfig.ServerNode)(0)}/user/server"
    val serverActor = system.actorSelection(serverNode)
    system.actorOf(UserActor.props(serverActor), "playerActor")

  }

}