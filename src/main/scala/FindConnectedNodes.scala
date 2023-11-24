import NetGraphAlgebraDefs.{NetGraph, NodeObject}
import java.util
import com.google.common.graph.EndpointPair
import scala.jdk.CollectionConverters._


class FindConnectedNodes {

  def getVNodesForSource(netGraph: NetGraph, sourceNode: NodeObject): List[NodeObject] = {
    val originalGraphEdges: util.Set[EndpointPair[NodeObject]] = netGraph.sm.edges()
    val originalEdgeList: List[EndpointPair[NodeObject]] = originalGraphEdges.asScala.toList

    val vNodeList: List[NodeObject] = originalEdgeList
      .filter(edge => edge.source() == sourceNode)
      .map(_.nodeV())
      .toList

    vNodeList
  }
}
