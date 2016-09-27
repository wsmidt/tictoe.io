package controllers

import java.lang.management.ManagementFactory
import javax.inject._

import buildInfo.BuildInfo
import controllers.HealthCheckController.HealthCheck
import play.api.libs.json.{JsValue, Json, Writes}
import play.api.mvc._

@Singleton
class HealthCheckController @Inject() extends Controller {
  import HealthCheck._

  val buildInfoJson = Json.parse(BuildInfo.toJson)
  val startTime = ManagementFactory.getRuntimeMXBean.getStartTime
  def uptime() = ManagementFactory.getRuntimeMXBean.getUptime()

  def healthCheck = Action {
    val healthCheck = HealthCheck(buildInfoJson, startTime, uptime())
    Ok(Json.prettyPrint(Json.toJson(healthCheck)))
  }
}

object HealthCheckController {
  case class HealthCheck(
    buildInfo: JsValue,
    startTime: Long,
    uptime: Long
  )

  object HealthCheck {
    implicit val jsonWrites: Writes[HealthCheck] = Json.writes[HealthCheck]
  }
}
