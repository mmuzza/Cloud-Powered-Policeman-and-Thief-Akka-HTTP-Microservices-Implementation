import NetGraphAlgebraDefs.{NetGraph, NodeObject}
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.http.scaladsl.model._
import akka.stream.{ActorMaterializer, Materializer}
import org.yaml.snakeyaml.Yaml
import java.io.{File, FileWriter, IOException}
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.DurationInt


// This class plays the entire police & thief game
// It responds to oncoming http requests and based on each individual request it responds to it accordingly
class GameServer(netOriginalGraph: NetGraph, netPerturbedGraph: NetGraph, yamlFilePath: String, implicit val materializer: Materializer) extends Actor {

  // I have made these global to be able to keep track
  // var originalGraph: Option[NetGraph] = None // Original Graph
  // var perturbedGraph: Option[NetGraph] = None // Perturbed Graph

  var nodeSelector: RandomNodeSelector = null.asInstanceOf[RandomNodeSelector] // ranodm generator

  // instance of the class connected nodes
  // This is used to call the function getVNodesForSource to grab adjacent nodes in a list for a specific node object
  val connectedNodes = new FindConnectedNodes()

  // These two hold list of adjacent nodes for police and thief respectively in original graph
  // Constantly updated at game start and each move
  var policePossibleNodes: List[NodeObject] = List.empty[NodeObject] // connected nodes will be held here for police
  var thiefPossibleNode: List[NodeObject] = List.empty[NodeObject] // connected nodes will be held here for thief

  // These two hold list of adjacent nodes for police and thief respectively in perturbed graph
  // Constantly updated at game start and each move
  var perturbedPolicePossibelNode: List[NodeObject] = List.empty[NodeObject]
  var perturbedThiefPossibelNode: List[NodeObject] = List.empty[NodeObject]

  // These two hold the current node police and thief is on respectively. Constantly updated at game start and each move
  var currentPoliceNode: NodeObject = _
  var currentThiefNode: NodeObject = _

  // This we declare to gather the response to send to http as well as use it for the yaml file
  var responseData: String = _

  // This boolean is declared to keep track of order of turns for police and thief
  // This is updated everytime police or thief makes a move so they can alternate
  var turns: Boolean = false

  // This is the yaml file we create at this given directory in the arguments
  // This path must be configured in configuration file so the file is created at your local computer
  // This file is used to write responseData throughout the gameplay
//  private val yamlFile = new File("/Users/muzza/Desktop/response_data.yaml") // yamlFilePath
  private val yamlFile = new File(yamlFilePath)


  // This function opens the yaml file we created globally
  // It writes the response passed to it as parameters
  // Then it closes the file
  private def writeResponseToYaml(data: String): Unit = {
    val yaml = new Yaml()
    val writer = new FileWriter(yamlFile, true) // 'true' appends to the existing file
    try {
      writer.write(yaml.dump(data))
      println(s"YAML file updated at: ${yamlFile.getAbsolutePath}")
    } catch {
      case e: IOException =>
        println(s"Error writing to YAML file: ${e.getMessage}")
    } finally {
      writer.close()
    }
  }


  override def receive: Receive = {

    // This is to start the game. Its common call from terminal. Either Police or Thief can request to start the game
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

      // We assign the global variables that keep track of current position of police/thief to the random assigned values
      currentPoliceNode = randomNode1
      currentThiefNode = randomNode2


      // Then we check if the node it has moved to is valuable data or not
      val valuableNodeChecker = new ValuableNodeChecker(netPerturbedGraph) // create an instance of the class
      val someNode: NodeObject = currentThiefNode
      val isValuable = valuableNodeChecker.isValuableNode(someNode) // Check if valuable data was found

      // Game ends and Thief wins if thief ends up on a valuable data as soon as game starts
      // The game is automatically restarted due to it
      if (isValuable) {
        // 1. Store Response in responseData => 2. Store it in the yaml file => 3. Send it as HTTP response
        responseData = "Thief Won! Valuable Data was found, thief successfully escaped\n\nRestarting Game!"
        writeResponseToYaml(responseData)
        sender() ! HttpResponse(StatusCodes.OK, entity = responseData)
      }


      // If police officer cannot move anywhere (Such as no adjacent nodes available), Police Officer loses!
      if (policePossibleNodes.isEmpty) {
        // 1. Store Response in responseData => 2. Store it in the yaml file => 3. Send it as HTTP response
        responseData = "Officer Lost! No possible nodes to move to."
        writeResponseToYaml(responseData)
        sender() ! HttpResponse(StatusCodes.OK, entity = responseData)
      }

      // If thief cannot move anywhere (Such as no adjacent nodes available), Thief loses!
      if (thiefPossibleNode.isEmpty) {
        // 1. Store Response in responseData => 2. Store it in the yaml file => 3. Send it as HTTP response
        responseData = "Thief Lost! No possible nodes to move to."
        writeResponseToYaml(responseData)
        sender() ! HttpResponse(StatusCodes.OK, entity = responseData)
      }

      // If all condition passes above, the game will continue:
      // Police Officer will be given the chance to go first always
      // Here it will be provided with its adjacent nodes so it can move accordingly

      // 1. Store Response in responseData => 2. Store it in the yaml file => 3. Send it as HTTP response
      responseData = "\nGame Started!\n\nPolice Officer, take position you are on Node: " + currentPoliceNode.id + "\n" +
        "Thief make your escape you are on Node: " + currentThiefNode.id + "\n\n" +
        "Police Officer goes first!\n" +
        "Officer: Pick one of the possible nodes to move to from the following: " +
        policePossibleNodes.map(_.id).mkString(" Or ") + "\n\n"
      writeResponseToYaml(responseData)
      sender() ! HttpResponse(StatusCodes.OK, entity = responseData)


//--------------------------------------Start Game Code ends here-------------------------------------------


    // This will execute when Police Officer tries to move from its current node position.
    case HttpRequest(HttpMethods.POST, Uri.Path("/police/makeMove"), _, entity, _) =>

      // First we check that it is Police Officer's turn
      if(!turns) {

        // We store the data being sent from HTTP request by user via terminal
        val strictEntity = Await.result(entity.toStrict(3.seconds), 3.seconds)
        val moveNumberStr = strictEntity.data.utf8String


        // Try block
        try {

          // We store the data into moveNumber sent by user which is the position they are trying to move to.
          val moveNumber = moveNumberStr

          // We store the adjacent nodes in moveValidator for the node that police officer is trying to move to
          // Original Graph
          val moveValidator = new MoveValidator(policePossibleNodes)

          // We store the adjacent nodes in perturbedMoveValidator for the node that police officer is trying to move to
          // Perturbed Graph
          val perturbedMoveValidator = new PerturbedMoveValidator(perturbedPolicePossibelNode)


          // Here we check if the node user trying to move to exists in perturbed and original
          (moveValidator.isLegalMove(moveNumber), perturbedMoveValidator.existsInPerturbed(moveNumber)) match {

            // This case checks that if the node exists in perturbed but does not exist in original then...
            // The police officer loses!
            case (None, Some(perturbedNode)) =>

              // 1. Store Response in responseData => 2. Store it in the yaml file => 3. Send it as HTTP response
              responseData = "Police Officer Lost! Move only exists in Perturbed Graph not Original Graph!\n"
              writeResponseToYaml(responseData)
              sender() ! HttpResponse(StatusCodes.OK, entity = responseData)

            // If that is not the case game goes on
            // This means the node may not exist in both perturbed and original
            // Or it may exist in both perturbed and original
            case _ =>

              // Therefore we check that the node Officer is trying to move to must exist in original graph
              moveValidator.isLegalMove(moveNumber) match {

                // Then we check to make sure the officer is moving to one of it's adjacent node that it was provided with
                case Some(legalNode) =>

                  // Legal & Successful move by Police Officer
                  // Therefore next turn must be thief so we set turns to true
                  turns = true

                  // Update the current police node location to the newly moved location
                  currentPoliceNode = legalNode

                  // Now we check if the current node is same as Thief Node
                  // If it is Police Officer wins the game and captures the thief
                  if (currentPoliceNode == currentThiefNode) {

                    // 1. Store Response in responseData => 2. Store it in the yaml file => 3. Send it as HTTP response
                    responseData = s"Police Officer Won! The thief was successfully arrested!\n\n" +
                      "Start New Game\n\n"
                    writeResponseToYaml(responseData)
                    sender() ! HttpResponse(StatusCodes.OK, entity = responseData)
                  }

                  // Updating adjacent nodes for newly moved Node Position by the officer for both original and perturbed
                  policePossibleNodes = connectedNodes.getVNodesForSource(netOriginalGraph, currentPoliceNode)
                  perturbedPolicePossibelNode = connectedNodes.getVNodesForSource(netPerturbedGraph, currentPoliceNode)

                  if (policePossibleNodes.isEmpty) {

                    // 1. Store Response in responseData => 2. Store it in the yaml file => 3. Send it as HTTP response
                    responseData = "Officer Lost! No possible nodes to move to."
                    writeResponseToYaml(responseData)
                    sender() ! HttpResponse(StatusCodes.OK, entity = responseData)
                  }


                  // Thief was not caught, police officer was successfully moved
                  // Now It will be thief's turn

                  // 1. Store Response in responseData => 2. Store it in the yaml file => 3. Send it as HTTP response
                  responseData = s"\nPolice Officer successfully moved to ${legalNode.id}\n\n" +
                    s"Thief's turn is next! Thief is currently on Node ${currentThiefNode.id}\n" +
                    "Thief: Pick one of the possible nodes to move to from the following: " +
                    thiefPossibleNode.map(_.id).mkString(" Or ") + "\n\n"
                  writeResponseToYaml(responseData)
                  sender() ! HttpResponse(StatusCodes.OK, entity = responseData)


                // The police officer chose a node to move to that is not connected/adjacent
                case None =>

                  // 1. Store Response in responseData => 2. Store it in the yaml file => 3. Send it as HTTP response
                  responseData = "You cannot move there, it is not connected. Please Try Again!\n"
                  writeResponseToYaml(responseData)
                  sender() ! HttpResponse(StatusCodes.BadRequest, entity = responseData)
              }
          }

        } catch { // try block fails --> exception is thrown --> Move was not a valid number
          case _: NumberFormatException =>

            // 1. Store Response in responseData => 2. Store it in the yaml file => 3. Send it as HTTP response
            responseData = "Invalid move number format."
            writeResponseToYaml(responseData)
            sender() ! HttpResponse(StatusCodes.BadRequest, entity = responseData)
        }
      }else{ // Turn boolean failed => It is thief's turn

        // 1. Store Response in responseData => 2. Store it in the yaml file => 3. Send it as HTTP response
        responseData = "It is not your turn. Thief's turn is next!"
        writeResponseToYaml(responseData)
        sender() ! HttpResponse(StatusCodes.BadRequest, entity = responseData)
      }

  //--------------------------------------Police Move Code ends here-------------------------------------------

    // This will executed when Thief tries to move from its current node position.
    case HttpRequest(HttpMethods.POST, Uri.Path("/thief/makeMove"), _, entity, _) =>

      // Check to make sure it is Thief's turn to make the move
      if(turns) {

        // Storing the Node/Move number thief is attempting to move to
        val strictEntity = Await.result(entity.toStrict(3.seconds), 3.seconds)
        val moveNumberStr = strictEntity.data.utf8String


        // Try block opens
        try {

          // Store moving node number into variable moveNumber
          val moveNumber = moveNumberStr

          // instance of class later used on to get adjacent nodes of thief from original graph
          val moveValidator = new MoveValidator(thiefPossibleNode)

          // instance of class later used on to get adjacent nodes of thief from perturbed graph
          val perturbedMoveValidator = new PerturbedMoveValidator(perturbedThiefPossibelNode)


          // Storing boolean values if the node attempting to move to exists in adjacent nodes for original and perturbed
          (moveValidator.isLegalMove(moveNumber), perturbedMoveValidator.existsInPerturbed(moveNumber)) match {

            // If moving to node does not exist is original graph but exists in perturbed => the thief loses
            case (None, Some(perturbedNode)) =>

              // 1. Store Response in responseData => 2. Store it in the yaml file => 3. Send it as HTTP response
              responseData = "Thief Lost! Move only exists in Perturbed Graph not Original Graph!\n"
              writeResponseToYaml(responseData)
              sender() ! HttpResponse(StatusCodes.BadRequest, entity = responseData)

            // Else the user move goes on
            // Meaning either the moving node does not exist in both original or perturbed graph
            // Or it exists in both original and perturbed
            case _ =>

              // Therefore we check if this node exists in original graph, which is important
              moveValidator.isLegalMove(moveNumber) match {


                // Then we make sure the thief is moving to a connected/adjacent node
                case Some(legalNode) =>

                  // Thief's new move was legal and successful
                  // We update turns boolean to false so next turn only police can go next
                  // Assuring that they are alternating
                  turns = false

                  // Update the current police node to the new moved node
                  currentThiefNode = legalNode

                  // If thief moves onto the same node as police officer, it automatically loses
                  // Police officer captures him/her
                  if (currentThiefNode == currentPoliceNode) {

                    // 1. Store Response in responseData => 2. Store it in the yaml file => 3. Send it as HTTP response
                    responseData = s"Thief moved onto the same node as Police Officer!" +
                      "Police Office Won! The thief was successfully arrested!\n\n" +
                      "Start New Game\n\n"
                    writeResponseToYaml(responseData)
                    sender() ! HttpResponse(StatusCodes.BadRequest, entity = responseData)

                  }

                  // If police does not capture him at that node
                  // We check if the node it has moved to is valuable data or not in perturbed graph
                  val valuableNodeChecker = new ValuableNodeChecker(netPerturbedGraph) // create an instance of the class
                  val someNode: NodeObject = currentThiefNode
                  val isValuable = valuableNodeChecker.isValuableNode(someNode) // Check if valuable data was found

                  // If the node thief chose to move to has valuable data in perturbed graph
                  // Thief automatically wins and escapes the officer
                  if (isValuable) {
                    // 1. Store Response in responseData => 2. Store it in the yaml file => 3. Send it as HTTP response
                    responseData = "Thief Won! Valuable Data was found, thief successfully escaped"
                    writeResponseToYaml(responseData)
                    sender() ! HttpResponse(StatusCodes.OK, entity = responseData)
                  }

                  // If node was not same as police, or valuable data... we proceed to
                  // Updating the new connected/adjacent nodes available from thief's new position
                  thiefPossibleNode = connectedNodes.getVNodesForSource(netOriginalGraph, currentThiefNode)
                  perturbedThiefPossibelNode = connectedNodes.getVNodesForSource(netPerturbedGraph, currentThiefNode)

                  // Then we check if there are any adjacent nodes to the new moved node
                  // If it is empty, the thief loses as it is stuck
                  if (thiefPossibleNode.isEmpty) {

                    // 1. Store Response in responseData => 2. Store it in the yaml file => 3. Send it as HTTP response
                    responseData = "Thief Lost! No possible nodes to move to."
                    writeResponseToYaml(responseData)
                    sender() ! HttpResponse(StatusCodes.BadRequest, entity = responseData)
                  }


                  // Thief was successfully moved
                  // Now It will be police officer's turn
                  // So we prepare the response by giving police it's options to move to

                  // 1. Store Response in responseData => 2. Store it in the yaml file => 3. Send it as HTTP response
                  responseData = s"\nThief successfully moved to ${currentThiefNode.id}\n\n" +
                    s"Police Officer's turn is next! Police Officer is currently on Node ${currentPoliceNode.id}\n" +
                    "Police Officer: Pick one of the possible nodes to move to from the following: " +
                    policePossibleNodes.map(_.id).mkString(" Or ") + "\n\n"
                  writeResponseToYaml(responseData)
                  sender() ! HttpResponse(StatusCodes.BadRequest, entity = responseData)


                // The thief chose a node that is not connected/adjacent to its current node
                case None =>

                  // 1. Store Response in responseData => 2. Store it in the yaml file => 3. Send it as HTTP response
                  responseData = "You cannot move there, it is not connected. Please Try Again!\n"
                  writeResponseToYaml(responseData)
                  sender() ! HttpResponse(StatusCodes.BadRequest, entity = responseData)
              }
          }
        } catch { // Try block failed, catch block is executed to throw invalid move number for node
          case _: NumberFormatException =>

            // 1. Store Response in responseData => 2. Store it in the yaml file => 3. Send it as HTTP response
            responseData = "Invalid move number format."
            writeResponseToYaml(responseData)
            sender() ! HttpResponse(StatusCodes.BadRequest, entity = responseData)
        }
      }else{ // It is police officer's turn, thief should wait

        // 1. Store Response in responseData => 2. Store it in the yaml file => 3. Send it as HTTP response
        responseData = "It is not your turn. Police Officer's turn is next!"
        writeResponseToYaml(responseData)
        sender() ! HttpResponse(StatusCodes.BadRequest, entity = responseData)
      }

  //----------------------------------Thief Move Code ends here-------------------------------------------

    // This will be executed when Thief or Police Officer tries to query information of both themselves and their opponent.
    case HttpRequest(HttpMethods.GET, Uri.Path("/query"), _, _, _) =>

      // We create an instance of the class Query which takes both original and perturbed Graph
      val getQuery = new Query(netOriginalGraph, netPerturbedGraph)

      // The first function in Query contains the path to node with valuable data from thief's current node position
      // We store that path in a string inside valuableDataNodePath to later send as response
      val valuableDataNodePath = getQuery.findClosestValuableDataNode(currentThiefNode)

     // The second function in Query contains the path to node Thief is at from Police Officer's current node position
     // We store that path in a string inside policeToThiefPath to later send as response
      val policeToThiefPath = getQuery.findThiefLocation(currentPoliceNode, currentThiefNode)


      // We check if path is returned to node with valuable data from thief
      val valuableNodePath = valuableDataNodePath match {

        // If it is we prepare the response with the path
        case Some(path) =>
          s"Path to Node with Valuable Data is as follows from Thief's Current Position:\n$path\n"

        // If not then we prepare response with no path found to valuable data
        case None =>
          "There is no path to Valuable Data found\n"
      }

      // We check if path is returned to node with thief from police
      val pathToThief = policeToThiefPath match {

        // If it is we prepare the response with the path
        case Some(path) =>
          s"Path to the Thief is as follows from Police Officer's Current Position:\n$path\n"

        // If not then we prepare response with no path found to Thief
        case None =>
          "There is no path to getting to Thief\n"
      }

      // 1. Store the response in the yaml file => 2. Send it as HTTP response
      writeResponseToYaml(valuableNodePath + "\n" + pathToThief)
      sender() ! HttpResponse(StatusCodes.OK, entity = valuableNodePath + "\n" + pathToThief)


  //------------------------------------Query Code ends here-------------------------------------------

    // This is called when player chooses to restart the game
    case HttpRequest(HttpMethods.POST, Uri.Path("/resetGame"), _, _, _) =>


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

      // We assign the global variables that keep track of current position of police/thief to the random assigned values
      currentPoliceNode = randomNode1
      currentThiefNode = randomNode2


      // Then we check if the node it has moved to is valuable data or not
      val valuableNodeChecker = new ValuableNodeChecker(netPerturbedGraph) // create an instance of the class
      val someNode: NodeObject = currentThiefNode
      val isValuable = valuableNodeChecker.isValuableNode(someNode) // Check if valuable data was found

      // Game ends and Thief wins if thief ends up on a valuable data as soon as game starts
      // The game is automatically restarted due to it
      if (isValuable) {
        // 1. Store Response in responseData => 2. Store it in the yaml file => 3. Send it as HTTP response
        responseData = "\n\nRestarting Game!\n\nThief Won! Valuable Data was found, thief successfully escaped"
        writeResponseToYaml(responseData)
        sender() ! HttpResponse(StatusCodes.OK, entity = responseData)
      }


      // If police officer cannot move anywhere (Such as no adjacent nodes available), Police Officer loses!
      if (policePossibleNodes.isEmpty) {
        // 1. Store Response in responseData => 2. Store it in the yaml file => 3. Send it as HTTP response
        responseData = "Restarting Game!\n\nOfficer Lost! No possible nodes to move to."
        writeResponseToYaml(responseData)
        sender() ! HttpResponse(StatusCodes.OK, entity = responseData)
      }

      // If thief cannot move anywhere (Such as no adjacent nodes available), Thief loses!
      if (thiefPossibleNode.isEmpty) {
        // 1. Store Response in responseData => 2. Store it in the yaml file => 3. Send it as HTTP response
        responseData = "Restarting Game!\n\nThief Lost! No possible nodes to move to."
        writeResponseToYaml(responseData)
        sender() ! HttpResponse(StatusCodes.OK, entity = responseData)
      }

      // If all condition passes above, the game will continue:
      // Police Officer will be given the chance to go first always
      // Here it will be provided with its adjacent nodes so it can move accordingly

      // 1. Store Response in responseData => 2. Store it in the yaml file => 3. Send it as HTTP response
      responseData = "Restarting Game!\n\nGame Started!\n\nPolice Officer, take position you are on Node: " + currentPoliceNode.id + "\n" +
        "Thief make your escape you are on Node: " + currentThiefNode.id + "\n\n" +
        "Police Officer goes first!\n" +
        "Officer: Pick one of the possible nodes to move to from the following: " +
        policePossibleNodes.map(_.id).mkString(" Or ") + "\n\n"
      writeResponseToYaml(responseData)
      sender() ! HttpResponse(StatusCodes.OK, entity = responseData)

  //------------------------------------Resetting Game Code ends here-------------------------------------------

    // This is executed if the HTTP response is not registered therefore it does not exist
    case _ =>
      responseData = "Not Found."
      writeResponseToYaml(responseData)
      sender() ! HttpResponse(StatusCodes.NotFound, entity = responseData)
  } // End of receiving HTTP requests


} // end of GameServer class


// This object is a companion object for the GameServer class. It provides a factory method (props)
// for creating instances of the GameServer actor with the specified parameters.
object GameServer {

  // The props method takes two graph parameters (netOriginalGraph and netPerturbedGraph) and an implicit Materializer.
  // It returns a Props object that is used to create instances of the GameServer actor.
  def props(netOriginalGraph: NetGraph, netPerturbedGraph: NetGraph, yamlFilePath: String)(implicit materializer: Materializer): Props =
    Props(new GameServer(netOriginalGraph, netPerturbedGraph, yamlFilePath, materializer))
} // end of GameServer object