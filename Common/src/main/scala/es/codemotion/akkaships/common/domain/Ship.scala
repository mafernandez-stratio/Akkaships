package es.codemotion.akkaships.common.domain

case object Ship {
  class SIterator(val ship: Ship) extends Iterator[Position] {

    private val horizontalExpansion: Position => Position = p => p.copy(j = p.j+1)
    private val verticalExpansion: Position => Position = p => p.copy(i = p.i+1)
    override def hasNext: Boolean = (p distanceFrom ship.pos) < ship.length
    override def next(): Position = if(!hasNext) throw new IndexOutOfBoundsException else {
      val ret = expansion(p)
      p = ret
      ret
    }

    private var p: Position = ship.pos
    private val expansion: Position => Position =
      if(ship.orientation == Horizontal) horizontalExpansion else verticalExpansion
  }
}

case class Ship(override val pos: Position, orientation: Orientation, length: Int,
                sunk: Boolean = false) extends BoardEntity(pos) with Iterable[Position] {
  import es.codemotion.akkaships.common.domain.Ship._
  override def iterator: Iterator[Position] = new SIterator(this)

  def hit(hitPos: Position): Boolean = {
    (orientation == Horizontal && pos.i == hitPos.i && pos.j <= hitPos.j && hitPos.j < pos.j+length) &&
      (orientation == Vertical && pos.j == hitPos.j && pos.i <= hitPos.i && hitPos.i < pos.i+length)
  }

}
