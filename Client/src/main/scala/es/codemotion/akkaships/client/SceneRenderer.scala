package es.codemotion.akkaships.client

import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.TextColor.{RGB, ANSI}
import com.googlecode.lanterna.input._
import com.googlecode.lanterna.terminal.{DefaultTerminalFactory, Terminal}

import es.codemotion.akkaships.common.domain._

object SceneRenderer {
  trait Command
  case class MoveCursor(delta: Position) extends Command
  case object HideCursor extends Command
  case object Fire extends Command

  //Customize board colours and symbols here.
  val defaultFgColor = ANSI.WHITE
  val defaultBgColor = ANSI.BLUE

  case class ThemeColor(fore: TextColor = defaultFgColor, back: TextColor = defaultBgColor)
  case class ThemeFeature(hitColor: ThemeColor = ThemeColor(), neutralColor: ThemeColor = ThemeColor(),
                          hitChar: Char = ' ', neutralChar: Char = ' ')

  object Theme {
    val water = ThemeFeature(hitChar = '~')
    val boat = ThemeFeature(ThemeColor(ANSI.YELLOW, ANSI.RED), ThemeColor(ANSI.BLACK, ANSI.WHITE), '#', '#')
    val wreck = ThemeFeature(ThemeColor(fore = ANSI.BLACK), ThemeColor(fore = ANSI.BLACK), '#', '#')

    val textColor = ThemeColor(back = ANSI.BLACK)
  }

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
    for(i <- 0 until size.n; j <- 0 until size.m) {
      term.setCursorPosition(j, i)
      term.setBackgroundColor(defaultBgColor)
      term.putCharacter(' ')
    }
    if(refresh) term.flush()
  }

  def showMessage(msg: String): Unit = {
    term.setCursorVisible(false)
    for(j <- 0 until size.m) {
      term.setBackgroundColor(Theme.textColor.back)
      term.setForegroundColor(Theme.textColor.fore)
      term.setCursorPosition(j, size.n)
      term.putCharacter(if(j < msg.length) msg(j) else ' ')
    }
  }

  def clearMessage: Unit = showMessage("")

  def paintBoard(elements: Seq[BoardEntity], cursor: Option[Position] = None): Unit = {
    val shotPositionSet = elements collect { case Shot(pos) => pos }
    val ships: Seq[Ship] = elements collect { case s: Ship => s }
    clearBoard(false)
    shotPositionSet foreach { pos =>
      term.setCursorPosition(pos.x, pos.y)
      term.setBackgroundColor(Theme.water.hitColor.back)
      term.setForegroundColor(Theme.water.hitColor.fore)
      term.putCharacter(Theme.water.hitChar)
    }
    for(ship <- ships; pos <- ship) {
      term.setCursorPosition(pos.x, pos.y)
      val (c, fcolor, bcolor) = ship match {
        case Ship(_, _, _, true) => (Theme.wreck.hitChar, Theme.wreck.hitColor.fore, Theme.wreck.hitColor.back)
        case _: Ship if shotPositionSet contains pos =>
          (Theme.boat.hitChar, Theme.boat.hitColor.fore, Theme.boat.hitColor.back)
        case _ =>
          (Theme.boat.neutralChar, Theme.boat.neutralColor.fore, Theme.boat.neutralColor.back)
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
      case (KeyType.Enter, _) => Fire
    } toSet
  }
}
