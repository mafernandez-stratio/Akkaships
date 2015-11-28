package es.codemotion.akkaships.client

object PlayerApp extends App {
  val akkaShipsPlayer = new PlayerInitialize()
  akkaShipsPlayer.initPlayer()
}
