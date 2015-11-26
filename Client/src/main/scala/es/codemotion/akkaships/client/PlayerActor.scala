package es.codemotion.akkaships.client

import java.io.File
import javax.sound.midi.{Sequencer, MidiSystem}

import akka.actor._
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import es.codemotion.akkaships.client.SceneRenderer.{Fire, HideCursor, MoveCursor}
import es.codemotion.akkaships.common.domain._

import scala.concurrent.duration._

object PlayerActor {

  def props(gameSrv: ActorSelection): Props = Props(new PlayerActor(gameSrv, new SceneRenderer(Size(20, 25))))

  //Mensajes Propios del Usuario
  case object SyncInput
  case class ShowTextMessage(msg: String)
  case object ClearTextArea

  case class State(cursor: Option[Position], boardSize: Size) {
    def isValid: Boolean = cursor forall (boardSize contains _)
  }

  def initialState(boardSize: Size) = State(None, boardSize)
}

class PlayerActor(gameServer: ActorSelection, scene: SceneRenderer) extends Actor with ActorLogging {

  val inputPollPeriod = 150 milliseconds
  var syncSched: Option[Cancellable] = None
  val cluster = Cluster(context.system)

  import PlayerActor._

  def behaviour(st: State = initialState(scene.size)): Receive = {
    case BoardUpdate(els) => {
      scene.paintBoard(els, st.cursor)
    }

    case SyncInput =>
      val commands = scene.getCommands
      if (commands contains HideCursor) {
        scene.hideCursor
        context.become(behaviour(State(None, scene.size)))
      } else commands collect {
        case MoveCursor(pos) =>
          val newPos = st.cursor.getOrElse(Position(0, 0)) + pos
          Some(State(Some(newPos), scene.size)) collect {
            case st if (st.isValid) =>
              scene.moveCursor(newPos)
              st
          } getOrElse st
        case Fire =>
          st.cursor foreach (pos=> gameServer ! Shot(pos))
          st
      } foreach { ns => context.become(behaviour(ns)) }
    case ClearTextArea => scene.clearMessage
    case ShowTextMessage(msg) => scene.showMessage(msg)
    case FinishBattle =>
      scene.showMessage("PARTIDA FINALIZADA!!!")
    case SunkMessage(msg) =>
      scene.showMessage(msg)
    case ScoreResult(results) =>
      scene.showScore(results)

  }

  override def receive: Receive = behaviour(initialState(scene.size))

  override def postStop(): Unit = {
    scene.clearBoard(true)
    cluster.unsubscribe(self)
    syncSched.foreach(_.cancel())
    super.postStop()
  }

  override def preStart(): Unit = {
    super.preStart()
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents, classOf[MemberEvent], classOf[UnreachableMember])
    // Sincronizacion para que cada lapso de tiempo se refresque el estado con lo que se inserta por teclado
    syncSched = Some(context.system.scheduler.schedule(inputPollPeriod, inputPollPeriod, self, SyncInput)(
      context.system.dispatcher
    ))
  }


  def music(midi : String): Sequencer = {
    val url = getClass.getResource(s"/$midi")
    val midiFile = new File(url.getPath)
    val song = MidiSystem.getSequence(midiFile)
    val midiPlayer = MidiSystem.getSequencer()
    midiPlayer.open()
    midiPlayer.setSequence(song)
    midiPlayer.setLoopCount(0) // repeat 0 times (play once)
    midiPlayer.start()
    midiPlayer
  }

}
