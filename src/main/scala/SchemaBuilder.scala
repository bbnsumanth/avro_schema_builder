
object SchemaBuilder {
  def getType(typ: Class[_]) = {
    val typeString = typ.toString.split("\\.").last
    typeString match{
      case "Integer" => "int"
      case "String" => "string"
      case "Boolean" => "boolean"
      case "Long" => "long"
      case "Float" => "float"
      case "Double" => "double"
      case "Array[Bytes]" => "bytes"
      case _ =>  s"invalid type $typeString"

    }
  }
  def getfields(fields: Map[String, Any],withDefaultNull:Boolean):String = {
    val fieldsSchema = new StringBuilder
    fields.foreach{case (key,value) => {
      if(value != null){
        value match {
          case v: scala.collection.immutable.Map[String,Any] =>
            fieldsSchema ++= s"""{"name":"${key}" , "type":{"type":"record","name":"${key}","fields":[${getfields(v,withDefaultNull)}] } },"""
          case _ =>
            withDefaultNull match{
              case true => fieldsSchema ++= s"""{"name": "$key","type": ["null", "${getType(value.getClass)}"]},"""
              case false => fieldsSchema ++= s"""{"name": "$key","type": "${getType(value.getClass)}"},"""
            }
        }
      }else{
        withDefaultNull match{
          case true => fieldsSchema ++= s"""{"name": "$key","type": ["null", "****"]},"""
          case false => fieldsSchema ++= s"""{"name": "$key","type": "****"},"""
        }
      }
    }}
    fieldsSchema.substring(0,fieldsSchema.length-1)
  }
  def getAvroSchema(jsonString:String,withDefaultNull:Boolean = true)={
    val event: Option[Event] = JsonParser.getEventFromStringMessage(jsonString)
    if(event.isDefined){
      s"""{"namespace":"${event.get.namespace}","type": "record","name": "${event.get.name}","fields": [${getfields(event.get.fields,withDefaultNull)}]}"""
    }else{
      "invalid json message"
    }
  }
}
