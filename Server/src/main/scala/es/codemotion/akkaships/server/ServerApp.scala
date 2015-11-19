package es.codemotion.akkaships.server

object ServerApp extends App {

  val akkaShipsServer = new Server

  akkaShipsServer.init(null)
  akkaShipsServer.start()

  while (!allShipsSunk()){

  }
  akkaShipsServer.stop()
  akkaShipsServer.destroy()



  def allShipsSunk():Boolean = false



  // Create an actor that handles cluster domain events
  //val server = system.actorOf(Props[Server], name = "AkkashipsServer")

}
