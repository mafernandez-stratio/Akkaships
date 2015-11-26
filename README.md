# Akkaships
Battleship game implemented in Akka. Training project for Codemotion 2015

## Requirements ##
[Apache Cassandra](http://www.apache.org/dyn/closer.lua/cassandra/2.1.11/apache-cassandra-2.1.11-bin.tar.gz)
 
To start cassandra: 

    > $CASSANDRA_HOME/bin/cassandra -f 

To start CQL Shell:
 
    > $CASSANDRA_HOME/bin/cqlsh

Requirement metadata:

    CREATE KEYSPACE IF NOT EXISTS akkaships WITH replication = {'class': 'SimpleStrategy', 'replication_factor' : 1}
    CREATE TABLE IF NOT EXISTS akkaships.users (user text, PRIMARY KEY((user)))
    CREATE TABLE IF NOT EXISTS akkaships.statistics (user text, ts timeuuid, hit boolean, sunk boolean, PRIMARY KEY((ts),user,hit,sunk))
    

## Execute Game mode localhost ##
First it is necessary to compile:

    mvn clean install

Now it's time to execute the server

    mvn exec:java -pl Server -Dexec.mainClass="es.codemotion.akkaships.server.ServerApp"
     
Once server is started it is possible to start the Player

    mvn exec:java -pl Client -Dexec.mainClass="es.codemotion.akkaships.client.PlayerApp"
    
    
## Configuration for a distributed environment ##
Player Configuration is in the file  $AKKASHIPS_HOME/Client/src/main/resources/player-reference.conf and there it's 
necessary to change ...
 
    player.akka.remote.netty.tcp.hostname = "[Your IP]"
    player.akka.cluster.seed-nodes = ["akka.tcp://ShipsServerCluster@[ServerIP]:61000"]


Server Configuration is in the file $AKKASHIPS_HOME/Server/src/main/resources/server-reference.conf and there it's 
necessary to change ...

    akkaships-server.akka.remote.netty.tcp.hostname = "[ServerIP]"
    akkaships-server.akka.cluster.seed-nodes = ["akka.tcp://ShipsServerCluster@[ServerIP]:61000"]
    
Now it's time to recompile and follow the steps of the previous steps