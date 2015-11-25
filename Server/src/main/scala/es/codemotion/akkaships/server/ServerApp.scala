package es.codemotion.akkaships.server

object ServerApp extends App {

  val akkaShipsServer = new Server

  akkaShipsServer.init(null)
  akkaShipsServer.start()

  while (true){

  }
  akkaShipsServer.stop()
  akkaShipsServer.destroy()
}
