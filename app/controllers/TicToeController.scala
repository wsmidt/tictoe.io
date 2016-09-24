package controllers

import javax.inject._

import play.api.mvc.WebSocket.MessageFlowTransformer
import akka.actor.ActorSystem
import akka.stream.Materializer
import com.typesafe.scalalogging.LazyLogging
import play.api.libs.streams.ActorFlow
import play.api.mvc._
import services.{ConnectionActor, InMessage, OutMessage}

@Singleton
class TicToeController @Inject() (implicit system: ActorSystem, materializer: Materializer) extends Controller with LazyLogging {
  logger.debug(s"${this.getClass.getSimpleName} started.")

  import services.WSMessage.JsonFormats._
  implicit val messageFlowTransformer = MessageFlowTransformer.jsonMessageFlowTransformer[InMessage, OutMessage]

  def connect = WebSocket.accept[InMessage, OutMessage] { request =>
    ActorFlow.actorRef(out => ConnectionActor.props(out))
  }
}
