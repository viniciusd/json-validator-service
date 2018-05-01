import akka.actor.{ActorRef, ActorSystem, Props}
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes.{InternalServerError, NoContent, OK}
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.stream.{ActorMaterializer, Materializer}
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}
import model.SchemaHandler
import model.SchemaHandler._
import spray.json.DefaultJsonProtocol

import scala.concurrent.ExecutionContextExecutor

trait Protocols extends DefaultJsonProtocol {
  implicit val schemaValidatedFormat = jsonFormat3(SchemaValidated.apply)
  implicit val schemaUploadedFormat = jsonFormat3(SchemaUploaded.apply)
  implicit val schemaFormat = jsonFormat1(Schema.apply)
}

trait Service extends Protocols {

  import scala.concurrent.duration._

  implicit val system: ActorSystem

  implicit def executor: ExecutionContextExecutor

  implicit val materializer: Materializer

  def config: Config

  val logger: LoggingAdapter

  def schemaHandler: ActorRef

  implicit def requestTimeout = Timeout(5 seconds)

  val routes =
    pathPrefix("validate") {
      path(Segment) { schemaId =>
        post {
          complete {
            (schemaHandler ? Validate(schemaId)).mapTo[SchemaValidated]
          }
        }
      }
    } ~ 
  pathPrefix("schema") {
    path(Segment) { schemaId =>
      post {
        complete {
          (schemaHandler ? Upload(schemaId)).mapTo[SchemaUploaded]
        }
      } ~
      get {
        complete {
          (schemaHandler ? Get(schemaId)).mapTo[Schema]
        }
      }
    }
  }
}

object AkkaHttpService extends App with Service {
  override implicit val system = ActorSystem()
  override implicit val executor = system.dispatcher
  override implicit val materializer = ActorMaterializer()

  override val config = ConfigFactory.load()
  override val logger = Logging(system, getClass)
  val schemaHandler = system.actorOf(Props[SchemaHandler])

  Http().bindAndHandle(routes, config.getString("http.interface"), config.getInt("http.port"))
}
