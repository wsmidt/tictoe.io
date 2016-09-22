package controllers

import javax.inject._
import play.api._
import play.api.mvc._

@Singleton
class TicToeController @Inject() extends Controller {

  def connect = Action { Ok("connected meow") }

}
