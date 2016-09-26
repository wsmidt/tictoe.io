package services

import com.typesafe.scalalogging.LazyLogging
import json.JsonMapping._
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
        "Ping" -> new Reads[Ping.type] {
          override def reads(json: JsValue): JsResult[Ping.type] = JsSuccess(Ping)
        }
      )
      override def reads(json: JsValue): JsResult[InMessage] = typedInMessageReads.reads(json) recover {
        case e: JsError =>
          logger.debug(s"Failed to parse InMessage. body: $json, errors: $e")
          JsonReadsError(json)
      }
    }

    implicit val outMessageWrites: Writes[OutMessage] = TypedWrites[OutMessage](
      "ErrorMessage" -> Json.writes[ErrorMessage],
      "Pong" -> new Writes[Pong.type] {
        override def writes(o: Pong.type): JsValue = JsNull
      },
      "AwaitingOpponent" -> new Writes[AwaitingOpponent.type] {
        override def writes(o: AwaitingOpponent.type): JsValue = JsNull
      },
      "OpponentFound" -> new Writes[OpponentFound.type] {
        override def writes(o: OpponentFound.type): JsValue = JsNull
      },
      "GameStarted" -> Json.writes[GameStarted]
    )
  }
}

