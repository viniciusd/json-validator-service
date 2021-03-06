package model

import akka.actor.{Actor, ActorLogging}

import java.nio.charset.StandardCharsets
import java.nio.file.{Paths, Files}

import scala.util.{Try,Success,Failure}

class ReaderHandler() extends Actor with ActorLogging {

  override def receive = {
    case str:String =>
      val path = Paths.get(str);
      val _sender = sender()

	  if (Files.exists(path) ) {
		Try(Files.readAllBytes(path)) match {
		  case Success(bytes) => _sender ! Success(new String(bytes, StandardCharsets.UTF_8))
		  case Failure(_) => _sender ! Failure(new Exception("Could not write schema"))
		}
		} else {
		  _sender ! Failure(new Exception("Could not find schema"))
		}
  }
}
