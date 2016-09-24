package services

import akka.actor.{Actor, ActorRef, Props}
import akka.actor.Actor.Receive
import com.typesafe.scalalogging.LazyLogging

class ConnectionActor(out: ActorRef) extends Actor with LazyLogging {
  logger.debug("connection established")

  override def receive: Receive = {
    case Ping => out ! Pong
    case other =>
      logger.error(s"received unhandled message: $other")
      out ! ErrorMessage("RecievedUnhandledMessage", ErrorMessage.Status.ServerError)
  }
}

object ConnectionActor {
  //TODO Props needs a unique actor name
  def props(out: ActorRef) = Props(new ConnectionActor(out))
}
