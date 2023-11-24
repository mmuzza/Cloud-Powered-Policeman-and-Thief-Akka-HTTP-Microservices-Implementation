import NetGraphAlgebraDefs.{NetGraph, NodeObject}
import java.util
import com.google.common.graph.EndpointPair
import scala.jdk.CollectionConverters._


class FindConnectedNodes {

  // This function takes the graph whether its perturbed and original and it takes a node object
  // It then loads the given graph's nodes and edges and searches for the specific node passed.
  // It then searches for all the existing edges for the node
  // And stores it edge.vNode in a list. VNode is a node it connects to
  // Then it returns that list of adjacent nodes
  def getVNodesForSource(netGraph: NetGraph, sourceNode: NodeObject): List[NodeObject] = {
    val originalGraphEdges: util.Set[EndpointPair[NodeObject]] = netGraph.sm.edges()
    val originalEdgeList: List[EndpointPair[NodeObject]] = originalGraphEdges.asScala.toList

    val vNodeList: List[NodeObject] = originalEdgeList
      .filter(edge => edge.source() == sourceNode)
      .map(_.nodeV())
      .toList

    vNodeList
  } // getVNodesForSource ends

} // FindConnectedNodes ends
