package es.codemotion.akkaships.server.actors

import akka.actor._
import akka.cluster.ClusterEvent._
import akka.cluster.{Cluster, Member}
import es.codemotion.akkaships.common.domain._

object BoardActor {
  def props(shipsNumber:Int,statisticsActor:ActorRef): Props = Props(new BoardActor(shipsNumber,statisticsActor))
  case object SyncInput
}

class BoardActor(val shipsNumber:Int, val statisticsActor:ActorRef) extends Actor with ActorLogging{
  val cluster = Cluster(context.system)
  var playersAlive=List[Member]()
  var boardElements=List[BoardEntity]()
  var sunks=0

  def receive = {

    case MemberUp(member) =>
      ???

    case UnreachableMember(member) =>
      ???

    case MemberRemoved(member, previousStatus) =>
      ???
    case _: MemberEvent => // ignore

  }

  override def preStart(): Unit = {
    super.preStart()
    // Cluster Subscription

  }

  override def postStop(): Unit = {
    //Cluster unsubscribe

    super.postStop()
  }



}
