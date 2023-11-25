**CS 441 PROJECT 3 - POLICE & THIEF GAME USING HTTP REQUEUST WITH AKKA FRAMEWORK**

Description: Police Thief Gamer Using HTTP commands with Akka Framework

Name: Muhammad Muzzammil

Instructions:

Begin by setting up your development environment, which includes installing IntelliJ, JDK, Scala runtime, the IntelliJ Scala plugin, and the Simple Build Toolkit (SBT). 

Ensure that you have also configured Scala monitoring tools for proper functionality.

Once the environment is set up, launch IntelliJ and open my project. Initiate the project build process. Please be patient as this step may take some time, as it involves downloading and incorporating the library dependencies listed in the build.sbt file into the project's classpath.

If you prefer an alternative approach, you can run the project via the command line. Open a terminal and navigate to the project directory for this purpose. 

The Classes included in this package are:

1. Main class
2. WebServer
3. GameServer
4. FindConnectedNodes
5. MoveValidator
6. PerturbedMoveValidator
7. Query
8. RandomNodeSelector
9. ValuableNodeChecker

Make sure that you can see all of these classes under src > main > scala > those classes. 

Once the project has been cloned:

Begin by following the NetGameSim Project’s ReadMe file to generate the NGS file for both Original Graph and Perturbed Graph. Store this on your local computer.

In order to make the project work you must change 4 of the directories for the following in the configuration file under src > main > resources > application.conf:

1. outputPath => Replace this with where you have stored the original and perturbed graphs in your local directory
2. originalGraphName => Replace this with the name of the file you have given to your originalGraph.ngs
3. perturbedGraphName => Replace this with the name of the file you have given to your perturbedGraph.ngs.perturbed
4. outputDataFilePath => Replace this with where you would like the yaml file to be produced.

After these has been set, the program is ready to run

You can run this project in two ways:

1. First being locally

  -  You will have to configure the files in the configuration file

2. Second being on AWS


  -  You will have to create the jar file for the main class
  -  Press the command sbt clean compile assembly
  -  That will generate the jar file for my project
  -  Use that file to upload to AWS
  -  You can then play the game on there

This project has a total of 8 tests in Scala. In order to run them using the terminal, cd into the project directory and run the command sbt clean compile test.

Use Case for the Classes in this project are as follows:

**1. Main Class:**
- Load in the configuration file
- Store all the directories for original graph, perturbed graph, and yaml file.
- It will then call on the Web Server class and pass all of the directories as arguments.

**2. WebServer Class:**  
- Imports necessary libraries and classes including those from Akka for actor based concurrency and Akka HTTP for building web services.
- The code loads an original graph and a perturbed graph from specified file paths. It logs success or terminates the program if the loading fails.
- An instance of the GameServer actor is created using the loaded graphs and a parameter from the command line.
- The code defines several HTTP routes and paths using Akka HTTP directives.
- Routes include handling requests for making moves (police and thief), starting the game, querying game state, and resetting the game.
- The code binds the defined routes to a specific host and port (localhost:9000) using Akka HTTP.
- The code includes a termination call to gracefully shut down the server in case of errors or when the program is terminated.
- The code is structured to handle HTTP requests related to a game using Akka HTTP, and it leverages Akka actors for managing the game logic.
  
**3. GameServer Class:**
- Import necessary libraries and classes.
- Define an actor class GameServer that handles the police and thief game logic based on HTTP requests.
- Initialize global variables and objects for game state and information storage.
- Handle the POST request to start the game.
- Generate random nodes for the police and thief.
- Store adjacent nodes for both police and thief in the original and perturbed graphs.
- Check for game ending conditions (e.g., thief on valuable data, no possible moves for police or thief).
- Send an HTTP response with relevant information.
- Handle the POST request when the police officer tries to make a move.
- Validate the move and check if it's legal.
- Update game state based on the move.
- Check for game-ending conditions (e.g., police capture the thief).
- Send an HTTP response with relevant information.
- Handle the POST request when the thief tries to make a move.
- Validate the move and check if it's legal.
- Update game state based on the move.
- Check for game-ending conditions (e.g., thief escapes or captured by the police).
- Send an HTTP response with relevant information.
- Handle the GET request for querying information about the game state.
- Use a Query object to find paths to valuable data and the thief from the police.
- Send an HTTP response with the queried information.
- Handle the POST request to reset the game.
- Generate new random nodes for the police and thief.
- Update game state accordingly.
- Check for game ending conditions.
- Send an HTTP response indicating the game restart.
- Handle cases where the HTTP endpoint is not recognized.
- Send an HTTP response with a "Not Found" message.
- Provides a factory method (props) for creating instances of the GameServer actor with specified parameters.
  
**4. FindConnectedNodes Class:**
- The class has a single method, getVNodesForSource, responsible for finding adjacent nodes for a given source node in a graph.
- Takes a NetGraph and a NodeObject as parameters.
- Retrieves the edges of the graph using netGraph.sm.edges() and converts it to a Scala list (originalEdgeList).
- Filters the edges to keep only those where the source node matches the given sourceNode.
- Maps the filtered edges to the destination nodes (nodeV()) and converts the result to a list of NodeObject (vNodeList).
- Returns the list of adjacent nodes (vNodeList)
- The purpose of this class is to find and return the adjacent nodes of a specified source node in a given graph
  
**5. MoveValidator Class:**
- The class takes a list of NodeObject named adjacentNodes as a parameter.
- Takes a move number as a string (moveNumberStr) as a parameter.
- Tries to convert moveNumberStr to an integer (moveNumber).
- Uses adjacentNodes.find(_.id == moveNumber) to find a NodeObject in adjacentNodes with an id matching the moveNumber.
- Returns an Option[NodeObject] representing either the found NodeObject or None if the move is not legal.
- Catches NumberFormatException and returns None if the conversion fails.
- The purpose of this class is to validate whether a move (specified by a move number) is legal based on a list of adjacent nodes provided during initialization.
  
**6. PerturbedMoveValidator Class:**
- This class does the exact same as MoveValidator Class but for Perturbed Graph
  
**7. RandomNodesSelector Class:**
- The class takes an original NetGraph (netOriginalGraph) as a parameter.
- private val nodeList: Convert the nodes of the original graph to a List[NodeObject] for easier random selection.
- Check if there are at least two nodes in the graph. If not, throw an IllegalArgumentException with the message "Not enough nodes to select from."
- Get two distinct random nodes (randomNode1 and randomNode2) from the nodeList.
- Ensure that both nodes are not equal (randomNode1 != randomNode2) and not connected to each other in any way using the original graph.
- Use a while loop to re-select randomNode2 if the equality or adjacency conditions are not met.
- Defines a method named getRandomNodes that returns a tuple (NodeObject, NodeObject) containing the two randomly selected nodes (randomNode1 and randomNode2)
- The purpose of this class is to facilitate the selection of two distinct random nodes from the original graph. These nodes are intended for use in a new game, ensuring that they are not equal and not connected to each other.
  
**8. ValuableNodeChecker Class:**
- The class takes a perturbed NetGraph (netPerturbedGraph) as a parameter.
- Define a method named isValuableNode that takes a NodeObject (node) as a parameter and returns a Boolean.
- Inside the method:
- Retrieve the nodes from the perturbed graph (netPerturbedGraph).
- Use the find method to find the node in the graph based on its ID (node.id).
- Check if the node is found and has valuableData set to true.
- Return true if the conditions are met, otherwise return false.
- The purpose of this class is to check if a given node in the perturbed graph is considered valuable. It does so by retrieving the nodes from the perturbed graph, finding the specified node, and checking if its valuableData property is set to true. The result is a Boolean indicating whether the node is valuable or not.
  
**9. Query Class:**
- The class takes two parameters, both of type NetGraph:
- netGraph: The original graph.
- perturbedNetGraph: The perturbed graph.
- Define a method named calculateScore that takes a NodeObject (node) as a parameter and returns a String.
- Inside the method:
- Create an instance of FindConnectedNodes to find adjacent nodes.
- Get the adjacent nodes for both the original and perturbed graphs using the getVNodesForSource method.
- Calculate the number of edges for both graphs.
- If the original graph has at least one edge:
- Increment the edge count for both the original and perturbed graphs.
- Create a string representing the score as the ratio of perturbed edges to original edges.
- If the original graph has no edges, return "0".
- Define a method named findClosestValuableDataNode that takes a NodeObject (sourceNode) as a parameter and returns an Option[String].
- Inside the method:
- Create an instance of FindConnectedNodes to find adjacent nodes.
- Initialize sets for visited nodes and a queue for BFS.
- Enqueue the source node without a score.
- While the queue is not empty:
- Dequeue the first element from the queue.
- If the current node has not been visited:
- Mark the current node as visited.
- Explore the connected nodes.
- Enqueue unvisited nodes with updated path and score.
- If the current node has valuable data, return the path with the score.
- If no valuable node is found, return None
- Create an instance of FindConnectedNodes to find adjacent nodes.
- Initialize sets for visited nodes and a queue for BFS.
- Enqueue the police node without a score.
- While the queue is not empty:
- Dequeue the first element from the queue.
- If the current node has not been visited:
- Mark the current node as visited.
- Explore the connected nodes.
- Enqueue unvisited nodes with updated path and score.
- If the current node is the destination thief node, return the path with the score.
- If the destination thief node is not found, return None.
