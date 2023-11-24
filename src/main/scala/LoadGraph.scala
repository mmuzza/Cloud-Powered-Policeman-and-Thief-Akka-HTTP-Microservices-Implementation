import NetGraphAlgebraDefs.{NetGraph, NodeObject}

class LoadGraph {

  def loadGraphNodes(): java.util.Set[NodeObject] = {
    val originalGraph: Option[NetGraph] = NetGraph.load("NetGameSimNetGraph_26-10-23-23-39-25.ngs", "/Users/muzza/Desktop/projectTwo/TO_USE/")
    val netOriginalGraph: NetGraph = originalGraph.getOrElse {
      // Handle the case where loading the graph failed
      println("Failed to load the graph.")
      sys.exit(1) // Terminate the program or handle the error appropriately
    }
    val originalGraphNodes: java.util.Set[NodeObject] = netOriginalGraph.sm.nodes()
    originalGraphNodes
  }
}
