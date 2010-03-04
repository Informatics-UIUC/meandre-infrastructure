package meandre.webservices.api

import meandre.kernel.Implicits._
import meandre.webservices.api.Templating._
import meandre.kernel.Configuration
import meandre.kernel.state.{LocationElement, Store}
import com.mongodb.{BasicDBList, BasicDBObject}

/**
 * The Meandre infrastructure implementation of the services API
 *
 * @author Xavier Llora
 * @date Mar 1, 2010 at 9:26:33 AM
 * 
 */


class MeandreInfrastructurePrivateAPI(cnf:Configuration) extends MeandreInfrastructureAbstractAPI {


  get("""/services/locations/add\.(json|xml|html)""".r, canonicalResponseType, tautologyGuard, public _) {
    val res:BasicDBObject = request.getRemoteUser match {
      case null => """{
          "status":"%s",
          "failure":{"message":"Unable to retrieve user"}
        }""".format(REQUEST_FAIL)
      case user => val store = Store(cnf,user,false)
                   val prms = paramsMap("location") zip paramsMap("description")
                   val addOutcome = prms map (
                       loc => (store.addElements(LocationElement(loc._1,loc._2)),loc)
                   )
                   val (bundle,added,failed) = (new BasicDBObject,new BasicDBList,new BasicDBList)
                   addOutcome foreach (
                       res => res._1.head match {
                         case Right(u) => u foreach (added add _.get )
                         case Left(t)  => failed.add(res._2._1)
                                          t.printStackTrace
                       }
                   )

                   if ( failed.size==0 ) {
                     bundle.put("status",REQUEST_OK)
                     val uris = new BasicDBObject
                     uris.put("added_uris",added)
                     bundle.put("success",uris)
                   }
                   else if ( added.size==0 ){
                     bundle.put("status",REQUEST_FAIL)
                     val uris = new BasicDBObject
                     uris.put("failed_uris",failed)
                     bundle.put("failure",uris)
                   }
                   else {
                     bundle.put("status",REQUEST_INCOMPLETE)
                     val uris = new BasicDBObject
                     uris.put("added_uris",added)
                     bundle.put("success",uris)
                     val urisf = new BasicDBObject
                     urisf.put("failed_uris",failed)
                     bundle.put("failure",urisf)
                   }
                   bundle
    }
    res serializeTo elements(0)
  }

  get("""/services/locations/list\.(json|xml|html)""".r, canonicalResponseType, tautologyGuard, public _) {
    val res:BasicDBObject = request.getRemoteUser match {
      case null => """{
          "status":"%s",
          "failure":{"message":"Unable to retrieve user"}
        }""".format(REQUEST_FAIL)
      case user => val locations = Store(cnf,user,false).locations
                   val res = new BasicDBObject
                   res.put("status",REQUEST_OK)
                   val list = new BasicDBList
                   locations foreach ( l => list.add(l._2) )
                   val labeled = new BasicDBObject
                   labeled.put("locations",list)
                   res.put("success",labeled)
                   res
    }
    res serializeTo elements(0)
  }


  //
  // The well known ping
  //
  get("""/services/test/ping\.(json|xml|html)""".r, canonicalResponseType, tautologyGuard, public _) {
    val res:BasicDBObject = """{
         "status":"OK",
         "success":{"message":"<b>test</b> pong!!!"}
    }"""
    res serializeTo elements(0)
  }


}