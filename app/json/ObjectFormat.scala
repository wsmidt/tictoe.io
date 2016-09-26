package json

import play.api.libs.json._

case class ObjectFormat[T](o: T) extends Format[T] {
  override def reads(json: JsValue): JsResult[T] = JsSuccess(o)
  override def writes(o: T): JsValue = JsNull
}
