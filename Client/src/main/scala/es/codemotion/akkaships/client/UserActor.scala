package es.codemotion.akkaships.client

import es.codemotion.akkaships.client.SceneRenderer.{Fire, MoveCursor, HideCursor}
import es.codemotion.akkaships.common.domain._


import scala.concurrent.duration._

import akka.actor.{ActorRef, Props, Cancellable, Actor}

object UserActor {

  def props(gameSrv: ActorRef): Props = Props(new UserActor(gameSrv, new SceneRenderer(Size(20, 50))))

  //Messages
  case class BoardUpdate(elements: Seq[BoardEntity])
  case object SyncInput
  case class ShowTextMessage(msg: String)
  case object ClearTextArea

  case class State(cursor: Option[Position], boardSize: Size) {
    def isValid: Boolean = cursor forall(boardSize contains _)
  }

  def initialState(boardSize: Size) = State(None, boardSize)

}

class UserActor(gameServer: ActorRef, scene: SceneRenderer) extends Actor {

  val inputPollPeriod = 150 milliseconds
  var syncSched: Option[Cancellable] = None

  import UserActor._

  def behaviour(st: State = initialState(scene.size)): Receive = {
    case BoardUpdate(els) => scene.paintBoard(els, st.cursor)
    case SyncInput =>
      val commands = scene.getCommands
      if(commands contains HideCursor) {
        scene.hideCursor
        context.become(behaviour(State(None, scene.size)))
      } else commands collect {
        case MoveCursor(pos) =>
          val newPos = st.cursor.getOrElse(Position(0,0)) + pos
          Some(State(Some(newPos), scene.size)) collect {
            case st if(st.isValid) =>
              scene.moveCursor(newPos)
              st
          } getOrElse st
        case Fire =>
          st.cursor foreach(gameServer ! Shot(_))
          st
      } foreach { ns => context.become(behaviour(ns)) }
    case ClearTextArea => scene.clearMessage
    case ShowTextMessage(msg) => scene.showMessage(msg)
  }

  override def receive: Receive = behaviour(initialState(scene.size))

  override def postStop(): Unit = {
    scene.clearBoard(true)
    syncSched.foreach(_.cancel())
    super.postStop()
  }

  override def preStart(): Unit = {
    super.preStart()
    syncSched = Some(context.system.scheduler.schedule(inputPollPeriod, inputPollPeriod, self, SyncInput)(
      context.system.dispatcher
    ))
  }

}
