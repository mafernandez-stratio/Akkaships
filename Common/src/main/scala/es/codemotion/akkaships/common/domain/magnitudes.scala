package es.codemotion.akkaships.common.domain

import Math.{abs, pow, sqrt}

trait Vect {
  val x: Int
  val y: Int
}

case class Position(i: Int, j: Int) extends Vect {
  override val x = j
  override val y = i

  def euclidDistance(that: Position): Double = sqrt(pow(that.i-i, 2.0) + pow(that.j-j, 2.0))
  def distanceFrom(that: Position): Int = abs(that.j - j)::abs(that.i - i)::euclidDistance(that).toInt::Nil max
  def +(that: Position) = Position(i + that.i, j + that.j)
}
case class Size(n: Int, m: Int) extends Vect {
  override val x = n
  override val y = m
  def contains(pos: Position): Boolean = (productIterator zip pos.productIterator) forall {
      case (a: Int, b: Int) => b >= 0 && b < a
  }
}

trait Orientation
case object Vertical extends Orientation
case object Horizontal extends Orientation
