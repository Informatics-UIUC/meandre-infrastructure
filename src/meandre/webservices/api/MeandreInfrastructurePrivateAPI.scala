package meandre.webservices.api

import meandre.Tools._
import meandre.kernel.Implicits._
import meandre.webservices.api.Templating._
import meandre.kernel.Configuration
import meandre.kernel.state.{LocationElement, Store}
import com.mongodb.{BasicDBList, BasicDBObject}
import meandre.state.Repository
import com.hp.hpl.jena.rdf.model.ModelFactory
import java.io.{ByteArrayOutputStream, StringReader}

/**
 * The Meandre infrastructure implementation of the services API
 *
 * @author Xavier Llora
 * @date Mar 1, 2010 at 9:26:33 AM
 *
 */


class MeandreInfrastructurePrivateAPI(cnf: Configuration) extends MeandreInfrastructureAbstractAPI(cnf) {

 
  // ---------------------------------------------------------------------------

  get("""/services/locations/add\.(json|xml|html)""".r, canonicalResponseType, tautologyGuard, public _) {
    requestFor {
      user =>
        if ( paramsMap.size==0 ) {
          val msg:BasicDBObject = """{"add_locations":"No locations present to add"}"""
          OKResponse(msg,user)
        }
        else {
          val store = Store(cnf, user, false)
          val prms = paramsMap("location") zip paramsMap("description")
          val addOutcome = prms map (
                  loc => (store.addElements(LocationElement(loc._1, loc._2)), loc)
                  )
          val (added, failed) = (new BasicDBList, new BasicDBList)
          addOutcome foreach (
                  res => res._1.head match {
                    case Right(u) => u foreach (added add _.get)
                    case Left(t) => failed.add(res._2._1)
                    t.printStackTrace
                  })
          if (failed.size == 0) {
            val uris = new BasicDBObject
            uris.put("added_uris", added)
            OKResponse(uris,user)
          }
          else if (added.size == 0) {
            val uris = new BasicDBObject
            uris.put("failed_uris", failed)
            FailResponse("Could not add any URIs", uris, user)
          }
          else {
            val uris = new BasicDBObject
            uris.put("added_uris", added)
            val urisf = new BasicDBObject
            urisf.put("failed_uris", failed)
            PartialFailResponse("Could not add some URIs", uris, urisf,user)
          }
        }
    }
  }


  // ---------------------------------------------------------------------------

  get("""/services/repository/regenerate\.(json|xml|html)""".r, canonicalResponseType, tautologyGuard, public _) {
    requestFor {
      user =>
        val store = Store(cnf, user, false)
        val locations = store.locations
        if ( locations.size==0 ) {
          val msg:BasicDBObject = """{"regenerate":"No locations present to regenerate"}"""
          OKResponse(msg,user)
        }
        else {
          store.removeAllLocations
          val sb = new StringBuffer("?")
          locations foreach (loc => {
            sb.append("&location="+loc._2.getString("location"))
            sb.append("&description="+loc._2.getString("description"))
          })

          val rd = request.getRequestDispatcher("/services/locations/add." + elements(0)+sb.toString)


          rd.forward(request, response)
          """{}"""
        }
    }
  }

  // ---------------------------------------------------------------------------

  get("""/services/locations/remove\.(json|xml|html)""".r, canonicalResponseType, tautologyGuard, public _) {
    requestFor {
      user =>
        user match {
          case user if paramsMap.contains("location") =>
            val store = Store(cnf, user, false)
            val prms = paramsMap("location")
            store.removeLocations(prms)
            val (response, removed) = (new BasicDBObject, new BasicDBList)
            prms foreach (removed add _)
            response.put("removed_locations", removed)
            OKResponse(response,user)
          case user => """{
            "status":"%s",
            "failure":{"message":"Missing location url to removeLocations"}
          }""".format(REQUEST_FAIL)
        }
    }
  }

  // ---------------------------------------------------------------------------

  get("""/services/locations/remove_all\.(json|xml|html)""".r, canonicalResponseType, tautologyGuard, public _) {
    requestFor {
      user =>
        val store = Store(cnf, user, false)
        val locations = store.locations
        store.removeAll
        val list = new BasicDBList
        locations foreach (l => list.add(l._2))
        val labeled = new BasicDBObject
        labeled.put("locations", list)
        OKResponse(labeled,user)
    }
  }

  // ---------------------------------------------------------------------------

  get("""/services/locations/list\.(json|xml|html)""".r, canonicalResponseType, tautologyGuard, public _) {
    requestFor {
      user =>
        val locations = Store(cnf, user, false).locations
        val list = new BasicDBList
        locations foreach (l => list.add(l._2))
        val labeled = new BasicDBObject
        labeled.put("locations", list)
        OKResponse(labeled,user)
    }
  }

  // ---------------------------------------------------------------------------

  get("""/services/repository/list_components\.(json|xml|html)""".r, canonicalResponseType, tautologyGuard, public _) {
    requestFor {
      user =>
        val st = Store(cnf, user, false)
        var cnd = "{}"
        val skip = if (params.contains("offset")) safeParseInt(params("offset")) else 0
        val limit = if (params.contains("limit")) safeParseInt(params("limit"))  else Math.MAX_INT            
        if (params.contains("order")) {
          cnd = params("order") match {
            case "name" => """{"%s":-1}""".format(K_NAME)
            case "date" => """{"%s":-1}""".format(K_DATE)
            case _ => "{}"
          }
        }
        val componentsMD = st.queryMetadata("{\"" + K_TYPE + "\": \"" + V_COMPONENT + "\""+"""}""",cnd,skip,limit)
        val list = new BasicDBList
        componentsMD foreach (
                metadata => {
                  val component:BasicDBObject = """{"uri":"%s","name":"%s"}""".format(metadata("uri"),metadata("name"))
                  val info: BasicDBObject = new BasicDBObject
                  metadata foreach (entry => info.put(entry._1, entry._2))
                  info removeField "type"
                  info removeField "name"
                  info removeField "uri"
                  component.put("metadata", info)
                  list add component
                }
                )
        val labeled = new BasicDBObject
        labeled.put("components", list)
        OKResponse(labeled,user)
    }
  }

  // ---------------------------------------------------------------------------

  get("""/services/repository/list_flows\.(json|xml|html)""".r, canonicalResponseType, tautologyGuard, public _) {
    requestFor {
      user =>
        val st = Store(cnf, user, false)
        var cnd = "{}"
        val skip = if (params.contains("offset")) safeParseInt(params("offset")) else 0
        val limit = if (params.contains("limit")) safeParseInt(params("limit"))  else Math.MAX_INT
        if (params.contains("order")) {
          cnd = params("order") match {
            case "name" => """{"%s":-1}""".format(K_NAME)
            case "date" => """{"%s":-1}""".format(K_DATE)
            case _ => "{}"
          }
        }
        val flowsMD = st.queryMetadata("{\"" + K_TYPE + "\": \"" + V_COMPONENT + "\""+"""}""",cnd,skip,limit)
        val list = new BasicDBList
        flowsMD foreach (
                metadata => {
                  val component: BasicDBObject = """{"uri":"%s","name":"%s"}""".format(metadata("uri"),metadata("name"))
                  val info: BasicDBObject = new BasicDBObject
                  metadata foreach (entry => info.put(entry._1, entry._2))
                  info removeField "type"
                  info removeField "name"
                  info removeField "uri"
                  component.put("metadata", info)
                  list add component
                }
                )
        val labeled = new BasicDBObject
        labeled.put("flows", list)
        OKResponse(labeled,user)
    }
  }

  // ---------------------------------------------------------------------------

  get("""/services/repository/clear\.(json|xml|html)""".r, canonicalResponseType, tautologyGuard, public _) {
    requestFor {
      user =>
        Repository(cnf,user).removeAll
        val labeled = new BasicDBObject
        labeled.put("repository", "All flows and components have been removed")
        OKResponse(labeled,user)
    }
  }

  // ---------------------------------------------------------------------------

  get("""/services/repository/tags\.(json|xml|html)""".r, canonicalResponseType, tautologyGuard, public _) {
    requestFor {
      user =>
        val tags = Repository(cnf,user).tagCloud
        val labeled = new BasicDBObject
        tags foreach ( kv=> labeled.put(kv._1,kv._2) )
        val wrapper = new BasicDBObject
        wrapper.put("tags",labeled)
        OKResponse(wrapper,user)
    }
  }

  // ---------------------------------------------------------------------------

  get("""/services/repository/tags_components\.(json|xml|html)""".r, canonicalResponseType, tautologyGuard, public _) {
    requestFor {
      user =>
        val tags = Repository(cnf,user).componentsTagCloud
        val labeled = new BasicDBObject
        tags foreach ( kv=> labeled.put(kv._1,kv._2) )
        val wrapper = new BasicDBObject
        wrapper.put("tags",labeled)
        OKResponse(wrapper,user)
    }
  }

  // ---------------------------------------------------------------------------

  get("""/services/repository/tags_flows\.(json|xml|html)""".r, canonicalResponseType, tautologyGuard, public _) {
    requestFor {
      user =>
        val tags = Repository(cnf,user).flowsTagCloud
        val labeled = new BasicDBObject
        tags foreach ( kv=> labeled.put(kv._1,kv._2) )
        val wrapper = new BasicDBObject
        wrapper.put("tags",labeled)
        OKResponse(wrapper,user)
    }
  }

  // ---------------------------------------------------------------------------

  get("""/services/repository/search_components\.(json|xml|html)""".r, canonicalResponseType, tautologyGuard, public _) {
    requestFor {
      user =>
        if (!params.contains("q")) {
          val emptySearch = new BasicDBObject
          emptySearch.put("query_results", "No searchable terms provided!")
          OKResponse(emptySearch,user)
        }
        else {
          val st = Store(cnf, user, false)
          var q = params("q").replace("\"|'", " ").split("""\s+""")
          if (q.size == 0) {
            val emptySearch = new BasicDBObject
            emptySearch.put("query_results", "No searchable terms provided!")
            OKResponse(emptySearch,user)
          }
          else {
            val qs = if (q.size == 1) "\"" + q(0) + "\""
                     else q.map(s => "\"" + s + "\"").reduceLeft(_ + ',' + _)
            val skip = if (params.contains("offset")) safeParseInt(params("offset")) else 0
            val limit = if (params.contains("limit")) safeParseInt(params("limit"))  else Math.MAX_INT
            var cnd = "{}"
            if (params.contains("order")) {
              cnd = params("order") match {
                case "name" => """{"%s":-1}""".format(K_NAME)
                case "date" => """{"%s":-1}""".format(K_DATE)
                case _ => "{}"
              }
            }
            val componentsMD = st.queryMetadata("{\"" + K_TYPE + "\": \"" + V_COMPONENT + "\""+""","_tokens":{"$all":[%s]}}""".format(qs),cnd, skip, limit)
            val list = new BasicDBList
            componentsMD foreach (
                    metadata => {
                      val component: BasicDBObject = """{"uri":"%s","name":"%s"}""".format(metadata("uri"), metadata("name"))
                      val info: BasicDBObject = new BasicDBObject
                      metadata foreach (entry => info.put(entry._1, entry._2))
                      info removeField "type"
                      info removeField "name"
                      info removeField "uri"
                      component.put("metadata", info)
                      list add component
                    }
                    )
            val labeled = new BasicDBObject
            labeled.put("components", list)
            OKResponse(labeled,user)
          }
        }
    }
  }

  // ---------------------------------------------------------------------------

  get("""/services/repository/search_flows\.(json|xml|html)""".r, canonicalResponseType, tautologyGuard, public _) {
    requestFor {
      user =>
        if (!params.contains("q")) {
          val emptySearch = new BasicDBObject
          emptySearch.put("query_results", "No searchable terms provided!")
          OKResponse(emptySearch,user)
        }
        else {
          val st = Store(cnf, user, false)
          var q = params("q").replace("\"|'", " ").split("""\s+""")
          if (q.size == 0) {
            val emptySearch = new BasicDBObject
            emptySearch.put("query_results", "No searchable terms provided!")
            OKResponse(emptySearch,user)
          }
          else {
            val qs = if (q.size == 1) "\"" + q(0) + "\""
                     else q.map(s => "\"" + s + "\"").reduceLeft(_ + ',' + _)
            val skip = if (params.contains("offset")) safeParseInt(params("offset")) else 0
            val limit = if (params.contains("limit")) safeParseInt(params("limit"))  else Math.MAX_INT
            var cnd = "{}"
            if (params.contains("order")) {
              cnd = params("order") match {
                case "name" => """{"%s":-1}""".format(K_NAME)
                case "date" => """{"%s":-1}""".format(K_DATE)
                case _ => "{}"
              }
            }
            val flowsMD = st.queryMetadata("{\"" + K_TYPE + "\": \"" + V_FLOW + "\""+""","_tokens":{"$all":[%s]}}""".format(qs),cnd, skip, limit)
            val list = new BasicDBList
            flowsMD foreach (
                    metadata => {
                      val flow: BasicDBObject = """{"uri":"%s","name":"%s"}""".format(metadata("uri"), metadata("name"))
                      val info: BasicDBObject = new BasicDBObject
                      metadata foreach (entry => info.put(entry._1, entry._2))
                      info removeField "type"
                      info removeField "name"
                      info removeField "uri"
                      flow.put("metadata", info)
                      list add flow
                    }
                    )
            val labeled = new BasicDBObject
            labeled.put("flows", list)
            OKResponse(labeled,user)
          }
        }
    }
  }

  // ---------------------------------------------------------------------------

  get("""/services/repository/search\.(json|xml|html)""".r, canonicalResponseType, tautologyGuard, public _) {
    requestFor {
      user =>
        if (!params.contains("q")) {
          val emptySearch = new BasicDBObject
          emptySearch.put("query_results", "No searchable terms provided!")
          OKResponse(emptySearch,user)
        }
        else {
          val st = Store(cnf, user, false)
          var q = params("q").replace("\"|'", " ").split("""\s+""")
          if (q.size == 0) {
            val emptySearch = new BasicDBObject
            emptySearch.put("query_results", "No searchable terms provided!")
            OKResponse(emptySearch,user)
          }
          else {
            val qs = if (q.size == 1) "\"" + q(0) + "\""
                     else q.map(s => "\"" + s + "\"").reduceLeft(_ + ',' + _)
            val skip = if (params.contains("offset")) safeParseInt(params("offset")) else 0
            val limit = if (params.contains("limit")) safeParseInt(params("limit"))  else Math.MAX_INT
            var cnd = "{}"
            if (params.contains("order")) {
              cnd = params("order") match {
                case "name" => """{"%s":-1}""".format(K_NAME)
                case "date" => """{"%s":-1}""".format(K_DATE)
                case _ => "{}"
              }
            }
            val flowsMD = st.queryMetadata("""{"_tokens":{"$all":[%s]}}""".format(qs),cnd, skip, limit)
            val list = new BasicDBList
            flowsMD foreach (
                    metadata => {
                      val flow: BasicDBObject = """{"uri":"%s","name":"%s"}""".format(metadata("uri"), metadata("name"))
                      val info: BasicDBObject = new BasicDBObject
                      metadata foreach (entry => info.put(entry._1, entry._2))
                      //info removeField "type"
                      info removeField "name"
                      info removeField "uri"
                      flow.put("metadata", info)
                      list add flow
                    }
                    )
            val labeled = new BasicDBObject
            labeled.put("repository", list)
            OKResponse(labeled,user)
          }
        }
    }
  }

  // ---------------------------------------------------------------------------

  get("""/services/repository/describe\.(rdf|ttl|nt)""".r, canonicalResponseType, tautologyGuard, public _) {
    requestFor {
      user =>
         val rdfType = elements(0) match {
           case "ttl" => "TURTLE"
           case "nt"  => "N3"
           case _     => "RDF/XML-ABBREV"
         }
         val st = Store(cnf, user, false)
         val res = new BasicDBObject
         params.contains("uri") match {
           case true  => st.getRDFForURI(params("uri"),elements(0)) match {
                            case None     => FailResponse(params("uri")+" no a known URI", res, user)
                            case Some(ba) => res.put(elements(0),new String(ba)) ; OKResponse(res,user)
                         }
           case false => val rdf = new StringBuffer(10000)
                         st.getAllRDF(None,"nt").foldLeft(rdf)((a,b)=>a.append(new String(b)+'\n'))
                         rdfType match {
                           case "N3" => res.put(elements(0),rdf.toString) ; OKResponse(res,user)
                           case _    => val model = ModelFactory.createDefaultModel
                                        model.read(new StringReader(rdf.toString),null,"N-TRIPLE")
                                        val baos = new ByteArrayOutputStream
                                        model.write(baos,rdfType)
                                        res.put(elements(0),baos.toString) ; OKResponse(res,user)
                         }
         }
    }
  }


  // ---------------------------------------------------------------------------

  get("""/services/repository/describe.(html)""".r, canonicalResponseType, tautologyGuard, public _) {
    requestFor {
      user =>
        params.contains("uri") match {
          case false => FailResponse("Missing required URIs of the components/flows to remove", new BasicDBObject, user)
          case true  => val descs = new BasicDBList
                        val st = Store(cnf, user, false)
                        paramsMap("uri").foreach(
                          uri => st.descriptorFor(uri) match {
                            case None =>
                            case Some(descriptor) => descs add descriptor2BasicDBObject(descriptor)
                          }
                        )
                        val res = new BasicDBObject
                        res.put("descriptors",descs) ; OKResponse(res,user)
        }
    }
  }


  // ---------------------------------------------------------------------------

  get("""/services/repository/remove\.(json|xml|html)""".r, canonicalResponseType, tautologyGuard, public _) {
    requestFor {
      user =>
        params.contains("uri") match {
          case false => FailResponse("Missing required URIs of the components/flows to remove", new BasicDBObject, user)
          case true  => val uris = new BasicDBList
                        val st = Store(cnf, user, false)
                        paramsMap("uri").foreach(
                          uri => { st.remove(uri) ; uris.add(uri) }
                        )
                        val res = new BasicDBObject
                        res.put("uris",uris) ; OKResponse(res,user)
        }
    }
  }

  // ---------------------------------------------------------------------------


}