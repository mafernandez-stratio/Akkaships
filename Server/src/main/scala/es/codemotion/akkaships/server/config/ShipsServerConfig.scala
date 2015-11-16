package es.codemotion.akkaships.server.config

import java.io.File
import com.typesafe.config.{ConfigFactory, Config}
import org.apache.log4j.Logger

object ShipsServerConfig {
  val SERVER_BASIC_CONFIG = "server-reference.conf"
  val PARENT_CONFIG_NAME = "crossdata-server"

  //  akka cluster values
  val SERVER_CLUSTER_NAME_KEY = "config.cluster.name"
  val SERVER_ACTOR_NAME_KEY = "config.cluster.actor"
  val SERVER_USER_CONFIG_FILE = "external.config.filename"
  val SERVER_USER_CONFIG_RESOURCE = "external.config.resource"
}

trait ShipsServerConfig extends NumberActorConfig {

  def getLocalIPs(): List[String] = {
    val addresses = for {
      networkInterface <- java.net.NetworkInterface.getNetworkInterfaces()
      address <- networkInterface.getInetAddresses
    } yield address.toString
    val filterthese = List(".*127.0.0.1", ".*localhost.*", ".*::1", ".*0:0:0:0:0:0:0:1")
    for {r <- addresses.toList; if (filterthese.find(e => r.matches(e)).isEmpty)} yield r
  }

  val ips = getLocalIPs()

  val logger: Logger


  lazy val clusterName = config.getString(ShipsServerConfig.SERVER_CLUSTER_NAME_KEY)
  lazy val actorName = config.getString(ShipsServerConfig.SERVER_ACTOR_NAME_KEY)

  override val config: Config = {

    var defaultConfig = ConfigFactory.load(ShipsServerConfig.SERVER_BASIC_CONFIG).getConfig(ShipsServerConfig.PARENT_CONFIG_NAME)
    val configFile = defaultConfig.getString(ShipsServerConfig.SERVER_USER_CONFIG_FILE)
    val configResource = defaultConfig.getString(ShipsServerConfig.SERVER_USER_CONFIG_RESOURCE)

    if (configResource != "") {
      val resource = ShipsServerConfig.getClass.getClassLoader.getResource(configResource)
      if (resource != null) {
        val userConfig = ConfigFactory.parseResources(configResource).getConfig(ShipsServerConfig.PARENT_CONFIG_NAME)
        defaultConfig = userConfig.withFallback(defaultConfig)
      } else {
        logger.warn("User resource (" + configResource + ") haven't been found")
        val file = new File(configResource)
        if (file.exists()) {
          val userConfig = ConfigFactory.parseFile(file).getConfig(ShipsServerConfig.PARENT_CONFIG_NAME)
          defaultConfig = userConfig.withFallback(defaultConfig)
        } else {
          logger.warn("User file (" + configResource + ") haven't been found in classpath")
        }
      }
    }

    if (configFile != "") {
      val file = new File(configFile)
      if (file.exists()) {
        val userConfig = ConfigFactory.parseFile(file).getConfig(ShipsServerConfig.PARENT_CONFIG_NAME)
        defaultConfig = userConfig.withFallback(defaultConfig)
      } else {
        logger.warn("User file (" + configFile + ") haven't been found")
      }
    }

    ConfigFactory.load(defaultConfig)
  }

}



