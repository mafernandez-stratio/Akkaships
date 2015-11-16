package es.codemotion.akkaships.server


object AkkaShipApplication extends App {

  val akkaShipServer = new ShipServer

  akkaShipServer.init(null)
  akkaShipServer.start()
  akkaShipServer.placeShips()
  while (!allShipsSunk()){
  }
  getStatistics()
  akkaShipServer.stop()
  akkaShipServer.destroy()


  private def allShipsSunk():Boolean={
    ???
  }

  private def getStatistics():String={
    ???
  }


}