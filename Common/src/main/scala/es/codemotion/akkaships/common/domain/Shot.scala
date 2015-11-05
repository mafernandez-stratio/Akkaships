package es.codemotion.akkaships.common.domain

case class Shot(override val pos: Position) extends BoardEntity(pos)
