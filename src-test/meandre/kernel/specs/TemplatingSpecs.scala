package meandre.kernel.specs

import meandre.kernel.specs.data.SpecsData
import org.specs.Specification
import com.mongodb.BasicDBObject
import java.io.{PrintWriter, ByteArrayOutputStream}
import meandre.webservices.Templating._
import xml.{Elem, XML}

/**
 * Basic templating specs for a the Templating object
 *
 * @author Xavier Llora
 * @date Feb 25, 2010 at 10:53:26 AM
 * 
 */

object TemplatingSpecs extends Specification {

  /**Simple aux function that serializes to text.
   *
   * @param format The format to serialize to
   * @param bdbo The basic object to serialize
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

    "be able to serialize heterogeneous objects" in {
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

}