package es.codemotion.akkaships.server

import akka.actor.{Actor, ActorLogging}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import es.codemotion.akkaships.common.domain.{Result, Shot}

class Server extends Actor with ActorLogging {

  val cluster = Cluster(context.system)

  // subscribe to cluster changes, re-subscribe when restart
  override def preStart(): Unit = {
    //#subscribe
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents,
      classOf[MemberEvent], classOf[UnreachableMember])
    //#subscribe
  }

  override
  def postStop(): Unit = cluster.unsubscribe(self)

  def receive = {
    case Shot(pos) =>
      log.info("Shot received from {}", sender.toString())
      sender ! Result("Well done!")
    case MemberUp(member) =>
      log.info("Member is Up: {}", member.address)
    case UnreachableMember(member) =>
      log.info("Member detected as unreachable: {}", member)
    case MemberRemoved(member, previousStatus) =>
      log.info("Member is Removed: {} after {}",
        member.address, previousStatus)
    case me: MemberEvent =>
      log.info("Unkown member event: {}", me)
  }

}
