import NetGraphAlgebraDefs.{NetGraph, NodeObject}
import scala.collection.convert.ImplicitConversions.`collection AsScalaIterable`

// This class is to check if node happens to be a valuable node
// This class takes in perturbed Graph
class ValuableNodeChecker(netPerturbedGraph: NetGraph) {

  // This function grabs the nodes from the perturbed graph
  // It takes in a node object
  // Checks to see if the valuable data node is set to true
  // If true it returns true else false
  def isValuableNode(node: NodeObject): Boolean = {
    // Retrieve the nodes from netPerturbedGraph
    val nodes = netPerturbedGraph.sm.nodes()

    // Find the node in the graph
    val nodeOption = nodes.find(_.id == node.id)

    // Check if the node is found and has ValuableData set to true
    nodeOption.exists(_.valuableData)
  }
}
