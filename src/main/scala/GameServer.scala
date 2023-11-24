import NetGraphAlgebraDefs.{NetGraph, NodeObject}
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.http.scaladsl.model._
import akka.stream.{ActorMaterializer, Materializer}
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.DurationInt

class GameServer(netOriginalGraph: NetGraph, netPerturbedGraph: NetGraph, implicit val materializer: Materializer) extends Actor {

  // I have made these global to be able to keep track
  var originalGraph: Option[NetGraph] = None // Original Graph
  var perturbedGraph: Option[NetGraph] = None // Perturbed Graph

  var nodeSelector: RandomNodeSelector = null.asInstanceOf[RandomNodeSelector] // ranodm generator

  val connectedNodes = new FindConnectedNodes() // instance of the class connected nodes
  var policePossibleNodes: List[NodeObject] = List.empty[NodeObject] // connected nodes will be held here for police
  var thiefPossibleNode: List[NodeObject] = List.empty[NodeObject] // connected nodes will be held here for thief

  var perturbedPolicePossibelNode: List[NodeObject] = List.empty[NodeObject]
  var perturbedThiefPossibelNode: List[NodeObject] = List.empty[NodeObject]

  var currentPoliceNode: NodeObject = _
  var currentThiefNode: NodeObject = _

  var turns: Boolean = false // this is to make sure thief and police take turns going back and forth




  override def receive: Receive = {

    // This is to start the game. Its common call from terminal. Either Police or Thief can do it
    // Upon triggering startGame, following will happen:
    // 1. Thief and Police both will be assigned a random node in the graph
    //     - Those two nodes will not be connected to each other initially and will not be the same
    // 2. Both thief and police will be provided with the possible nodes that they can move to
    case HttpRequest(HttpMethods.POST, Uri.Path("/startGame"), _, _, _) =>


      // we create an instance of random Node Selector class
      // We call this to generate 2 random nodes on graph to assign it to thief and police
      nodeSelector = new RandomNodeSelector(netOriginalGraph)
      val (randomNode1, randomNode2) = nodeSelector.getRandomNodes

      // We store the adjacent nodes from original graph for both police and thief's current nodes
      policePossibleNodes = connectedNodes.getVNodesForSource(netOriginalGraph, randomNode1)
      thiefPossibleNode = connectedNodes.getVNodesForSource(netOriginalGraph, randomNode2)

      // We store the adjacent nodes from perturbed graph for both police and thief's current nodes
      perturbedPolicePossibelNode = connectedNodes.getVNodesForSource(netPerturbedGraph, randomNode1)
      perturbedThiefPossibelNode = connectedNodes.getVNodesForSource(netPerturbedGraph, randomNode2)

      currentPoliceNode = randomNode1
      currentThiefNode = randomNode2


      // Then we check if the node it has moved to is valuable data or not
      val valuableNodeChecker = new ValuableNodeChecker(netPerturbedGraph) // create an instance of the class
      val someNode: NodeObject = currentThiefNode
      val isValuable = valuableNodeChecker.isValuableNode(someNode) // Check if valuable data was found

      // Game ends and Thief wins if thief ends up on a valuable data
      if (isValuable) {
        sender() ! HttpResponse(StatusCodes.OK, entity = "Thief Won! Valuable Data was found, thief successfully escaped")
      }


      // If police cannot move anywhere (Such as no nodes), it loses!
      if (policePossibleNodes.isEmpty) {
        sender() ! HttpResponse(StatusCodes.OK, entity = "Officer Lost! No possible nodes to move to.")
      }

      // If thief cannot move anywhere (Such as no nodes), it loses!
      if (thiefPossibleNode.isEmpty) {
        sender() ! HttpResponse(StatusCodes.OK, entity = "Thief Lost! No possible nodes to move to.")
      }


      // Sending HTTP Response
      sender() ! HttpResponse(
        StatusCodes.OK, entity =
          "\nGame Started!\n\nPolice Officer, take position you are on Node: " + currentPoliceNode.id + "\n" +
            "Thief make your escape you are on Node: " + currentThiefNode.id + "\n\n" +
            "Police Officer goes first!\n" +
            "Officer: Pick one of the possible nodes to move to from the following: " +
            policePossibleNodes.map(_.id).mkString(" Or ") + "\n\n"

      )

//--------------------------------------Start Game Code ends here-------------------------------------------


    // This will execute when police officer tries to move
    case HttpRequest(HttpMethods.POST, Uri.Path("/police/makeMove"), _, entity, _) =>

      if(!turns) {

        val strictEntity = Await.result(entity.toStrict(3.seconds), 3.seconds)
        val moveNumberStr = strictEntity.data.utf8String


        try {
          val moveNumber = moveNumberStr

          // instance used to check if it exists as adajacent node in perturbed graph
          val moveValidator = new MoveValidator(policePossibleNodes)

          // instance used to check if it exists as adajacent node in perturbed graph
          val perturbedMoveValidator = new PerturbedMoveValidator(perturbedPolicePossibelNode)


          // This will check if the move exists in perturbed but not in original --> then thief loses
          (moveValidator.isLegalMove(moveNumber), perturbedMoveValidator.existsInPerturbed(moveNumber)) match {
            case (None, Some(perturbedNode)) =>
              sender() ! HttpResponse(StatusCodes.OK, entity = "Police Officer Lost! Move only exists in Perturbed Graph not Original Graph!\n")
            // Otherwise the game continues to check further and play along
            case _ =>


              moveValidator.isLegalMove(moveNumber) match {

                // First we make sure the police officer is moving to a connected node from the options given to him/her
                case Some(legalNode) =>

                  turns = true // Only thief can go next since police turn is over

                  // Update the current police node to the new moved node
                  currentPoliceNode = legalNode

                  // Now we check if the current node is same as Thief Node
                  if (currentPoliceNode == currentThiefNode) {
                    sender() ! HttpResponse(
                      StatusCodes.OK, entity = s"Police Office Won! The thief was successfully arrested!\n\n" +
                        "Start New Game\n\n"
                    )
                  }

                  // Storing the new connected nodes availale from officer's new position
                  policePossibleNodes = connectedNodes.getVNodesForSource(netOriginalGraph, currentPoliceNode)
                  perturbedPolicePossibelNode = connectedNodes.getVNodesForSource(netPerturbedGraph, currentPoliceNode)

                  if (policePossibleNodes.isEmpty) {
                    sender() ! HttpResponse(StatusCodes.OK, entity = "Officer Lost! No possible nodes to move to.")
                  }


                  // Thief was not caught, police officer was successfully moved
                  // Now It will be thief's turn
                  sender() ! HttpResponse(
                    StatusCodes.OK, entity = s"\nPolice Officer successfully moved to ${legalNode.id}\n\n" +
                      s"Thief's turn is next! Thief is currently on Node ${currentThiefNode.id}\n" +
                      "Thief: Pick one of the possible nodes to move to from the following: " +
                      thiefPossibleNode.map(_.id).mkString(" Or ") + "\n\n"
                  )

                // The police officer chose a node that is not connected
                case None =>
                  sender() ! HttpResponse(StatusCodes.BadRequest, entity = "You cannot move there, it is not connected. Please Try Again!\n")
              }
          }

        } catch {
          case _: NumberFormatException =>
            sender() ! HttpResponse(StatusCodes.BadRequest, entity = "Invalid move number format.")
        }
      }else{
        sender() ! HttpResponse(StatusCodes.BadRequest, entity = "It is not your turn. Thief's turn is next!")
      }

  //--------------------------------------Police Move Code ends here-------------------------------------------

    // This will execute when thief tries to make a move
    case HttpRequest(HttpMethods.POST, Uri.Path("/thief/makeMove"), _, entity, _) =>

      if(turns) {
        val strictEntity = Await.result(entity.toStrict(3.seconds), 3.seconds)
        val moveNumberStr = strictEntity.data.utf8String


        try {
          val moveNumber = moveNumberStr

          // instance used to check if it exists as adajacent node in original graph
          val moveValidator = new MoveValidator(thiefPossibleNode)

          // instance used to check if it exists as adajacent node in perturbed graph
          val perturbedMoveValidator = new PerturbedMoveValidator(perturbedThiefPossibelNode)


          // This will check if the move exists in perturbed but not in original --> then thief loses
          (moveValidator.isLegalMove(moveNumber), perturbedMoveValidator.existsInPerturbed(moveNumber)) match {
            case (None, Some(perturbedNode)) =>
              sender() ! HttpResponse(StatusCodes.OK, entity = "Thief Lost! Move only exists in Perturbed Graph not Original Graph!\n")
            case _ =>


              moveValidator.isLegalMove(moveNumber) match {


                // First we make sure the thief is moving to a connected node from the options given to him/her
                case Some(legalNode) =>

                  turns = false // Only police can go next since thief turn is over

                  // Update the current police node to the new moved node
                  currentThiefNode = legalNode

                  // If thief moves onto the same node as police officer, it automatically loses
                  if (currentThiefNode == currentPoliceNode) {
                    sender() ! HttpResponse(
                      StatusCodes.OK, entity = s"Thief moved onto the same node as Police Officer!" +
                        "Police Office Won! The thief was successfully arrested!\n\n" +
                        "Start New Game\n\n"
                    )
                  }

                  // Then we check if the node it has moved to is valuable data or not
                  val valuableNodeChecker = new ValuableNodeChecker(netPerturbedGraph) // create an instance of the class
                  val someNode: NodeObject = currentThiefNode
                  val isValuable = valuableNodeChecker.isValuableNode(someNode) // Check if valuable data was found

                  // Game ends and Thief wins if thief ends up on a valuable data
                  if (isValuable) {
                    sender() ! HttpResponse(StatusCodes.OK, entity = "Thief Won! Valuable Data was found, thief successfully escaped")
                  }

                  // Storing the new connected nodes availale from officer's new position
                  thiefPossibleNode = connectedNodes.getVNodesForSource(netOriginalGraph, currentThiefNode)
                  perturbedThiefPossibelNode = connectedNodes.getVNodesForSource(netPerturbedGraph, currentThiefNode)

                  if (thiefPossibleNode.isEmpty) {
                    sender() ! HttpResponse(StatusCodes.OK, entity = "Thief Lost! No possible nodes to move to.")
                  }


                  // Thief was successsfully moved
                  // Now It will be police officer's turn
                  sender() ! HttpResponse(
                    StatusCodes.OK, entity = s"\nThief successfully moved to ${currentThiefNode.id}\n\n" +
                      s"Police Officer's turn is next! Police Officer is currently on Node ${currentPoliceNode.id}\n" +
                      "Police Officer: Pick one of the possible nodes to move to from the following: " +
                      policePossibleNodes.map(_.id).mkString(" Or ") + "\n\n"
                  )


                // The thief chose a node that is not connected to its current node
                case None =>
                  sender() ! HttpResponse(StatusCodes.BadRequest, entity = "You cannot move there, it is not connected. Please Try Again!\n")
              }
          }
        } catch {
          case _: NumberFormatException =>
            sender() ! HttpResponse(StatusCodes.BadRequest, entity = "Invalid move number format.")
        }
      }else{
        sender() ! HttpResponse(StatusCodes.BadRequest, entity = "It is not your turn. Police Officer's turn is next!")
      }

  //----------------------------------Thief Move Code ends here-------------------------------------------


    case HttpRequest(HttpMethods.GET, Uri.Path("/gameState"), _, _, _) =>

      val getQuery = new Query(netOriginalGraph, netPerturbedGraph)
      val valuableDataNodePath = getQuery.findClosestValuableDataNode(currentThiefNode)
      val policeToThiefPath = getQuery.findThiefLocation(currentPoliceNode, currentThiefNode)

      val valuableNodePath = valuableDataNodePath match {
        case Some(path) =>
          s"Path to Node with Valuable Data is as follows from Thief's Current Position:\n$path\n"
        case None =>
          "There is no path to Valuable Data found"
      }

      val pathToThief = policeToThiefPath match {
        case Some(path) =>
          s"Path to the Thief is as follows from Police Officer's Current Position:\n$path\n"
        case None =>
          "There is no path to getting to Thief"
      }


      sender() ! HttpResponse(StatusCodes.OK, entity = valuableNodePath + "\n" + pathToThief)



    case HttpRequest(HttpMethods.POST, Uri.Path("/resetGame"), _, _, _) =>
      // Reset the game to its initial state
      // ...

      // Respond with a confirmation or new game state
      sender() ! HttpResponse(StatusCodes.OK, entity = "Game reset.")

    case _ =>
      // Handle other HTTP requests or unknown paths
      sender() ! HttpResponse(StatusCodes.NotFound, entity = "Not Found.")
  }
}


object GameServer {
  def props(netOriginalGraph: NetGraph, netPerturbedGraph: NetGraph)(implicit materializer: Materializer): Props =
    Props(new GameServer(netOriginalGraph, netPerturbedGraph, materializer))

}