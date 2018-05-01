// See the LICENCE.txt file distributed with this work for additional
// information regarding copyright ownership.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

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
    case Validate(id) =>
      val _sender = sender()
      _sender ! SchemaValidated("validateDocument", id, "success")

    case Upload(id) =>
      val _sender = sender()
      _sender ! SchemaUploaded("uploadSchema", id, "success")

    case Get(id) =>
      //closing over the sender in Future is not safe
      //http://helenaedelson.com/?p=879
      val _sender = sender()
      _sender ! Schema(id)
  }
}
