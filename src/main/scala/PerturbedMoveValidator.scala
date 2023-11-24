import NetGraphAlgebraDefs.NodeObject

class PerturbedMoveValidator(adjacentNodes: List[NodeObject]) {

  def existsInPerturbed(moveNumberStr: String): Option[NodeObject] = {
    try {
      val moveNumber = moveNumberStr.toInt

      // Find the policePossibleNode that matches the moveNumber
      adjacentNodes.find(_.id == moveNumber)
    } catch {
      case _: NumberFormatException =>
        // Handle invalid move number format
        None
    }
  }
}
