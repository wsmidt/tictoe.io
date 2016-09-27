package services

import akka.actor.{ActorRef, Props, Terminated}

class GameActor(xPlayerConnection: ActorRef, oPlayerConnection: ActorRef) extends AppActor[GameActor.State] {

  override val initialState: GameActor.State = GameActor.State()

  //watch the connection actors for when the are Terminated
  context.watch(xPlayerConnection)
  context.watch(oPlayerConnection)

  //TODO the Board should determine whos turn it is
  xPlayerConnection ! GameStarted(true)
  oPlayerConnection ! GameStarted(false)

  override def receive(state: GameActor.State): Receive = {
    case Terminated(connectionActor) if connectionActor.equals(xPlayerConnection) =>
      oPlayerConnection ! OpponentDisconnected
      context.stop(self)
    case Terminated(connectionActor) if connectionActor.equals(oPlayerConnection) =>
      xPlayerConnection ! OpponentDisconnected
      context.stop(self)
    case other =>
      logger.error(s"received unhandled message: $other")
      sender ! ErrorMessage("RecievedUnhandledMessage", ErrorMessage.Status.ServerError)
  }
}

object GameActor {
  //TODO Props needs a unique actor name
  def props(xPlayerConnection: ActorRef, oPlayerConnection: ActorRef): Props = Props(new GameActor(xPlayerConnection, oPlayerConnection))

  //TODO the board will be in the state
  case class State()
}
