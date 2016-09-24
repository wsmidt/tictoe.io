package json

import play.api.libs.json._

import scala.reflect._

object JsonMapping {
  case class TypedJson(typ: String, data: JsValue)

  object TypedJson {
    implicit val jsonFormat = Json.format[TypedJson]
  }

  implicit class WritesMapping[T](typeAndWrites: (String, Writes[T]))(implicit val classTag: ClassTag[T]) {
    val typ: String = typeAndWrites._1
    val format: Writes[T] = typeAndWrites._2
    def matchesInstance(o: Any): Boolean = classTag.runtimeClass.isInstance(o)
    def toJson(o: Any): JsValue = format.writes(o.asInstanceOf[T])
  }

  implicit class ReadsMapping[T](typeAndReads: (String, Reads[T])) {
    val typ: String = typeAndReads._1
    val format: Reads[T] = typeAndReads._2
    def fromJson(jsValue: JsValue): JsResult[T] = format.reads(jsValue)
  }

  class TypedReads[T](typeMappings: Seq[ReadsMapping[_ <: T]]) extends Reads[T] {
    implicit val typedJsonReads = TypedJson.jsonFormat
    override def reads(json: JsValue): JsResult[T] = json.validate[TypedJson].flatMap { typedJson =>
      typeMappings.find(_.typ == typedJson.typ) match {
        case None => JsError(s"Type mapping not found for type: ${typedJson.typ}")
        case Some(mapping) => mapping.fromJson(typedJson.data)
      }
    }
  }
  object TypedReads {
    def apply[T](choices: ReadsMapping[_ <: T]*): TypedReads[T] = new TypedReads[T](choices)
  }

  class TypedWrites[T](typeMappings: Seq[WritesMapping[_ <: T]]) extends Writes[T] {
    implicit val typedJsonWrites = TypedJson.jsonFormat

    override def writes(o: T): JsValue = typeMappings.find(_.matchesInstance(o)) match {
      case None => throw new IllegalArgumentException(s"Type mapping not found for $o in ${typeMappings.map(_.getClass.getClasses)}")
      case Some(mapping) => Json.toJson(TypedJson(mapping.typ, mapping.toJson(o)))
    }
  }

  object TypedWrites {
    def apply[T](choices: WritesMapping[_ <: T]*): TypedWrites[T] = new TypedWrites[T](choices)
  }

}
