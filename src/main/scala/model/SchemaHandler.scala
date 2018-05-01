package model

import akka.actor.{Actor, ActorLogging}

object SchemaHandler {

  case class Validate(id: String)
  case class Upload(id: String)
  case class Get(id: String)
  case class SchemaValidated(action: String, id: String, status: String)
  case class SchemaUploaded(action: String, id: String, status: String)
  case class Schema(id: String)

}

class SchemaHandler() extends Actor with ActorLogging {
  import SchemaHandler._
  implicit val ec = context.dispatcher
  override def receive: Receive = {
    // closing over the sender in Future is not safe
    // I am keeping those references just in case for now

    case Validate(id) =>
      val _sender = sender()
      _sender ! SchemaValidated("validateDocument", id, "success")

    case Upload(id) =>
      val _sender = sender()
      _sender ! SchemaUploaded("uploadSchema", id, "success")

    case Get(id) =>
      val _sender = sender()
      _sender ! Schema(id)
  }
}
