package model

import akka.actor.{Actor, ActorLogging}

import java.nio.charset.StandardCharsets
import java.nio.file.{Paths, Files}

import scala.util.{Try,Success,Failure}

class WriterHandler(outFile: String) extends Actor with ActorLogging {

  val path = Paths.get(outFile);

  override def receive = {
    // TODO: Do not accept if it is an invalid JSON
    case str:String =>
      val _sender = sender()
      Try(Files.write(path, str.getBytes(StandardCharsets.UTF_8))) match {
        case Success(_) => _sender ! Success(Unit)
        case Failure(_) => _sender ! Failure(new Exception("Could not write schema"))
      }
  }
}
