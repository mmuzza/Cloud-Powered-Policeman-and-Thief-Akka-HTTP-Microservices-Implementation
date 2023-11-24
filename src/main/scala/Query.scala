import NetGraphAlgebraDefs.{NetGraph, NodeObject}

class Query(netGraph: NetGraph, perturbedNetGraph: NetGraph) {

  // Function to calculate a score for a node
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




//    def findClosestValuableDataNode(sourceNode: NodeObject): Option[String] = {
//    val connectedNodesFinder = new FindConnectedNodes()
//
//    var visited = Set.empty[NodeObject]
//    var queue = List((sourceNode, ""))
//
//    while (queue.nonEmpty) {
//      val (currentNode, pathSoFar) = queue.head
//      queue = queue.tail
//
//      if (currentNode.valuableData) {
//        // If the current node has "valuableData", return the path
//        return Some(pathSoFar + currentNode.id.toString)
//      } else {
//        // Explore the connected nodes
//        val vNodes = connectedNodesFinder.getVNodesForSource(perturbedNetGraph, currentNode)
//        val unvisitedNodes = vNodes.filterNot(visited.contains)
//
//        // Enqueue unvisited nodes with updated path
//        queue ++= unvisitedNodes.map(nextNode => (nextNode, pathSoFar + currentNode.id.toString + "\n"))
//
//        // Mark the current node as visited
//        visited += currentNode
//      }
//    }
//
//    // If no valuable node is found
//    None
//  }


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