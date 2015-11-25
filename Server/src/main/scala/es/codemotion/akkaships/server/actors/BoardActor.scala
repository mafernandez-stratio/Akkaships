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
    case Ship(pos, orientation, length, sunk) =>

      sunks += 1
      if (sunks == shipsNumber) {
        if (playersAlive.nonEmpty)
          playersAlive.foreach(member => context.actorSelection(RootActorPath(member.address) / "user" / "playerActor")
            ! FinishBattle)
        statisticsActor ! Score
      }

    case Shot(pos) =>
      log.info("-------------------> BoardActor gets a shot message")
      boardElements :::= Shot(pos) :: Nil
      if (playersAlive.nonEmpty)
        playersAlive.foreach(member => context.actorSelection(RootActorPath(member.address) / "user" / "playerActor")
          ! BoardUpdate(boardElements))
      log.info("BoardActor Elements --------> {}", boardElements)
    case Water(w) =>
      log.info("-------------------> BoardActor gets a water message")
      if (playersAlive.nonEmpty)
        playersAlive.foreach(member => context.actorSelection(RootActorPath(member.address) / "user" / "playerActor")
          ! BoardUpdate(boardElements))
    case MemberUp(member) =>
      log.info("Member is Up: {} of type {}", member.address, member.roles.toString())
      if (member.roles.contains("player")) {
        playersAlive :::= member :: Nil
        statisticsActor ! NewPlayer(member.address.toString)
      }


    case UnreachableMember(member) =>
      log.info("Member detected as unreachable: {} of type {}", member.address, member.roles.toString())
      if (member.roles.contains("player"))
        playersAlive=playersAlive.filter(_!=member)

    case MemberRemoved(member, previousStatus) =>
      log.info("Member is Removed: {} after {} of type {}", member.address, previousStatus, member.roles.toString())
      if (member.roles.contains("player"))
        playersAlive=playersAlive.filter(_!=member)

    case _: MemberEvent => // ignore

    case ScoreResult(results)=>
      log.info(results)
      playersAlive.foreach(member => context.actorSelection(RootActorPath(member.address) / "user" / "playerActor")
        ! ScoreResult(results))

  }

  override def preStart(): Unit = {
    super.preStart()
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents, classOf[MemberEvent], classOf[UnreachableMember])
  }

  override def postStop(): Unit = {
    cluster.unsubscribe(self)
    super.postStop()
  }



}
