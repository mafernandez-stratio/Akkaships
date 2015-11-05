package es.codemotion.akkaships.client

import com.googlecode.lanterna.TextColor.{RGB, ANSI}
import com.googlecode.lanterna.input._
import com.googlecode.lanterna.terminal.{DefaultTerminalFactory, Terminal}

import es.codemotion.akkaships.common.domain._

object SceneRenderer {
  trait Command
  case class MoveCursor(delta: Position) extends Command
  case object HideCursor extends Command
}

class SceneRenderer(val size: Size) {

  import SceneRenderer._

  val term: Terminal = {
    val factory = new DefaultTerminalFactory()
    factory.setSuppressSwingTerminalFrame(true)
    factory.createTerminal()
  }


  def clearBoard(refresh: Boolean = true): Unit = {
    term.clearScreen()
    term.setCursorVisible(false)
    for(i <- 1 to size.n; j <- 1 to size.m) {
      term.setCursorPosition(j, i)
      term.setBackgroundColor(ANSI.BLUE)
      term.putCharacter(' ')
    }
    if(refresh) term.flush()
  }

  def paintBoard(elements: Seq[BoardEntity], cursor: Option[Position] = None): Unit = {
    val shotPositionSet = elements collect { case Shot(pos) => pos }
    val ships: Seq[Ship] = elements collect { case s: Ship => s }
    clearBoard(false)
    for(ship <- ships; pos <- ship) {
      term.setCursorPosition(pos.x, pos.y)
      val (c, fcolor, bcolor) = ship match {
        case Ship(_, _, _, true) => ('*', new RGB(5, 5, 5), ANSI.BLUE)
        case _: Ship if shotPositionSet contains pos => ('#', ANSI.YELLOW, ANSI.RED)
        case _ => ('#', ANSI.BLACK, ANSI.WHITE)
      }
      term.setForegroundColor(fcolor)
      term.setBackgroundColor(bcolor)
      term.putCharacter(c)
    }
    term.setCursorVisible(false)
    cursor.foreach(moveCursor(_))
    term.flush()
  }

  def moveCursor(pos: Position): Unit = {
    val visible = size contains pos
    if(visible) term.setCursorPosition(pos.x, pos.y)
    term.setCursorVisible(visible)
  }

  def hideCursor: Unit = term.setCursorVisible(false)

  def getCommands: Set[Command] = {
    val pollIt = new Iterator[KeyStroke] {
      private var buff: Option[KeyStroke] = None
      override def hasNext: Boolean = { if(buff.isEmpty) buff = Option(term.pollInput); buff.isDefined }
      override def next(): KeyStroke = if(hasNext) { val r = buff.get; buff = None; r} else throw new NoSuchElementException
    }
    val strokesByType: Map[KeyType, List[KeyStroke]] = pollIt.toList.groupBy { key =>
      if(
        Set(KeyType.ArrowUp, KeyType.ArrowDown,
          KeyType.ArrowLeft, KeyType.ArrowRight
        ) contains key.getKeyType) KeyType.ArrowUp
      else key.getKeyType
    }
    strokesByType collect {
      case (KeyType.ArrowUp, keys) =>
        val pos = (Position(0,0) /: keys) {
          case (prev, key: KeyStroke) => prev + {
            key.getKeyType match {
              case KeyType.ArrowUp => Position(-1, 0)
              case KeyType.ArrowDown => Position(1, 0)
              case KeyType.ArrowLeft => Position(0, -1)
              case KeyType.ArrowRight => Position(0, 1)
            }
          }
        }
        MoveCursor(pos)
      case (KeyType.Escape, _) => HideCursor
    } toSet
  }
}
