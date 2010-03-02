package meandre.kernel.specs

import meandre.kernel.specs.data.SpecsData
import meandre.kernel.Implicits._
import org.specs.Specification
import com.mongodb.BasicDBObject
import java.io.{PrintWriter, ByteArrayOutputStream}
import meandre.webservices._
import meandre.webservices.api.Templating._
import xml.{Elem, XML}

/**
 * Basic templating specs for a the Templating object
 *
 * @author Xavier Llora
 * @date Feb 25, 2010 at 10:53:26 AM
 * 
 */

object TemplatingSpecs extends Specification("The templating specification") {

  /**Simple aux function that serializes to text.
   *
   * @param format The format to serializeTo to
   * @param bdbo The basic object to serializeTo
   * @return The string containing the serialized object
   */
  protected def serialize ( format:String, bdbo:BasicDBObject ) = {
    val baos = new ByteArrayOutputStream()
    val pw   = new PrintWriter(baos,true)
    bdbo.format(format,pw)
    baos.toString
  }

  "JSON templating" should {

    "return the same object it received" in {
      val response = SpecsData.heterogeneousResponse
      response.asJSON must beEqualTo(response.toString)
    }

  }

  "XML templating" should {

    "be able to serializeTo heterogeneous objects" in {
      val response   = SpecsData.heterogeneousResponse
      val serialized = serialize("xml",response)
      serialized.length must beEqualTo(1027)
      val parsedXML  = XML.loadString(serialized)
      (parsedXML\\"hello").text must beEqualTo("world")
      (parsedXML\\"vegies"\\"meandre_list_item").size must beEqualTo(3)
      (parsedXML\\"owner"\\"name").text must beEqualTo("joe")
      (parsedXML\\"complex_list"\\"type").text must beEqualTo("Complex list")
      (parsedXML\\"emptylist"\\"meandre_list_item").size must beEqualTo(0)
      (parsedXML\\"emptyobject"\\"meandre_item").first.descendant.head.text.trim.length must beEqualTo(0)
    }
  }


  "HTML templating" should {

    "be able to serializeTo successful reponse objects" in {
      val response:BasicDBObject = """{"status":"OK"}"""
      response.put("success",SpecsData.heterogeneousResponse)
      val serialized = serialize("html",response)
      serialized.length must beGreaterThan(12700)
      serialized.indexOf("""<h3 class="response">Response status <span class="response-success">OK</span></h3>""") must beGreaterThan(0)
      serialized.indexOf("""<td class="dockey">vegies</td><td class="docvalue">""") must beGreaterThan(0)
      serialized.indexOf("""<td class="dockey">type</td><td class="docvalue">""") must beGreaterThan(0)
    }


    "be able to serializeTo failure reponse objects" in {
      val response:BasicDBObject = """{"status":"INCOMPLETE"}"""
      response.put("message","Wow couldn't do it all!!!")
      response.put("success",SpecsData.heterogeneousResponse)
      val failResponse:BasicDBObject = """{"failed message":"Oops"}"""
      response.put("failure",failResponse)
      val serialized = serialize("html",response)
      serialized.length must beGreaterThan(13100)
      serialized.indexOf("""<h3 class="response">Response status <span class="response-incomplete">INCOMPLETE</span></h3>""") must beGreaterThan(0)
      serialized.indexOf("""Wow couldn't do it all!!!""") must beGreaterThan(0)
      serialized.indexOf("""failed message""") must beGreaterThan(0)
      serialized.indexOf("""Oops""") must beGreaterThan(0)
      serialized.indexOf("""<td class="dockey">type</td><td class="docvalue">""") must beGreaterThan(0)
    }


    "be able to serializeTo partial reponse objects" in {
      val response:BasicDBObject = """{"status":"INCOMPLETE"}"""
      response.put("message","Failed misserably")
      response.put("failure",SpecsData.heterogeneousResponse)
      val serialized = serialize("html",response)
      serialized.length must beGreaterThan(12900)
      serialized.indexOf("""<h3 class="response">Response status <span class="response-incomplete">INCOMPLETE</span></h3>""") must beGreaterThan(0)
      serialized.indexOf("""Failed misserably""") must beGreaterThan(0)
      serialized.indexOf("""<td class="dockey">type</td><td class="docvalue">""") must beGreaterThan(0)
    }


  }


}