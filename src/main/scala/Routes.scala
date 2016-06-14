import akka.actor.{ActorRefFactory, Actor}
import spray.routing.{Route, HttpService}
import spray.routing.RejectionHandler.Default
import spray.json._


//create a normal class,then mixin httpServiceActor,this will connect the routing DSL to our actor hierarchy
//class MainService extends HttpServiceActor{
//  override def receive: Receive = {
//    case HttpRequest(GET, Uri.Path("/ping"), _, _, _) =>
//      sender ! HttpResponse(entity = "PONG")
//    case _ =>
//  }
//}

//create an actor ,then mix in HttpService,we should implement actorRefFactory to link routing dslw with our actor hierarchy
class Routes extends Actor with HttpService{
  override implicit def actorRefFactory: ActorRefFactory = context
  implicit val system = context.system
  //directives:eg:path,get etc
  //directive anotomy
//  name(arguments) { extractions =>
//    ... // inner Route
//  }
//  A directive does one or more of the following:
//  Transform the incoming RequestContext before passing it on to its inner Route
//  Filter the RequestContext according to some logic, i.e. only pass on certain requests and reject all others
//  Extract values from the RequestContext and make them available to its inner Route as “extractions”
//  Complete the request.

  // A RequestContext is the central object that is passed on through a route structure and, potentially, in between actors.
  // It’s immutable but light-weight and can therefore be copied quickly. When a directive receives a RequestContext instance
  // from the outside it can decide to pass this instance on unchanged to its inner Route or it can create a copy of the RequestContext instance, with one or more changes, and pass on this copy to its inner Route.

//  foo {
//    bar {
//      baz {
//        ctx => ctx.complete("Hello")
//      }
//    }
//  }

//  Assume that foo and baz “hook in” response transformation logic whereas bar leaves the responder of the RequestContext it receives unchanged before passing it on to its inner Route. This is what happens when the complete("Hello") is called:
//
//    The complete method creates an HttpResponse an sends it to responder of the RequestContext.
//  The response transformation logic supplied by the baz directive runs and sends its result to the responder of the RequestContext the baz directive received.
//  The response transformation logic supplied by the foo directive runs and sends its result to the responder of the RequestContext the foo directive received.
//  The responder of the original RequestContext, which is the sender ActorRef of the HttpRequest, receives the response and sends it out to the client.
//  As you can see all response handling logic forms a logic chain that directives can choose to “hook into”.

  case class Payload(message: String, withDefault: String)
  object JsonImplicits extends DefaultJsonProtocol {
    implicit val impPayload = jsonFormat2(Payload)
  }

  val routes: Route = {
    import JsonImplicits._
    import spray.httpx.SprayJsonSupport.sprayJsonUnmarshaller
    path("schema"){
      post{
        entity(as[Payload]){payload =>
          val avroSchema = SchemaBuilder.getAvroSchema(payload.message,payload.withDefault.toBoolean)
          complete(avroSchema)
        }
      }
    }~
    path("schemaBuilder"){
      post{
        formFields('message,'withDefault.as[Boolean]){(message:String,default:Boolean) =>
          val avroSchema = SchemaBuilder.getAvroSchema(message,default)
          complete(avroSchema)
        }
      }
    }~
    path(""){
      getFromFile("./src/main/resources/main.html")
    }
  }

  override def receive: Receive = runRoute(routes)

}

