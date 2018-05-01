package model

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}
import spray.json.JsValue

import scala.concurrent.duration._
import scala.util.{Try,Success,Failure}

object SchemaHandler {

  def config: Config = ConfigFactory.load()
  def system = ActorSystem("WriterSystem")

  case class Validate(id: String)
  case class Upload(id: String, schema: JsValue)
  case class Get(id: String)
  case class SchemaValidated(action: String, id: String, status: String)
  case class SchemaUploaded(action: String, id: String, status: String)
  case class SchemaNotUploaded(action: String, id: String, status: String, message: String)
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

    case Upload(id, schema) =>
      val _sender = sender()
      val outputPath = config.getString("schemas.storageDirectory") + id
      val writer = system.actorOf(Props(new WriterHandler(outputPath)))
      implicit val timeout: Timeout = Timeout(5 seconds)
      (writer ? schema.toString).map {
        case Success(_) => _sender ! SchemaUploaded("uploadSchema", id, "success")
        case Failure(e) => _sender ! SchemaNotUploaded("uploadSchema", id, "error", e.getMessage)
      }

    case Get(id) =>
      val _sender = sender()
      _sender ! Schema(id)
  }
}
