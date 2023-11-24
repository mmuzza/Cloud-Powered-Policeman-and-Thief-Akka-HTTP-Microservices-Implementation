import NetGraphAlgebraDefs.NetGraph
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest, HttpResponse, Uri}
import akka.http.scaladsl.server.Directives.{as, complete, entity, get, path, pathPrefix, post}
import akka.stream.ActorMaterializer
import akka.util.Timeout
import akka.http.scaladsl.server.Directives._

object WebServer {


  def main(args: Array[String]): Unit = {


    // Loading in the Original graph
    var originalGraph = NetGraph.load("NetGameSimNetGraph_26-10-23-23-39-25.ngs", "/Users/muzza/Desktop/projectTwo/TO_USE/")
    var netOriginalGraph = originalGraph.getOrElse {
      println("Failed to load the graph.")
      sys.exit(1) // Terminate the program or handle the error appropriately
    }

    // Loading in the perturbed Graph
    var perturbedGraph = NetGraph.load("NetGameSimNetGraph_26-10-23-23-39-25.ngs.perturbed", "/Users/muzza/Desktop/projectTwo/TO_USE/")
    var netPerturbedGraph = perturbedGraph.getOrElse {
      println("Failed to load the graph.")
      sys.exit(1) // Terminate the program or handle the error appropriately
    }


    implicit val system = ActorSystem("GameSystem")
//    implicit val materializer = ActorMaterializer()
    val gameServer = system.actorOf(GameServer.props(netOriginalGraph, netPerturbedGraph), "gameServer")


    import akka.pattern.ask
    import scala.concurrent.duration._

    implicit val timeout: Timeout = Timeout(5.seconds)

    val route =
      pathPrefix("game") {

        path("police"/"makeMove") {
          post {
            entity(as[String]) { moveJson =>
              val responseFuture = (gameServer ? HttpRequest(HttpMethods.POST, Uri("/police/makeMove"), entity = HttpEntity(ContentTypes.`application/json`, moveJson)))
                .mapTo[HttpResponse]

              complete(responseFuture)
            }
          }
        } ~
          path("startGame") {
            post {
              entity(as[String]) { moveJson =>
                val responseFuture = (gameServer ? HttpRequest(HttpMethods.POST, Uri("/startGame"), entity = HttpEntity(ContentTypes.`application/json`, moveJson)))
                  .mapTo[HttpResponse]

                complete(responseFuture)
              }
            }
          } ~
          path("thief" / "makeMove") {
            post {
              entity(as[String]) { moveJson =>
                val responseFuture = (gameServer ? HttpRequest(HttpMethods.POST, Uri("/thief/makeMove"), entity = HttpEntity(ContentTypes.`application/json`, moveJson)))
                  .mapTo[HttpResponse]

                complete(responseFuture)
              }
            }
          } ~
          path("gameState") {
            get {
              val responseFuture = (gameServer ? HttpRequest(HttpMethods.GET, Uri("/gameState")))
                .mapTo[HttpResponse]

              complete(responseFuture)
            }
          } ~
          path("resetGame") {
            post {
              val responseFuture = (gameServer ? HttpRequest(HttpMethods.POST, Uri("/resetGame")))
                .mapTo[HttpResponse]

              complete(responseFuture)
            }
          }
      }



    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

    println(s"Server online at http://localhost:8080/")
  }
}