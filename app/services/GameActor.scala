package services

import akka.actor.{Actor, ActorRef, Props}
import akka.actor.Actor.Receive
import com.typesafe.scalalogging.LazyLogging

class GameActor(xPlayerConnection: ActorRef, oPlayerConnection: ActorRef) extends Actor with LazyLogging {

  //TODO the Board should determine whos turn it is
  xPlayerConnection ! GameStarted(true)
  oPlayerConnection ! GameStarted(false)

  override def receive: Receive = {
    case other =>
      logger.error(s"received unhandled message: $other")
      sender ! ErrorMessage("RecievedUnhandledMessage", ErrorMessage.Status.ServerError)
  }
}

object GameActor {
  //TODO Props needs a unique actor name
  def props(xPlayerConnection: ActorRef, oPlayerConnection: ActorRef): Props = Props(new GameActor(xPlayerConnection, oPlayerConnection))
}
