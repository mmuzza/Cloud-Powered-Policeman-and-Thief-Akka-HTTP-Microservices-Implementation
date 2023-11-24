import NetGraphAlgebraDefs.{NetGraph, NodeObject}


// This class takes both original and perturbed graph
// It contains two major functions which are used in finding:
// The path to thief's node from Police & valuable data node from Thief
class Query(netGraph: NetGraph, perturbedNetGraph: NetGraph) {

  // This function is used in calculating the score
  // It compares the perturbed graph node with original graph node

  // The denominator of the score is original graph node:
  //      - It is calculates based on if:
  //          - It exists in original graph then denominator += 1
  //          - Number of edges denominator += # Of Edges

  // The numerator of the score is perturbed graph node:
  //      - It is calculates based on if:
  //          - It exists in original graph then numerator += 1
  //          - Number of edges numerator += # Of Edges
  def calculateScore(node: NodeObject): String = {
    var adjacentNodes = new FindConnectedNodes
    val originalAdjacent: List[NodeObject] = adjacentNodes.getVNodesForSource(netGraph, node)
    val perturbedAdjacent: List[NodeObject] = adjacentNodes.getVNodesForSource(perturbedNetGraph, node)

    var perturbedEdgeSize: Int = perturbedAdjacent.size
    var originalEdgeSize: Int = originalAdjacent.size

    // It means the node exists and there are edges to it
    if (originalEdgeSize > 0) {
      originalEdgeSize = originalEdgeSize + 1
      perturbedEdgeSize = perturbedEdgeSize + 1
    } else {
      return "0"
    }

    val score = perturbedEdgeSize + "/" + originalEdgeSize
    score
  }

  // This function takes a starting node which is thief's location
  // It loads in perturbed graph and searches and finds the node
  // It then calls on FindConnectedNodes to get the adjacent nodes
  // It keeps doing that using iterative approach fo finding the closes valuable data node.
  // It stores the path to getting the valuable data node along with each node's score by calling calculateScore
  // Then returns the path
  def findClosestValuableDataNode(sourceNode: NodeObject): Option[String] = {
    val connectedNodesFinder = new FindConnectedNodes()

    var visited = Set.empty[NodeObject]
    var queue = List.empty[(NodeObject, String)]

    // Enqueue the source node without a score
    queue = queue :+ (sourceNode, "")

    while (queue.nonEmpty) {
      // Dequeue the first element from the queue
      val (currentNode, pathSoFar) = queue.head
      queue = queue.tail

      if (visited.contains(currentNode)) {
        // Skip nodes that have already been visited
      } else {
        visited += currentNode

        // Explore the connected nodes
        val vNodes = connectedNodesFinder.getVNodesForSource(perturbedNetGraph, currentNode)
        val unvisitedNodes = vNodes.filterNot(visited.contains)

        // Enqueue unvisited nodes with updated path and score
        queue = queue ++ unvisitedNodes.map { nextNode =>
          val score = calculateScore(nextNode)
          (nextNode, pathSoFar + currentNode.id.toString + "     =====> Score: " + score + "\n")
        }

        if (currentNode.valuableData) {
          // If the current node has "valuableData", return the path with the score
          return Some(pathSoFar + currentNode.id.toString + "     =====> Score: " + calculateScore(currentNode) + "\n")
        }
      }
    }

    // If no valuable node is found
    None
  }



  // This function takes a starting node which is police officer's location and thief's location
  // It loads in perturbed graph and searches and finds the police officer node
  // It then calls on FindConnectedNodes to get the adjacent nodes of police officer's node
  // It keeps doing that using iterative approach fo finding the closet route to thief's node
  // It stores the path to getting the thief's node along with each node's score by calling calculateScore
  // Then returns the path
  def findThiefLocation(startPoliceNode: NodeObject, destinationThiefNode: NodeObject): Option[String] = {
    val connectedNodesFinder = new FindConnectedNodes()

    var visited = Set.empty[NodeObject]
    var queue = List.empty[(NodeObject, String)]

    // Enqueue the source node without a score
    queue = queue :+ (startPoliceNode, "")

    while (queue.nonEmpty) {
      // Dequeue the first element from the queue
      val (currentNode, pathSoFar) = queue.head
      queue = queue.tail

      if (visited.contains(currentNode)) {
        // Skip nodes that have already been visited
      } else {
        visited += currentNode

        // Explore the connected nodes
        val vNodes = connectedNodesFinder.getVNodesForSource(perturbedNetGraph, currentNode)
        val unvisitedNodes = vNodes.filterNot(visited.contains)

        // Enqueue unvisited nodes with updated path and score
        queue = queue ++ unvisitedNodes.map { nextNode =>
          val score = calculateScore(nextNode)
          (nextNode, pathSoFar + currentNode.id.toString + "     =====> Score: " + score + "\n")
        }

        if (currentNode == destinationThiefNode) {
          // If the current node is the destination thief node, return the path with the score
          return Some(pathSoFar + currentNode.id.toString + "     =====> Score: " + calculateScore(currentNode) + "\n")
        }
      }
    }

    // If the destination thief node is not found
    None
  }
}