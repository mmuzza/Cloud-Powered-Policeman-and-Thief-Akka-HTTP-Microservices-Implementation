
import NetGraphAlgebraDefs.NetGraph.logger
import NetGraphAlgebraDefs.{NetGraph, NodeObject}
import Utilz.NGSConstants.{CONFIGENTRYNAME, obtainConfigModule}
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.matchers.must.Matchers
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

// This is a class to test major classes being used in this code
// Total of 8 test cases in this file
// We first load all necessary files needed from configuration file
// If you are testing this code from your system, please make sure to update the configuration file
// Read Me file for this project goes more in detail on what exactly to change
class Test extends AnyFunSuite with Matchers {

  // Loading configuration file to extract
  logger.info("Loading Configuration files")
  val config: Config = ConfigFactory.load()
  val globalConfig: Config = obtainConfigModule(config, CONFIGENTRYNAME)
  logger.info("Configuration Class was loaded")

  // Storing all files directory that is needed from configuration file
  // If you are running from your system please update the configuration file
  // So it loads it from your local computer
  logger.info("Extracting all paths to original, and perturbed graphs")
  val fileDirectoryForGraphs: String = globalConfig.getString("outputDirectory")
  val originalGraphFileName: String = globalConfig.getString("originalGraphName")
  val perturbedGraphFileName: String = globalConfig.getString("perturbedGraphName")

  // Loading original graph
  logger.info("Loading the Original Graph from the given path")
  var originalGraph = NetGraph.load(originalGraphFileName, fileDirectoryForGraphs)

  var netOriginalGraph = originalGraph.getOrElse {
    logger.info("Failed to load the original graph")
    sys.exit(1) // Terminate the program or handle the error appropriately
  }
  logger.info("Original graph was successfully loaded")


  // Loading perturbed Graph
  logger.info("Loading the Perturbed Graph from the given path")
  var perturbedGraph = NetGraph.load(perturbedGraphFileName, fileDirectoryForGraphs)

  var netPerturbedGraph = perturbedGraph.getOrElse {
    logger.info("Failed to load the perturbed graph")
    sys.exit(1) // Terminate the program or handle the error appropriately
  }
  logger.info("Perturbed graph was successfully loaded")

  var originalGraphNodes = netOriginalGraph.sm.nodes()

  var testingNode1: NodeObject = _
  var testingNode2: NodeObject = _
  var testingNode3: NodeObject = _
  var testingNode4: NodeObject = _

  originalGraphNodes.forEach(node =>
    if(node.id == 141){
      testingNode1 = node
    }
    else if (node.id == 486) {
      testingNode2 = node
    }
    else if (node.id == 395) {
      testingNode3 = node
    }
    else if (node.id == 420) {
      testingNode4 = node
    }
  )

//------------------------------------------------------------------------------------------------

  logger.info("Starting Test Cases")


  // Example: Creating an instance of MoveValidator with some adjacent nodes
  val adjacentNodes: List[NodeObject] = List(
    testingNode1,
    testingNode2,
    testingNode3,
    testingNode4
  )

  // Creating Instances of all class that will be tested
  val moveValidatorClass: MoveValidator = new MoveValidator(adjacentNodes)
  val valuableNodeCheckerClass: ValuableNodeChecker = new ValuableNodeChecker(netPerturbedGraph)
  val randomNodeSelectorClass: RandomNodeSelector = new RandomNodeSelector(netPerturbedGraph)
  val findConnectedNodesClass: FindConnectedNodes = new FindConnectedNodes()
  val queryClass: Query = new Query(netOriginalGraph, netPerturbedGraph)


  // TEST CASE 1
  test("Test 1 for Move Validator Class") {

    var result = moveValidatorClass.isLegalMove("420")

    // Should return testing Node 4
    result should contain(testingNode4)
    println("Test 1 Passed")
  }

  // TEST CASE 2
  test("Test 2 for Move Validator Class") {

    var result = moveValidatorClass.isLegalMove("141")

    // Should return testing Node 1
    result should contain(testingNode1)
    println("Test 2 Passed")
  }

  // TEST CASE 3
  test("Test 3 for Move Validator Class") {

    var result = moveValidatorClass.isLegalMove("486")

    // Should return testing Node 2
    result should contain(testingNode2)
    println("Test 3 Passed")
  }

  // TEST CASE 4
  test("Test 1 for Valuable Node Checker Class") {

    var result = valuableNodeCheckerClass.isValuableNode(testingNode3)

    // Should return TRUE
    result shouldBe (true)
    println("Test 4 Passed")
  }

  // TEST CASE 5
  test("Test 2 for Valuable Node Checker Class") {

    var result = valuableNodeCheckerClass.isValuableNode(testingNode2)

    // Should return FALSE
    result shouldBe (false)
    println("Test 5 Passed")
  }

  // TEST CASE 6
  test("Test 1 for Random Node Selector Class") {

    // Test the getRandomNodes function
    val result: (NodeObject, NodeObject) = randomNodeSelectorClass.getRandomNodes

    // Assert the return type is a tuple of NodeObjects
    result shouldBe a[(NodeObject, NodeObject)]
    println("Test Case 6 Passed")
  }

  // TEST CASE 7
  test("Test 1 for Find Connected Nodes Class") {

    // Test the getVNodesForSource function
    val result: List[NodeObject] = findConnectedNodesClass.getVNodesForSource(netOriginalGraph, testingNode1)

    result shouldBe a[List[NodeObject]]
    result should not be empty

    println("Test Case 7 Passed")
  }

  // TEST CASE 8
  test("Test 1 for Query Class") {

    // Test the findClosestValuableDataNode function
    val result: Option[String] = queryClass.findClosestValuableDataNode(testingNode2)

    // Assert that the result is Some(String) and it's not empty
    result shouldBe a[Some[String]]
    result should not be empty
    println("Test Case 8 Passed")
  }


} // End of test Class
