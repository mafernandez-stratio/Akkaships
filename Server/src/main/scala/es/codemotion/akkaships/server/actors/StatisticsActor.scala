package es.codemotion.akkaships.server.actors

import akka.actor.{Actor, ActorLogging, Props}
import com.datastax.driver.core.Cluster
import es.codemotion.akkaships.common.domain.{NewPlayer, Score, ScoreResult, Statistics}

object StatisticsActor {
  def props(): Props = Props(new StatisticsActor())
}


class StatisticsActor extends Actor with ActorLogging {
  val cluster = Cluster.builder().addContactPoint("127.0.0.1").build()
  val session = cluster.newSession()
  var sunkScore = scala.collection.mutable.Map[String, Long]()
  var hitScore = scala.collection.mutable.Map[String, Long]()
  var usersList = List[String]()
  session.execute("TRUNCATE akkaships.users")
  session.execute("TRUNCATE akkaships.statistics")

  def getResults(): String = {
    val users = session.execute(s"select * from akkaships.users")
    val rows = users.all()

    for (a <- 0 to rows.size() - 1) {
      usersList :::= rows.get(a).getString(0) :: Nil
    }

    for (user <- usersList) {
      val resSunk = session.execute(s"SELECT count(*) FROM akkaships.statistics WHERE user='$user' AND hit=true AND" +
        s" sunk=TRUE ALLOW FILTERING").all()
      sunkScore(user) = resSunk.get(0).getLong(0)


      val resHit = session.execute(s"SELECT count(*) FROM akkaships.statistics WHERE user='$user' AND hit=true AND " +
        s"sunk=false ALLOW FILTERING").all()
      hitScore(user) = resHit.get(0).getLong(0)
    }

    var results= "AKKA SHIPS SCORE RESULTS \n============================= \n\n"
    results += "HIT RESULTS \n_______________\n\n"

    hitScore foreach {
      case (key, value) => {
        val akkaURL=key.split('@')
        results += s"${akkaURL(1)}  ==>  $value\n"
      }
    }

    results += "\nSUNK RESULTS \n_______________\n\n"
    sunkScore foreach {
      case (key, value) => {
        val akkaURL=key.split('@')
        results += s"${akkaURL(1)}  ==>  $value\n"
      }
    }
    results
  }

  def receive = {
    case Statistics(user, hit, sunk) =>
      session.execute(s"INSERT INTO akkaships.statistics (user,ts,hit,sunk) values('$user',now(),$hit,$sunk)")
    case NewPlayer(player) =>
      session.execute(s"INSERT INTO akkaships.users (user) values('$player')")
    case Score =>
      sender ! ScoreResult(getResults())
  }
}
