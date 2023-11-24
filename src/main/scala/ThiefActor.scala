//import akka.actor.{Actor, ActorRef, Props}
//
//// Thief actor
//class ThiefActor(server: ActorRef) extends Actor {
//  override def receive: Receive = {
//    case "Start" =>
//      println("Thief: Starting the game.")
//
//      // Send a message to the server
//      server ! "Thief"
//    case _ =>
//      println("Thief: Unknown message received.")
//  }
//}
//
//object ThiefActor {
//  def props(server: ActorRef): Props = Props(new ThiefActor(server))
//}


import akka.actor.{Actor, ActorRef, Props}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpRequest}

class ThiefActor(gameServer: ActorRef) extends Actor {
  override def receive: Receive = {
    case "Start" =>
      println("Thief: Starting the game.")
      // Example of sending an HTTP POST request to make a move
      val moveJson = """{"player": "thief", "move": "steal"}"""
      gameServer.tell(HttpRequest(entity = HttpEntity(ContentTypes.`application/json`, moveJson)), self)

    case _ =>
      println("Thief: Unknown message received.")
  }
}

object ThiefActor {
  def props(gameServer: ActorRef): Props = Props(new ThiefActor(gameServer))
}

