package model

import scala.collection.JavaConverters._

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.github.fge.jsonschema.main.{JsonSchema, JsonSchemaFactory}
import com.typesafe.config.{Config, ConfigFactory}
import spray.json._

import scala.concurrent.duration._
import scala.util.{Try,Success,Failure}

object SchemaHandler {

  def config: Config = ConfigFactory.load()
  def system = ActorSystem("WriterSystem")

  case class Validate(id: String, json: JsValue)
  case class Upload(id: String, schema: JsValue)
  case class Get(id: String)
  case class SchemaValidationSuccess(action: String, id: String, status: String)
  case class SchemaValidationFailure(action: String, id: String, status: String, message: String)
  case class SchemaUploaded(action: String, id: String, status: String)
  case class SchemaNotUploaded(action: String, id: String, status: String, message: String)
  case class SchemaNotFound(id: String)
  case class FailedToReadSchema(id: String)
}

class SchemaHandler() extends Actor with ActorLogging {
  import SchemaHandler._
  implicit val ec = context.dispatcher

  override def receive: Receive = {
    // closing over the sender in Future is not safe
    // I am keeping those references just in case for now

    case Validate(id, json) =>
      val _sender = sender()
      implicit val timeout: Timeout = Timeout(5 seconds)
      (self ? Get(id)).map {
		case resp:JsValue =>
		  val objectMapper = new ObjectMapper
		  val factory: JsonSchemaFactory = JsonSchemaFactory.byDefault()
		  val schema: JsonSchema = factory.getJsonSchema(objectMapper.readTree(resp.toString))
		  val jsonNode: JsonNode = objectMapper.readTree(json.toString)

		  val report = schema.validate(jsonNode)
		  val messages = report.asScala.filter {
			case msg => ! msg.toString.contains("found: \"null\"")
		  }.map {
			case msg => msg.toString
		  }.mkString("")
		  // FIXME: Use a decent way of filtering nulls out
		  // the way it is implemented is error prone.
		  // It's temporarily here as a _make it work_ solution
		  // Note that report.isSuccess() is an option, we would then
		  // need to filter out the nulls before validating
		  if(messages.length == 0) {
			_sender ! SchemaValidationSuccess("validateDocument", id, "success")
		  } else {
			_sender ! SchemaValidationFailure("validateDocument", id, "error", messages)
		  }
		case resp:SchemaNotFound => _sender ! SchemaNotFound(id)
		case resp:FailedToReadSchema => _sender ! SchemaNotFound(id)
      }

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
      val inputPath = config.getString("schemas.storageDirectory") + id
      val reader = system.actorOf(Props(new ReaderHandler()))
	  implicit val timeout: Timeout = Timeout(5 seconds)
	  (reader ? inputPath).map {
		case Success(schema:String) => _sender ! schema.parseJson
		case Failure(msg) =>
		  if (msg.getMessage == "Could not find schema") {
			_sender ! SchemaNotFound(id)
		  } else {
			_sender ! FailedToReadSchema(id)
		  }
	  }
  }
}
