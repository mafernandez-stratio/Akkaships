package es.codemotion.akkaships.client

import SceneRenderer.{MoveCursor, HideCursor}
import es.codemotion.akkaships.common.domain._


import scala.concurrent.duration._

import akka.actor.{Props, Cancellable, Actor}

object UserActor {

  val props: Props = Props(new UserActor(new SceneRenderer(Size(20, 50))))

  case class BoardUpdate(elements: Seq[BoardEntity])
  case object SyncInput

  case class State(cursor: Option[Position])
  val initialState = State(None)

}

class UserActor(scene: SceneRenderer) extends Actor {

  val inputPollPeriod = 150 milliseconds
  var syncSched: Option[Cancellable] = None

  import UserActor._

  def behaviour(st: State = initialState): Receive = {
    case BoardUpdate(els) => scene.paintBoard(els, st.cursor)
    case SyncInput =>
      val commands = scene.getCommands
      if(commands contains HideCursor) {
        scene.hideCursor
        context.become(behaviour(State(None)))
      }
      else commands collect { case MoveCursor(pos) =>
        val newPos = st.cursor.getOrElse(Position(0,0)) + pos
        scene.moveCursor(newPos)
        State(Some(newPos))
      } foreach { ns => context.become(behaviour(ns)) }
  }

  override def receive: Receive = behaviour(State(None))

  override def postStop(): Unit = {
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
