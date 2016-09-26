package services

case class Player(id: Int) {
  override def toString: String = id.toString
}

case class Point(x: Int, y: Int) {
  override def toString: String = s"($x, $y)"
}

sealed trait GameState
case class WinState(msg: String) extends GameState
case class CatState(msg: String) extends GameState
case class Ongoing(msg: String) extends GameState
case class InvalidMove(msg: String) extends GameState

case class Board(gridMap: Map[Point, Player] = Map.empty[Point, Player])

case class TicToeBoard(board: Board, state: GameState) {

  def move(player: Player, position: Point): Either[TicToeBoard, TicToeBoard] = {
    // check if space is open / valid for move - TODO: check in bounds?
    if (!board.gridMap.contains(position)) {
      val newGameState = checkGameState(board.gridMap, position, player)
      val newGridMap = board.gridMap + (position -> player)
      Right(TicToeBoard(Board(newGridMap), newGameState))
    } else {
      // space already taken, return unmodified board with InvalidMove state
      Left(TicToeBoard(board, InvalidMove(s"Unable to move to $position!!!")))
    }
  }

  // TODO: return winning diagonal/row/column?
  def checkGameState(gridMap: Map[Point, Player], currentMove: Point, currentPlayer: Player): GameState = {
    if (gridMap.size <= 3) {
      Ongoing("Not enough moves yet.")
    } else {
      val playerPreviousMoves = gridMap.filter{case (k, v) => v == currentPlayer}.keySet

      // diag check
      val diagUp = Set(Point(2, 0), Point(1, 1), Point(0, 2))
      val diagDown = Set(Point(0, 0), Point(1, 1), Point(2, 2))
      if ((diagUp.contains(currentMove) && diagUp.subsetOf(playerPreviousMoves + currentMove)) ||
        (diagDown.contains(currentMove) && diagDown.subsetOf(playerPreviousMoves + currentMove))) {
        return WinState(s"Player $currentPlayer wins on the diagonal!")
      }

      // row check
      if (playerPreviousMoves.count(p => p.x == currentMove.x) == 2) {
        return WinState(s"Player $currentPlayer wins on the row!")
      }

      // column check
      if (playerPreviousMoves.count(p => p.y == currentMove.y) == 2) {
        return WinState(s"Player $currentPlayer wins on the column!")
      }

      Ongoing("No winner yet.")
    }
  }
}
