//import akka.actor.{Actor, Props}
//
//// Police actor
//class PoliceActor extends Actor {
//  override def receive: Receive = {
//    case "Thief spotted!" =>
//      println("Police: Received alert. Investigating the area.")
//      // Send a message to the server
//      sender() ! "Police"
//    case _ =>
//      println("Police: Unknown message received.")
//  }
//}
//
//object PoliceActor {
//  def props: Props = Props(new PoliceActor)
//}


import akka.actor.{Actor, ActorRef, Props}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpRequest}

class PoliceActor(gameServer: ActorRef) extends Actor {

  override def receive: Receive = {

    case "Thief spotted!" =>
      println("Police: Received alert. Investigating the area.")

      // Example of sending an HTTP POST request to make a move
      val moveJson = """{"player": "police", "move": "investigate"}"""
      gameServer.tell(HttpRequest(entity = HttpEntity(ContentTypes.`application/json`, moveJson)), self)


    case "PrintHeyWorld" =>
      "Hey, World!"


    case _ =>
      println("Police: Unknown message received.")


  }
}

//def printHeyWorld(): String = {
//  "Hey, World!"
//}


object PoliceActor {
  def props(gameServer: ActorRef): Props = Props(new PoliceActor(gameServer))
}

