/**
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package es.codemotion.akkaships.client.config

import java.io.File

import com.typesafe.config.{Config, ConfigFactory}
import org.apache.log4j.Logger

object PlayerConfig {

  val PlayerConfigDefault = "player-reference.conf"
  val ParentConfigName = "player"
  val PlayerConfigResource = "external.config.resource"
  val PlayerConfigFile = "external.config.filename"
  val PlayerConfigHosts = "config.cluster.hosts"
  val ServerNode ="akka.cluster.seed-nodes"

}

trait PlayerConfig {

  import PlayerConfig._

  lazy val logger: Logger = ???

  val config: Config = {

    val defaultConfig = ConfigFactory.load(PlayerConfigDefault).getConfig(ParentConfigName)
    val configFile = defaultConfig.getString(PlayerConfigFile)
    val configResource = defaultConfig.getString(PlayerConfigResource)

    //Get the player-application.conf properties if exists in resources
    val configWithResource: Config = {
      val resource = PlayerConfig.getClass.getClassLoader.getResource(PlayerConfigResource)
      Option(resource).fold {
        logger.warn("User resource (" + configResource + ") haven't been found")
        val file = new File(configResource)
        if (file.exists()) {
          val userConfig = ConfigFactory.parseFile(file).getConfig(ParentConfigName)
          userConfig.withFallback(defaultConfig)
        } else {
          logger.warn("User file (" + configResource + ") haven't been found in classpath")
          defaultConfig
        }
      } { resTemp =>
        val userConfig = ConfigFactory.parseResources(PlayerConfigResource).getConfig(ParentConfigName)
        userConfig.withFallback(defaultConfig)
      }
    }

    //Get the user external player-application.conf properties if exists
    val finalConfig: Config = {
      if(configFile.isEmpty){
        configWithResource
      }else{
        val file = new File(configFile)
        if (file.exists()) {
          val userConfig = ConfigFactory.parseFile(file).getConfig(ParentConfigName)
          userConfig.withFallback(configWithResource)
        } else {
          logger.error("User file (" + configFile + ") haven't been found")
          configWithResource
        }
      }
    }

    ConfigFactory.load(finalConfig)
  }

}