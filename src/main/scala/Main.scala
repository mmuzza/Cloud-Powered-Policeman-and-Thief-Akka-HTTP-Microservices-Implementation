import NetGraphAlgebraDefs.NetGraph.logger
import com.typesafe.config.{Config, ConfigFactory}
import Utilz.NGSConstants.{CONFIGENTRYNAME, obtainConfigModule}


// Main can be ran to run the whole game
// The
object Main {

  def main(args: Array[String]): Unit = {

    // Loading configuration file to extract
    logger.info("Loading Configuration file")
    val config: Config = ConfigFactory.load()
    val globalConfig: Config = obtainConfigModule(config, CONFIGENTRYNAME)
    logger.info("Configuration file was loaded")

    // Storing all files directory that is needed from configuration file
    // If you are running from your system please update the configuration file
    // So it loads it from your local computer
    logger.info("Extracting path to original, and perturbed graphs")
    val fileDirectoryForGraphs = globalConfig.getString("outputDirectory")
    val originalGraphFileName = globalConfig.getString("originalGraphName")
    val perturbedGraphFileName = globalConfig.getString("perturbedGraphName")
    val yamlFilePath = globalConfig.getString("outputDataFilePath")
    logger.info("Extracting path to original, and perturbed graphs was successful")

    // This will start the game up and the server
    logger.info("Starting the Server and the Game")
    WebServer.startServerAndGame(fileDirectoryForGraphs, originalGraphFileName, perturbedGraphFileName, yamlFilePath)


  } // Main ends

} //  Object Main ends
