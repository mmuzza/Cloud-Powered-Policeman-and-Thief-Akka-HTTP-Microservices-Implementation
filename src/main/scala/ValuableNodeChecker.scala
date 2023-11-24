import NetGraphAlgebraDefs.{NetGraph, NodeObject}

import scala.collection.convert.ImplicitConversions.`collection AsScalaIterable`

class ValuableNodeChecker(netPerturbedGraph: NetGraph) {

  def isValuableNode(node: NodeObject): Boolean = {
    // Retrieve the nodes from netPerturbedGraph
    val nodes = netPerturbedGraph.sm.nodes()

    // Find the node in the graph
    val nodeOption = nodes.find(_.id == node.id)

    // Check if the node is found and has ValuableData set to true
    nodeOption.exists(_.valuableData)
  }
}
