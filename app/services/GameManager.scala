package services

import javax.inject._

import akka.actor.ActorRef
import services.GameManager.Messages.ConnectionReceived

@Singleton
class GameManager extends AppActor[GameManager.State] {

  override val initialState: GameManager.State = GameManager.State(
    awaitingConnectionActor = None
  )

  logger.debug("manager started")

  def receive(state: GameManager.State): Receive = {
    case ConnectionReceived(connectionActor) => state.awaitingConnectionActor match {
      case Some(awaitingConnectionActor) =>
        connectionActor ! OpponentFound
        awaitingConnectionActor ! OpponentFound

        //TODO Props needs a unique actor name. perhaps we should inject the GameActor?
        context.actorOf(GameActor.props(awaitingConnectionActor, connectionActor))

        val updatedState = state.copy(awaitingConnectionActor = None)
        updateState(updatedState)
      case None =>
        connectionActor ! AwaitingOpponent

        val updatedState = state.copy(awaitingConnectionActor = Some(connectionActor))
        updateState(updatedState)
    }
    case other =>
      logger.error(s"Received unhandled message: $other")
  }
}

object GameManager {
  case class State(awaitingConnectionActor: Option[ActorRef])

  object Messages {
    case class ConnectionReceived(connectionActor: ActorRef)
  }
}