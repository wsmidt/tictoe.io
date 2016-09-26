package services

import akka.actor.{ActorRef, Props}

class ConnectionActor(out: ActorRef, gameManager: ActorRef) extends AppActor[ConnectionActor.State] {

  override val initialState: ConnectionActor.State = ConnectionActor.State(
    gameActor = None
  )

  logger.debug("connection established")

  //notify the game manager of this newly established connection
  gameManager ! GameManager.Messages.ConnectionReceived(self)

  override def receive(state: ConnectionActor.State): Receive = {
    case Ping => out ! Pong
    case start: GameStarted =>
      out ! start
      val updatedState = state.copy(gameActor = Some(sender))
      updateState(updatedState)
    case outMsg: OutMessage => out ! outMsg
    case inMsg: InMessage => state.gameActor match {
      case Some(gameActor) => gameActor ! inMsg
      case None => out ! ErrorMessage("GameNotFound", ErrorMessage.Status.NotFound)
    }
    case other =>
      logger.error(s"received unhandled message: $other")
      out ! ErrorMessage("RecievedUnhandledMessage", ErrorMessage.Status.ServerError)
  }

}

object ConnectionActor {
  //TODO Props needs a unique actor name
  def props(out: ActorRef, gameManager: ActorRef) = Props(new ConnectionActor(out, gameManager))

  case class State(gameActor: Option[ActorRef])
}
