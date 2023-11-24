import NetGraphAlgebraDefs.{NetGraph, NodeObject}

import scala.jdk.CollectionConverters.CollectionHasAsScala
import scala.util.Random

class RandomNodeSelector(netOriginalGraph: NetGraph) {
  // Convert the nodes to a List for easier random selection
  private val nodeList: List[NodeObject] = netOriginalGraph.sm.nodes().asScala.toList

  // Check if there are at least two nodes in the graph
  if (nodeList.size < 2) {
    throw new IllegalArgumentException("Not enough nodes to select from.")
  }

  // Get two distinct random nodes
  private var randomNode1: NodeObject = nodeList(Random.nextInt(nodeList.size))
  private var randomNode2: NodeObject = nodeList(Random.nextInt(nodeList.size))

  // Ensure that both nodes are not equal
  // And when the game starts, they are not connected to each other in any way
  while (randomNode1 == randomNode2 || netOriginalGraph.sm.hasEdgeConnecting(randomNode1, randomNode2)) {
    randomNode2 = nodeList(Random.nextInt(nodeList.size))
  }

  // Now, you have two distinct random nodes
  def getRandomNodes: (NodeObject, NodeObject) = (randomNode1, randomNode2)
}

//import NetGraphAlgebraDefs.{NetGraph, NodeObject}
//
//import scala.jdk.CollectionConverters.CollectionHasAsScala
//import scala.util.Random
//
//class RandomNodeSelector(netOriginalGraph: NetGraph) {
//  // Convert the nodes to a List for easier random selection
//  private val nodeList: List[NodeObject] = netOriginalGraph.sm.nodes().asScala.toList
//
//  // Check if there are at least two nodes in the graph
//  if (nodeList.size < 2) {
//    throw new IllegalArgumentException("Not enough nodes to select from.")
//  }
//
//  // Get a random node
//  private var randomNode1: NodeObject = nodeList(Random.nextInt(nodeList.size))
//
//  // Get the node with ID 158, if it exists
//  private var randomNode2: NodeObject = nodeList.find(_.id == 137).getOrElse {
//    // If a node with ID 158 doesn't exist, select a different random node
//    while (randomNode2 == randomNode1 || netOriginalGraph.sm.hasEdgeConnecting(randomNode1, randomNode2)) {
//      randomNode2 = nodeList(Random.nextInt(nodeList.size))
//    }
//    randomNode2
//  }
//
//  // Now, you have two nodes: randomNode1 and a node with ID 158 (or another random node if ID 158 doesn't exist)
//  def getRandomNodes: (NodeObject, NodeObject) = (randomNode1, randomNode2)
//}
