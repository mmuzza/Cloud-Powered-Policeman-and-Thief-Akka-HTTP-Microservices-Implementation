import NetGraphAlgebraDefs.{NetGraph, NodeObject}
import com.google.common.graph.EndpointPair

import scala.collection.convert.ImplicitConversions.`collection asJava`
import scala.jdk.CollectionConverters.CollectionHasAsScala

object Main {

  var tempNode : NodeObject = _

  def main(args: Array[String]): Unit = {
    println("Hello, World!")

    //originalGraphFile.ngs
    val originalGraph: Option[NetGraph] = NetGraph.load("NetGameSimNetGraph_26-10-23-23-39-25.ngs", "/Users/muzza/Desktop/projectTwo/TO_USE/")
//    val originalGraph: Option[NetGraph] = NetGraph.load("NetGameSimNetGraph_26-10-23-23-39-25.ngs", "/Users/muzza/Desktop/projectTwo/TO_USE/")

    val netOriginalGraph: NetGraph = originalGraph.getOrElse {
      // Handle the case where loading the graph failed
      println("Failed to load the graph.")
      sys.exit(1) // Terminate the program or handle the error appropriately
    }

    val originalGraphNodes: java.util.Set[NodeObject] = netOriginalGraph.sm.nodes()



    originalGraphNodes.forEach { nodeObject =>

      if(nodeObject.id == 16) {
        tempNode = nodeObject
//        println(s"Node ID: ${nodeObject.id}")
        println(s"Node: ${nodeObject}")


      }
    }




    val originalGraphEdges: java.util.Set[EndpointPair[NodeObject]] = netOriginalGraph.sm.edges()
    val originalEdgeList = originalGraphEdges.asScala.toList

    originalEdgeList.forEach {edge =>
      println(edge)
    }


  } // Main ends



} //  Object Main ends
