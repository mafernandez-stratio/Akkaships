package es.codemotion.akkaships.common.domain

case class BoardUpdate(elements: Seq[BoardEntity])
case object FinishBattle
case class SunkMessage(message:String)
case class Statistics(user:String, hit:Boolean, sunk:Boolean)
case object Score
case class ScoreResult(results:String)
case class NewPlayer(player:String)


