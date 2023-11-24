//import akka.actor.{ActorSystem, Props}
//
//// Main application object
//object GameApp extends App {
//  // Create the actor system
//  val system = ActorSystem("GameSystem")
//
//  // Create the server actor
//  val server = system.actorOf(GameServer.props, "server")
//
//  // Create the police actor
//  val police = system.actorOf(PoliceActor.props, "police")
//
//  // Create the thief actor
//  val thief = system.actorOf(ThiefActor.props(server), "thief")
//
//  // Start the game by sending a message to the thief
//  thief ! "Start"
//}


import akka.actor.{ActorRef, ActorSystem, Props}

object GameApp extends App {
  val system = ActorSystem("GameSystem")

  // Create the game server actor
//  val gameServer = system.actorOf(GameServer.props, "gameServer")
//
//  // Create other actors and start the game (e.g., PoliceActor and ThiefActor)
//  val police = system.actorOf(PoliceActor.props(gameServer), "police")
//  val thief = system.actorOf(ThiefActor.props(gameServer), "thief")

  // Send HTTP POST requests to make moves or query the game state
  // ...

  // Shut down the system when the program exits
  // system.terminate()
}
