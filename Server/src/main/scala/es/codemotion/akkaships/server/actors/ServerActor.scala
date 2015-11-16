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
package es.codemotion.akkaships.server.actors

import akka.actor.{Actor, Props}
import akka.cluster.Cluster
import es.codemotion.akkaships.server.config.ShipsServerConfig
import org.apache.log4j.Logger


object ServerActor {
  def props(cluster: Cluster): Props = Props(new ServerActor(cluster))
}

class ServerActor(cluster: Cluster) extends Actor with ShipsServerConfig {

  override lazy val logger = Logger.getLogger(classOf[ServerActor])

  def receive: Receive = {

   case any =>
      logger.error(s"Something is going wrong!. Unknown message: $any")

  }

}