package services

import akka.actor.{Actor, ActorRef, Props}
import akka.actor.Actor.Receive
import com.typesafe.scalalogging.LazyLogging

class ConnectionActor(out: ActorRef, gameManager: ActorRef) extends Actor with LazyLogging {
  logger.debug("connection established")

  //notify the game manager of this newly established connection
  gameManager ! GameManager.Messages.ConnectionReceived(self)

  def receive(state: ConnectionActor.State): Receive = {
    case Ping => out ! Pong
    case outMsg: OutMessage => out ! outMsg
    case inMsg: InMessage => state.gameActor match {
      case Some(gameActor) => gameActor ! inMsg
      case None => //TODO some error back to user
    }
    case other =>
      logger.error(s"received unhandled message: $other")
      out ! ErrorMessage("RecievedUnhandledMessage", ErrorMessage.Status.ServerError)
  }

  override def receive: Receive = receive(ConnectionActor.State())
}

object ConnectionActor {
  //TODO Props needs a unique actor name
  def props(out: ActorRef, gameManager: ActorRef) = Props(new ConnectionActor(out, gameManager))

  case class State(gameActor: Option[ActorRef] = None)
}
