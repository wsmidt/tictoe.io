package controllers

import javax.inject._
import play.api._
import play.api.mvc._

@Singleton
class HealthCheckController @Inject() extends Controller {

  def healthcheck = Action {
    Ok("kittens!")
  }

}
