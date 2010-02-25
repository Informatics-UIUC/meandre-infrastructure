package meandre.kernel.specs.data

import meandre.Implicits._
import meandre.webservices._
import meandre.webservices.Templating._
import com.mongodb._

/**
 * Contains miscellaneous data used for some of the specifications
 *
 * @author Xavier Llora
 * @date Feb 25, 2010 at 10:57:50 AM
 * 
 */

object SpecsData {

  val heterogeneousResponse:BasicDBObject = """
       {
         "hello":"world",
         "vegies": ["french","fries","ketchup"],
         "owner":{"name":"joe"},
         "emptylist":[],
         "emptyobject":{},
         "complex_list":[
                  [1,2,3],
                  [4,5,6],
                  {"type":"Complex list"}
                 ]
       }"""


}