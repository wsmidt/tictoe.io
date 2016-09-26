package services

import com.typesafe.scalalogging.LazyLogging
import json.JsonMapping._
import json.ObjectFormat
import play.api.libs.json._
sealed trait WSMessage

sealed trait InMessage extends WSMessage
case class JsonReadsError(body: JsValue) extends InMessage
case object Ping extends InMessage

sealed trait OutMessage extends WSMessage
case object Pong extends OutMessage
case object AwaitingOpponent extends OutMessage
case object OpponentFound extends OutMessage
case class GameStarted(turn: Boolean) extends OutMessage
case class ErrorMessage(msg: String, status: Int) extends OutMessage

object ErrorMessage {
  type ErrorStatus = Int
  object Status {
    val BadRequest: ErrorStatus = 400
    val ServerError: ErrorStatus = 500
    val NotFound: ErrorStatus = 404
  }
}

object WSMessage {
  object JsonFormats {
    //This wraps the typed reads so that if theres an error in parsing the message we can then
    // return an JsonReadsError InMessage so that the sockets does not close on a parse JsError.
    implicit val inMessageReads: Reads[InMessage] = new Reads[InMessage] with LazyLogging {
      val typedInMessageReads = TypedReads[InMessage](
        "JsonReadsError" -> Json.reads[JsonReadsError],
        "Ping" -> ObjectFormat(Ping)
      )
      override def reads(json: JsValue): JsResult[InMessage] = typedInMessageReads.reads(json) recover {
        case e: JsError =>
          logger.debug(s"Failed to parse InMessage. body: $json, errors: $e")
          JsonReadsError(json)
      }
    }

    implicit val outMessageWrites: Writes[OutMessage] = TypedWrites[OutMessage](
      "ErrorMessage" -> Json.writes[ErrorMessage],
      "Pong" -> ObjectFormat(Pong),
      "AwaitingOpponent" -> ObjectFormat(AwaitingOpponent),
      "OpponentFound" -> ObjectFormat(OpponentFound),
      "GameStarted" -> Json.writes[GameStarted]
    )
  }
}

