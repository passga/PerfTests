package com.bonitasoft.engine.perf.utils


import java.util

import org.bonitasoft.engine.bpm.contract.FileInputValue
import org.json4s.jackson.JsonMethods._
/**
  * @author Pablo Alonso de Linaje García
  */
object ContractUtils {

  def serialize (any: Any) : java.io.Serializable = {
    return any.asInstanceOf[java.io.Serializable]
  }

  def newFile (any: String) : java.io.Serializable = {
    val file:FileInputValue = new FileInputValue(any, "text/plain", any.getBytes())
    return file.asInstanceOf[java.io.Serializable]
  }

  /**
    * It does not work
    * org.json4s.package$MappingException: No constructor for type Map[String, Serializable], JObject(List((input2,JInt(22)), (input1,JString(this is my data))))    *
    * @param json
    * @return
    */
  def contractFromJson(json: String) : util.HashMap[String, java.io.Serializable] = {
    val stepContract :util.HashMap[String, java.io.Serializable] = new util.HashMap[String, java.io.Serializable]()

    implicit val formats = org.json4s.DefaultFormats
    stepContract.putAll(parse(json).extract[util.Map[String, java.io.Serializable]]);
    return stepContract
  }
}
