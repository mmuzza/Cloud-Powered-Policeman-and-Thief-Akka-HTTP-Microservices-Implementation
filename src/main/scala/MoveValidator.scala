import NetGraphAlgebraDefs.NodeObject

// This class checks if the move thief and police are trying to attempt is legal or not
// This class takes a List of adjacent nodes which is generated by FindConnectedNodes class
class MoveValidator(adjacentNodes: List[NodeObject]) {

  // This function takes in the move-number/node-number user is attempting to move to
  // It then checks if that move-number/node.id exists in the adjacent node
  // If it does it returns that NodeObject else None
  def isLegalMove(moveNumberStr: String): Option[NodeObject] = {
    try {
      val moveNumber = moveNumberStr.toInt

      adjacentNodes.find(_.id == moveNumber)
    } catch {
      case _: NumberFormatException =>
        None
    }
  } // isLegalMove function ends here

} // MoveValidator class ends here
