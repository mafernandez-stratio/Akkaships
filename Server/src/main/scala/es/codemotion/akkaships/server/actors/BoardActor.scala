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
        /* ------------------------ MENSAJE DE RESULTADOS A STATISTIC ACTOR -------------------------------------*/
        if (playersAlive.nonEmpty) {
          /*----------------------- MENSAJE DE FIN DE PARTIDA A TODOS LOS PLAYERS -------------------------------*/
        }
      }

    case Shot(pos) =>
      log.info("-------------------> BoardActor gets a shot message")
      boardElements :::= Shot(pos) :: Nil
      if (playersAlive.nonEmpty) {
        playersAlive.foreach(member => context.actorSelection(RootActorPath(member.address) / "user" / "playerActor")
          ! BoardUpdate(boardElements))
      }

      /*-----------------IMPLEMENTAR RECIBIR MENSAJE DE SCORE RESULT QUE LLEGA DE STATISTIC ACTOR ----------------*/


    case MemberUp(member) =>
      if (member.roles.contains("player")) {
        playersAlive :::= member :: Nil
        statisticsActor ! NewPlayer(member.address.toString)
      }

    case UnreachableMember(member) =>
      if (member.roles.contains("player"))
        playersAlive=playersAlive.filter(_!=member)


    case MemberRemoved(member, previousStatus) =>
      if (member.roles.contains("player"))
        playersAlive=playersAlive.filter(_!=member)
    case _: MemberEvent => // ignore

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
