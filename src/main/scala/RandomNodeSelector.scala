import NetGraphAlgebraDefs.{NetGraph, NodeObject}
import scala.jdk.CollectionConverters.CollectionHasAsScala
import scala.util.Random


// This class takes in original net graph
// It is called when new game is started
// This generates two random nodes
// both nodes are at different locations and are not adjacent to each other
class RandomNodeSelector(netOriginalGraph: NetGraph) {

  // Convert the nodes to a List for easier random selection
  private val nodeList: List[NodeObject] = netOriginalGraph.sm.nodes().asScala.toList

  // Check if there are at least two nodes in the graph
  if (nodeList.size < 2) {
    throw new IllegalArgumentException("Not enough nodes to select from.")
  }

  // Getting two distinct random nodes
  private var randomNode1: NodeObject = nodeList(Random.nextInt(nodeList.size))
  private var randomNode2: NodeObject = nodeList(Random.nextInt(nodeList.size))

  // Ensure that both nodes are not equal
  // And when the game starts, they are not connected to each other in any way
  while (randomNode1 == randomNode2 || netOriginalGraph.sm.hasEdgeConnecting(randomNode1, randomNode2)) {
    randomNode2 = nodeList(Random.nextInt(nodeList.size))
  }

  // Now, you have two distinct random nodes that we can assign to when getRandomNodes is called.
  def getRandomNodes: (NodeObject, NodeObject) = (randomNode1, randomNode2)
}
