import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.slf4j.{LoggerFactory, Logger}

import scala.collection.immutable.HashMap


object JsonParser {

  protected val log: Logger = LoggerFactory.getLogger(getClass)
  val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)

  def getEventFromStringMessage(message:String): Option[Event] = {
    try{
      val map: HashMap[String, Any] = mapper.readValue(message,classOf[HashMap[String,Any]])
      val namespace = map.get("namespace").get.asInstanceOf[String]
      val name = map.get("name").get.asInstanceOf[String]
      val key: Option[String] = try{Some(map.get("key").get.asInstanceOf[String])}catch{
        case _ => None
      }
      val fields = map.get("fields").get.asInstanceOf[scala.collection.immutable.Map[String,Any]]
      Some(Event(namespace,name,key,fields))
    }catch{
      case e =>
        //todo:log this failed message to some file.
        log.error(s"invalid message.$message can not be parsed to Event due to $e")
        None
    }
  }

  def getEventFromStringMessage(namespace:String,name:String,fieldsString:String): Option[Event] = {
    try{
      val fields = mapper.readValue(fieldsString,classOf[scala.collection.immutable.Map[String,Any]])
      Some(Event(namespace,name,None,fields))
    }catch{
      case e =>
        //todo:log this failed message to some file.
        log.error(s"invalid message.$fieldsString can not be parsed to Event due to $e")
        None
    }
  }

  def getFieldTypes(message:String) = {
    val map: HashMap[String, Any] = mapper.readValue(message,classOf[HashMap[String,Any]])
    val fields = map.get("fields").get.asInstanceOf[scala.collection.immutable.Map[String,Any]]
    fields.foreach{case (key,value) => {
      value match {
        case value:scala.collection.immutable.Map[String, Any] => {
          println(s"##################  inner object ${key}  ############################")
          value.foreach{case (ke,va) => {
            if(va != null)println(s"    $ke ::: ${va.getClass}") else println(s"       $ke ::: null")
          }}
        }
        case _ =>
          if(value != null)println(s"$key ::: ${value.getClass}") else println(s"       $key ::: null")
      }
    }}
  }
}



case class Event(namespace:String, name:String, key:Option[String], fields:scala.collection.immutable.Map[String,Any])