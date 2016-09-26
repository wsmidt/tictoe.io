package services

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorLogging}

trait AppActor[S] extends Actor with ActorLogging {
  val logger = log //this is just for consistency with LazyLogger.logging

  val initialState: S

  override final def receive: Receive = receive(initialState)

  def receive(state: S): Receive

  def updateState(state: S) = context.become(receive(state))
}
