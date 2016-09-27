package services

import javax.inject._

import akka.actor.{ActorRef, Terminated}
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
        //the game manager is no longer responsible for the connections Terminated. the GameActor
        // is now responsible for its Terminated message as it needs to also notify the opponent
        context.unwatch(awaitingConnectionActor)

        connectionActor ! OpponentFound
        awaitingConnectionActor ! OpponentFound

        //TODO Props needs a unique actor name. perhaps we should inject the GameActor?
        context.actorOf(GameActor.props(awaitingConnectionActor, connectionActor))

        val updatedState = state.copy(awaitingConnectionActor = None)
        updateState(updatedState)
      case None =>
        //monitor the connection actor. on Terminated we need clean it out of the state.
        context.watch(connectionActor)

        connectionActor ! AwaitingOpponent

        val updatedState = state.copy(awaitingConnectionActor = Some(connectionActor))
        updateState(updatedState)
    }

    case Terminated(connectionActor) =>
      if (state.awaitingConnectionActor.contains(connectionActor)) {
        logger.debug("Awaiting actor disconnected. No longer awaiting game.")
        val updatedState = state.copy(awaitingConnectionActor = None)
        updateState(updatedState)
      } else {
        //Note: this will only happen if the Terminated is queued after another connection arrives and
        // a GameActor is started. If a GameActor was started it will handle the Termination as it needs
        // to notify the opponent connaction actor.
        logger.warning("Terminated actor is no longer awaiting. GameActor will handle the termination.")
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