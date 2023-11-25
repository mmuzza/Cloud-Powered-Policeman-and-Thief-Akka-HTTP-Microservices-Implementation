import NetGraphAlgebraDefs.NetGraph
import NetGraphAlgebraDefs.NetGraph.logger
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest, HttpResponse, Uri}
import akka.http.scaladsl.server.Directives.{as, complete, entity, get, path, pathPrefix, post, _}
import akka.util.Timeout


// This class contains a function which is responsible for loading in both original and perturbed graphs
// It registers routes and paths for HTTP commands
// And accordingly calls on the GameServer class to handle each request
object WebServer {

  // This function is called by main class
  // Used to start the server and handle all http requests to play the game
  def startServerAndGame(args: String*): Unit = {

    // Loading original graph
    logger.info("Loading the Original Graph from the given path")
    var originalGraph = NetGraph.load({args(1)}, {args(0)})
    var netOriginalGraph = originalGraph.getOrElse {
      logger.info("Failed to load the original graph")
      sys.exit(1) // Terminate the program or handle the error appropriately
    }
    logger.info("Original graph was successfully loaded")


    // Loading perturbed Graph
    logger.info("Loading the Perturbed Graph from the given path")
    var perturbedGraph = NetGraph.load({args(2)}, {args(0)})
    var netPerturbedGraph = perturbedGraph.getOrElse {
      logger.info("Failed to load the perturbed graph")
      sys.exit(1) // Terminate the program or handle the error appropriately
    }
    logger.info("Perturbed graph was successfully loaded")


    // Creating an instance of GameServer which will be used to call on for http requests
    implicit val system = ActorSystem("GameSystem")
    val gameServer = system.actorOf(GameServer.props(netOriginalGraph, netPerturbedGraph, {args(3)}), "gameServer")


    import akka.pattern.ask
    import scala.concurrent.duration._

    implicit val timeout: Timeout = Timeout(5.seconds)

    logger.info("Registering route to 'Game'")
    logger.info("Registering the following path prefixes 'police/makeMove', 'startGame', 'thief/makeMove', 'query', & 'resetGame'")

    val route = {

      // Anyone sending command using http must use 'game' as prefix before the requests
      pathPrefix("game") {

        // First registered request is "game/police/makeMove"
        // It is set to be a POST request by HTTP
        // It will send it to GameServer class to handle it
        path("police" / "makeMove") {
          post {
            entity(as[String]) { moveJson =>
              val responseFuture = (gameServer ? HttpRequest(HttpMethods.POST, Uri("/police/makeMove"), entity = HttpEntity(ContentTypes.`application/json`, moveJson)))
                .mapTo[HttpResponse]

              complete(responseFuture)
            }
          }
        } ~
          // Second registered request is "game/startGame"
          // It is set to be a POST request by HTTP
          // It will send it to GameServer class to handle it
          path("startGame") {
            post {
              entity(as[String]) { moveJson =>
                val responseFuture = (gameServer ? HttpRequest(HttpMethods.POST, Uri("/startGame"), entity = HttpEntity(ContentTypes.`application/json`, moveJson)))
                  .mapTo[HttpResponse]

                complete(responseFuture)
              }
            }
          } ~
          // Third registered request is "game/thief/makeMove"
          // It is set to be a POST request by HTTP
          // It will send it to GameServer class to handle it
          path("thief" / "makeMove") {
            post {
              entity(as[String]) { moveJson =>
                val responseFuture = (gameServer ? HttpRequest(HttpMethods.POST, Uri("/thief/makeMove"), entity = HttpEntity(ContentTypes.`application/json`, moveJson)))
                  .mapTo[HttpResponse]

                complete(responseFuture)
              }
            }
          } ~
          // Forth registered request is "game/gameState"
          // It is set to be a GET request by HTTP
          // It will send it to GameServer class to handle it
          path("query") {
            get {
              val responseFuture = (gameServer ? HttpRequest(HttpMethods.GET, Uri("/query")))
                .mapTo[HttpResponse]

              complete(responseFuture)
            }
          } ~
          // First registered request is "game/requestGame"
          // It is set to be a POST request by HTTP
          // It will send it to GameServer class to handle it
          path("resetGame") {
            post {
              val responseFuture = (gameServer ? HttpRequest(HttpMethods.POST, Uri("/resetGame")))
                .mapTo[HttpResponse]

              complete(responseFuture)
            }
          }
      }
    }
    logger.info("Registering route and path successful")

    logger.info("Connecting to the local host")
    val bindingFuture = Http().bindAndHandle(route, "localhost", 9000)
    logger.info("Server online at http://localhost:9000/")

  } // end of startServerAndGame function



} // Object WebServer ends here