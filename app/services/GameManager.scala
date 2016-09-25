package services

import javax.inject._

import akka.actor.{Actor, ActorRef}
import akka.actor.Actor.Receive
import com.typesafe.scalalogging.LazyLogging
import services.GameManager.Messages.ConnectionReceived

@Singleton
class GameManager extends Actor with LazyLogging {
  logger.debug("manager started")

  override def receive: Receive = receive(GameManager.State())

  def receive(state: GameManager.State): Receive = {
    case ConnectionReceived(connectionActor) => state.awaitingConnectionActor match {
      case Some(awaitingConnectionActor) =>
        connectionActor ! OpponentFound
        awaitingConnectionActor ! OpponentFound

        //TODO create GameActor and send the game actor to the connections!

        val updatedState = state.copy(awaitingConnectionActor = None)
        context.become(receive(updatedState))
      case None =>
        connectionActor ! AwaitingOpponent

        val updatedState = state.copy(awaitingConnectionActor = Some(connectionActor))
        context.become(receive(updatedState))
    }
    case other =>
      logger.error(s"Received unhandled message: $other")
  }
}

object GameManager {
  case class State(awaitingConnectionActor: Option[ActorRef] = None)

  object Messages {
    case class ConnectionReceived(connectionActor: ActorRef)
  }
}