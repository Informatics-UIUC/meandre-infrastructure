package meandre.webservices.api

import meandre.Tools._
import meandre.kernel.Implicits._
import meandre.webservices.api.Templating._
import meandre.kernel.Configuration
import meandre.state.Repository
import org.apache.commons.fileupload.disk.DiskFileItemFactory
import org.apache.commons.fileupload.servlet.ServletFileUpload
import org.apache.commons.fileupload.FileItem
import com.hp.hpl.jena.rdf.model.{Model, ModelFactory}
import java.io.{ByteArrayInputStream, InputStream, ByteArrayOutputStream, StringReader}
import meandre.kernel.state.{BundledElement, LocationElement, Store}
import meandre.kernel.rdf._
import com.mongodb.{Mongo, BasicDBList, BasicDBObject}
import meandre.webservices.realm.MongoDBRealm
import com.mongodb.util.JSON
import snare.Snare
import meandre.webservices.logger.Logger
import meandre.kernel.execution.{ExecutionWrapper, JobQueue}

/**
 * The Meandre infrastructure implementation of the services API
 *
 * @author Xavier Llora
 * @date Mar 1, 2010 at 9:26:33 AM
 *
 */


class MeandreInfrastructurePrivateAPI(cnf: Configuration, snareMon:Snare, log:Logger) extends MeandreInfrastructureAbstractAPI(cnf) {

 
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
        val flowsMD = st.queryMetadata("{\"" + K_TYPE + "\": \"" + V_FLOW + "\""+"""}""",cnd,skip,limit)
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
                            case None     => FailResponse(params("uri")+" not a known URI", res, user)
                            case Some(ba) => res.put(elements(0),uriRewrite(new String(ba))) ; OKResponse(res,user)
                         }
           case false => val rdf = new StringBuffer(10000)
                         st.getAllRDF(None,"nt").foldLeft(rdf)((a,b)=>a.append(new String(b)+'\n'))
                         rdfType match {
                           case "N3" => res.put(elements(0),uriRewrite(rdf.toString)) ; OKResponse(res,user)
                           case _    => val model = ModelFactory.createDefaultModel
                                        model.read(new StringReader(rdf.toString),null,"N-TRIPLE")
                                        val baos = new ByteArrayOutputStream
                                        model.write(baos,rdfType)
                                        res.put(elements(0),uriRewrite(baos.toString)) ; OKResponse(res,user)
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
                        if ( descs.size>0 ) {
                          val res = new BasicDBObject
                          res.put("descriptors",descs) ; OKResponse(res,user)
                        }
                        else {
                          FailResponse("Unknow URI %s" format params("uri"), new BasicDBObject, user)
                        }
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

  post("""/services/repository/add\.(json|xml|html)""".r, canonicalResponseType, tautologyGuard, public _) {
    requestFor {
      user =>

        //
        // TODO Currently deprecating embedding, should I rethink this decision?
        //
        //var embed = false
        var overwrite = false
        val upload = new ServletFileUpload(new DiskFileItemFactory)
        val lstItems = upload parseRequest request
        val itr = lstItems.iterator

        var descs: List[Descriptor] = Nil
        var cntxs: Map[String, (String,Array[Byte])] = Map()

        while (itr.hasNext) {
          val item = itr.next.asInstanceOf[FileItem]

          // Get the name of the field
          val fieldName = item.getFieldName

          // check if the current item is a form field or an uploaded file
          if (fieldName equals "repository") {
            val data = item.getString
            val sRDFContent = new String(data)
            if (sRDFContent.length > 0)
              descs = descs ++ DescriptorsFactory.buildDescriptors(DescriptorsFactory.readModelFromText(sRDFContent))
          }
          else if (fieldName equals "context") {
            val sFile = item.getName
            val data = item.get
            if (data.length > 0) {
              cntxs += sFile -> (item.getContentType,data)
            }
          }
//          else if (fieldName equals "embed") {
//            val sValue = item.getString.trim
//            if (sValue.length > 0)
//              embed = sValue match {
//                case "true" => true
//                case _ => false
//              }
//          }
          else if (fieldName equals "overwrite") {
            val sValue = item.getString.trim
            if (sValue.length > 0)
              overwrite = sValue match {
                case "true" => true
                case _ => false
              }
          }
        }

        if ( descs.isEmpty )
          FailResponse("Missing required repository field", new BasicDBObject, user)
        else {
          val st = Store(cnf, user, overwrite)
          val scntxs = cntxs.toList.sort( (a,b) => (a._1 compareTo b._1) < 0 )
          val names = scntxs.map(_._1)
          val types = scntxs.map(_._2._1)

          val out = descs.map( _ match {
            case c:ComponentDescriptor =>
              val iss = scntxs.map( kv => new ByteArrayInputStream(kv._2._2) )
              st.addElements(BundledElement(c,names,types,iss))

            case f:FlowDescriptor =>
              st.addElements(BundledElement(f,Nil,Nil,Nil))
          })

          // Return type
          val res = new BasicDBObject
          val uris = new BasicDBList
          out.foreach(_.foreach(_ match {
            case Right(s) => s.foreach(_ match {
              case Some(uri) => uris add uri
              case _ =>
            })
            case _ =>
          }))
          res.put("uris", uris )
          OKResponse(res, user)
        }

    }
  }

  // ---------------------------------------------------------------------------

  get("""/services/repository/integrity\.(json|xml|html)""".r, canonicalResponseType, tautologyGuard, public _) {
    requestFor {
      user =>
        val st = Store(cnf, user, false)
        //
        // Check for missing components
        //
        val missingComponents = new BasicDBObject
        st.flows.foreach( f => {
          val mc = new BasicDBList
          f.instances.foreach(
            cid => if (!st.exist(cid.componentUri)) mc add cid.componentUri
          )
          if ( !mc.isEmpty )
            missingComponents.put(f.uri,mc)
        })
        //
        // Check for missing contexts
        //
        val missingContexts = new BasicDBObject
        val outsiders = new BasicDBList
        st.components.foreach ( c => {
          val mc = new BasicDBList
          c.context.foreach ( _ match {
            case URIContext(uri) if (uri.startsWith("context://localhost") && !st.containsContext(uri)) => mc add uri
            case URIContext(uri) if (!uri.startsWith("context://localhost") && !uri.startsWith(c.uri)) => outsiders add uri
            case _ =>
          })
          if ( !mc.isEmpty )
            missingContexts.put(c.uri,mc)
        })

        //
        // Generate the response
        //
        val res = new BasicDBObject
        if ( !missingComponents.isEmpty ) res.put("missing components", missingComponents)
        if ( !missingContexts.isEmpty) res.put("missing contexts", missingContexts)
        if ( !outsiders.isEmpty) res.put("external contexts", outsiders)
        OKResponse(res, user)

    }
  }

  // ---------------------------------------------------------------------------
  
  /** The mongo db connection object */
  protected val mongoDbRealm = MongoDBRealm(cnf)


  // ---------------------------------------------------------------------------

  get("""/services/security/valid_roles\.(json|xml|html)""".r, canonicalResponseType, tautologyGuard, public _) {
    requestFor {
      user =>
        val roles = new BasicDBList
        MongoDBRealm.AVAILABLE_ROLES.foreach(roles add _)
        val res = new BasicDBObject
        res.put("roles", roles)
        OKResponse(res, user)
    }
  }
  
  // ---------------------------------------------------------------------------

  get("""/services/security/user\.(json|xml|html)""".r, canonicalResponseType, tautologyGuard, public _) {
    requestFor {
      user =>
        val userPrincipal = request.getUserPrincipal.asInstanceOf[BasicDBObject]

        //
        // Render Output
        //
        val res = new BasicDBObject
        val screenName = userPrincipal getString "_id"
        userPrincipal.put("screenname",screenName)
        userPrincipal removeField "_id"
        userPrincipal removeField "_ns"
        userPrincipal removeField "authenticated"
        userPrincipal.put("password","*******")
        res.put("user", userPrincipal)
        OKResponse(res, user)
    }
  }
  
  // ---------------------------------------------------------------------------

  get("""/services/security/users\.(json|xml|html)""".r, canonicalResponseType, tautologyGuard, public _) {
    requestFor {
      user =>
        val userPrincipal = request.getUserPrincipal.asInstanceOf[BasicDBObject]
        var ps:List[BasicDBObject] = List(userPrincipal)

        if ( request.isUserInRole("admin") ) {
          val db:BasicDBObject = """{ "_id" : { "$ne" : "%s" } }""" format userPrincipal.getString("_id")
          ps = ps ++ (mongoDbRealm listUsers db)
        }

        //
        // Render Output
        //
        val res = new BasicDBObject
        val users = new BasicDBList
        ps.foreach(up => {
          val screenName = up getString "_id"
          up removeField "_id"
          up removeField "_ns"
          up removeField "authenticated"
          up.put("password","*******")
          val userObject = new BasicDBObject
          userObject.put(screenName,up)
          users add userObject
        })
        res.put("users", users)
        OKResponse(res, user)
    }
  }


  // ---------------------------------------------------------------------------

  post("""/services/security/add\.(json|xml|html)""".r, canonicalResponseType, tautologyGuard, public _) {
    def obj(sn:String,key:String,value:String) = {
      val o = new BasicDBObject
      o.put("screen_name",sn)
      o.put(key,value)
      o
    }

    requestFor {
      user =>
        if ( !request.isUserInRole("admin") ) {
          FailResponse("You need to belong to the admin role to create users", new BasicDBObject, user)
        }
        else if ( !params.contains("screen_name") || !params.contains("roles") ||
                  !params.contains("profile") || !params.contains("password") ) {
          FailResponse("Missing parammeters [screen_name,roles,profile,password] all required", new BasicDBObject, user)
        }
        else {
          val users = zip4(paramsMap("screen_name").toList,paramsMap("roles").toList,paramsMap("profile").toList,paramsMap("password").toList)
          val succ  = new BasicDBList
          val fail  = new BasicDBList
          users.foreach( _ match {
            case (sn,rls,prf,pswd) =>
              if ( mongoDbRealm existsUser sn ) fail add obj(sn,"reason","duplicated screen name")
              else if ( rls.split(",").exists( (a) => !MongoDBRealm.AVAILABLE_ROLES.contains(a)) ) fail add obj(sn,"reason","invalid role")
              else if ( try { JSON.parse(prf); false} catch { case _ => true} ) fail add obj(sn,"reason","invalid profile")
              else {
                mongoDbRealm.addUser(sn,rls.split(",").toList.map(_.trim),JSON.parse(prf).asInstanceOf[BasicDBObject],pswd)
                succ add obj(sn,"added","OK")
              }
          })

          val res = new BasicDBObject
          if ( fail.isEmpty ) {
            res.put("added",succ)
            OKResponse(res, user)
          }
          else if ( succ.isEmpty ) {
            res.put("failed",fail)
            FailResponse("Failed to create users",res, user)
          }
          else {
            val (so,sf) = (new BasicDBObject,new BasicDBObject)
            so.put("users",succ)
            sf.put("users",fail)
            PartialFailResponse("Some users could not be created", so, sf,user)
          }
        }
      }
  }


  // ---------------------------------------------------------------------------

  get("""/services/security/remove\.(json|xml|html)""".r, canonicalResponseType, tautologyGuard, public _) {
    def obj(sn:String,key:String,value:String) = {
      val o = new BasicDBObject
      o.put("screen_name",sn)
      o.put(key,value)
      o
    }

    requestFor {
      user =>
        if ( !request.isUserInRole("admin") ) {
          FailResponse("You need to belong to the admin role to remove users", new BasicDBObject, user)
        }
        else if ( !params.contains("screen_name") ) {
          FailResponse("Missing parammeter screen_name", new BasicDBObject, user)
        }
        else {
          val users = paramsMap("screen_name").toList
          val succ  = new BasicDBList
          val fail  = new BasicDBList
          users.foreach( _ match {
            case sn =>
              if ( !(mongoDbRealm existsUser sn) ) fail add obj(sn,"reason","unknown screen name")
              else {
                mongoDbRealm.removeUser(sn)
                succ add obj(sn,"removed","OK")
              }
          })

          val res = new BasicDBObject
          if ( fail.isEmpty ) {
            res.put("added",succ)
            OKResponse(res, user)
          }
          else if ( succ.isEmpty ) {
            res.put("failed",fail)
            FailResponse("Failed to remove some users",res, user)
          }
          else {
            val (so,sf) = (new BasicDBObject,new BasicDBObject)
            so.put("users",succ)
            sf.put("users",fail)
            PartialFailResponse("Some users could not be removed", so, sf,user)
          }
        }
      }
  }


  // ---------------------------------------------------------------------------

  get("""/services/security/grant_roles\.(json|xml|html)""".r, canonicalResponseType, tautologyGuard, public _) {
   def obj(sn:String,key:String,value:String) = {
      val o = new BasicDBObject
      o.put("screen_name",sn)
      o.put(key,value)
      o
    }

   requestFor {
      user =>
        if ( !request.isUserInRole("admin") ) {
          FailResponse("You need to belong to the admin role to grant user roles", new BasicDBObject, user)
        }
        else if ( !params.contains("screen_name") || !params.contains("roles") ) {
          FailResponse("Missing parammeter screen_name or roles", new BasicDBObject, user)
        }
        else {
          val actions = paramsMap("screen_name").toList.zip(paramsMap("roles").toList)
          val succ = new BasicDBList
          val fail = new BasicDBList
          val vr = Set(MongoDBRealm.AVAILABLE_ROLES:_*)
          actions.foreach(
            ar => {
              val (scren_name,roles) = (ar._1,ar._2.split(",").toList.map(_.trim))
              if ( !mongoDbRealm.existsUser(scren_name) )
                fail add obj(scren_name,"reason","unknown screen name")
              else
                roles.foreach(
                  r => {
                    if ( !(vr contains r) )
                      fail add obj(scren_name,"reason","unknown role %s" format r)
                    else {
                      mongoDbRealm.addRoleToUser(scren_name,r)
                      succ add obj(scren_name,"granted role",r)
                    }
                  }
                )
            }
          )

          val res = new BasicDBObject
          if (fail.isEmpty) {
            res.put("granted", succ)
            OKResponse(res, user)
          }
          else if (succ.isEmpty) {
            res.put("failed", fail)
            FailResponse("Failed to grant roles to users", res, user)
          }
          else {
            val (so, sf) = (new BasicDBObject, new BasicDBObject)
            so.put("users", succ)
            sf.put("users", fail)
            PartialFailResponse("Some roles could not be granted", so, sf, user)
          }
        }
    }
  }


  // ---------------------------------------------------------------------------

  get("""/services/security/revoke_roles\.(json|xml|html)""".r, canonicalResponseType, tautologyGuard, public _) {
   def obj(sn:String,key:String,value:String) = {
      val o = new BasicDBObject
      o.put("screen_name",sn)
      o.put(key,value)
      o
    }

   requestFor {
      user =>
        if ( !request.isUserInRole("admin") ) {
          FailResponse("You need to belong to the admin role to revoke user roles", new BasicDBObject, user)
        }
        else if ( !params.contains("screen_name") || !params.contains("roles") ) {
          FailResponse("Missing parammeter screen_name or roles", new BasicDBObject, user)
        }
        else {
          val actions = paramsMap("screen_name").toList.zip(paramsMap("roles").toList)
          val succ = new BasicDBList
          val fail = new BasicDBList
          val vr = Set(MongoDBRealm.AVAILABLE_ROLES:_*)
          actions.foreach(
            ar => {
              val (scren_name,roles) = (ar._1,ar._2.split(",").toList.map(_.trim))
              if ( !mongoDbRealm.existsUser(scren_name) )
                fail add obj(scren_name,"reason","unknown screen name")
              else
                roles.foreach(
                  r => {
                    if ( !(vr contains r) )
                      fail add obj(scren_name,"reason","unknown role %s" format r)
                    else {
                      mongoDbRealm.revokeRoleFromUser(scren_name,r)
                      succ add obj(scren_name,"revoked role",r)
                    }
                  }
                )
            }
          )

          val res = new BasicDBObject
          if (fail.isEmpty) {
            res.put("revoked", succ)
            OKResponse(res, user)
          }
          else if (succ.isEmpty) {
            res.put("failed", fail)
            FailResponse("Failed to revoke roles to users", res, user)
          }
          else {
            val (so, sf) = (new BasicDBObject, new BasicDBObject)
            so.put("users", succ)
            sf.put("users", fail)
            PartialFailResponse("Some roles could not be revoked", so, sf, user)
          }
        }
    }
  }


  // ---------------------------------------------------------------------------

  post("""/services/security/update\.(json|xml|html)""".r, canonicalResponseType, tautologyGuard, public _) {
    def obj(sn:String,key:String,value:String) = {
      val o = new BasicDBObject
      o.put("screen_name",sn)
      o.put(key,value)
      o
    }

    requestFor {
      user =>
        if (!params.contains("screen_name") || !params.contains("roles") ||
               !params.contains("profile") || !params.contains("password")) {
          FailResponse("Missing parammeters [screen_name,roles,profile,password] all required", new BasicDBObject, user)
        }
        else if ( user!=request.getRemoteUser && !request.isUserInRole("admin")  ) {
          FailResponse("You need to belong to the admin role to update other user information", new BasicDBObject, user)
        }
        else {
          val users = zip4(paramsMap("screen_name").toList,paramsMap("roles").toList,paramsMap("profile").toList,paramsMap("password").toList)
          val succ  = new BasicDBList
          val fail  = new BasicDBList
          users.foreach( _ match {
            case (sn,rls,prf,pswd) =>
              if ( !(mongoDbRealm existsUser sn) ) fail add obj(sn,"reason","unexistent screen name")
              else if ( rls.trim.length>0 && rls.split(",").exists( (a) => !MongoDBRealm.AVAILABLE_ROLES.contains(a)) ) fail add obj(sn,"reason","invalid role")
              else if ( try { if (prf.length>0) {JSON.parse(prf); false} else false} catch { case _ => true} ) fail add obj(sn,"reason","invalid profile")
              else {
                mongoDbRealm.updateUser(sn,rls.split(",").toList.map(_.trim),JSON.parse(prf).asInstanceOf[BasicDBObject],pswd)
                succ add obj(sn,"updated","OK")
              }
          })

          val res = new BasicDBObject
          if ( fail.isEmpty ) {
            res.put("updated",succ)
            OKResponse(res, user)
          }
          else if ( succ.isEmpty ) {
            res.put("failed",fail)
            FailResponse("Failed to update users",res, user)
          }
          else {
            val (so,sf) = (new BasicDBObject,new BasicDBObject)
            so.put("users",succ)
            sf.put("users",fail)
            PartialFailResponse("Some users could not be updated", so, sf,user)
          }
        }
      }
  }


  // ---------------------------------------------------------------------------

  get("""/services/publish/publish\.(json|xml|html)""".r, canonicalResponseType, tautologyGuard, public _) {
    requestFor {
      user =>
        if (!params.contains("uri"))
          FailResponse("Missing parammeter uri", new BasicDBObject, user)
        else {
          val uris = paramsMap("uri").toList
          val st = Store(cnf, user, false)
          val pubUris = st.publishURI(uris,request.isUserInRole("admin"))
          val succ = new BasicDBList
          pubUris.foreach(succ add _)
          val res = new BasicDBObject
          res.put("published",succ)
          OKResponse(res, user)
        }
    }
  }


  // ---------------------------------------------------------------------------

  get("""/services/publish/unpublish\.(json|xml|html)""".r, canonicalResponseType, tautologyGuard, public _) {
    requestFor {
      user =>
        if (!params.contains("uri"))
          FailResponse("Missing parammeter uri", new BasicDBObject, user)
        else {
          val uris = paramsMap("uri").toList
          val st = Store(cnf, user, false)
          val unpubUris = st.unpublishURI(uris,request.isUserInRole("admin"))
          val succ = new BasicDBList
          unpubUris.foreach(succ add _)
          val res = new BasicDBObject
          res.put("unpublished",succ)
          OKResponse(res, user)
        }
    }
  }


  // ---------------------------------------------------------------------------

  get("""/services/publish/list_published\.(json|xml|html)""".r, canonicalResponseType, tautologyGuard, public _) {
    requestFor {
      user =>
        val st = Store(cnf, user, false)
        val res = new BasicDBObject
        res.put("published",st.listPublished)
        OKResponse(res, user)
    }
  }


  // ---------------------------------------------------------------------------

  get("""/services/publish/publish_all\.(json|xml|html)""".r, canonicalResponseType, tautologyGuard, public _) {
    requestFor {
      user =>
        val st = Store(cnf, user, false)
        val uris = st.queryMetadata("{}","""{"%s":"%d"}""".format(K_PUB_DATE,-1),0,Integer.MAX_VALUE).map(_("uri").toString)
        val pubURIs = new BasicDBList
        st.publishURI(uris,request.isUserInRole("admin")).foreach(pubURIs add _)
        val res = new BasicDBObject
        res.put("published",pubURIs)
        OKResponse(res, user)
    }
  }


  // ---------------------------------------------------------------------------

  get("""/services/publish/unpublish_all\.(json|xml|html)""".r, canonicalResponseType, tautologyGuard, public _) {
    requestFor {
      user =>
        val st = Store(cnf, user, false)
        st.unpublishAllUserURIs
        val res = new BasicDBObject
        res.put("unpublished","all")
        OKResponse(res, user)
    }
  }


  // ---------------------------------------------------------------------------

  get("""/services/publish/force_unpublish_all\.(json|xml|html)""".r, canonicalResponseType, tautologyGuard, public _) {
    requestFor {
      user =>
        if ( !request.isUserInRole("admin")  ) {
          FailResponse("You need to belong to the admin role to force the unpublishing of the public repository", new BasicDBObject, user)
        }
        else {
          val st = Store(cnf, user, false)
          st.unpublishPublicRepository
          val res = new BasicDBObject
          res.put("unpublished","all")
          OKResponse(res, user)
        }
    }
  }


  // ---------------------------------------------------------------------------

  get("""/services/server/shutdown\.(json|xml|html)""".r, canonicalResponseType, tautologyGuard, public _) {
    requestFor {
      user =>
        if ( !request.isUserInRole("admin")  ) {
          FailResponse("You need to belong to the admin role to force the unpublishing of the public repository", new BasicDBObject, user)
        }
        else {
          val peers = paramsMap.contains("uuid") match {
            case false => List(snareMon.uuid.toString)
            case true  => paramsMap("uuid").toList
          }

          val resMsg = new BasicDBList
          peers.filter(_.trim!="").foreach(
            p => {
              val msg:BasicDBObject =
              """{
                    "msg" : "shutdown",
                    "type" :"request",
                    "source" : "%s",
                    "target" : "%s"
              }""" format (snareMon.uuid,p)
              snareMon.notifyPeer(p,msg)
              resMsg add msg
            }
          )
          val res = new BasicDBObject
          res.put("shutdown",resMsg)
          log.severe("Requesting sever immediate shutdown for %s".format(peers.foldLeft("")((a,b)=>a+b+" ")))
          OKResponse(res, user)
        }
    }
  }


  // ---------------------------------------------------------------------------

  get("""/services/server/shutdown_cluster\.(json|xml|html)""".r, canonicalResponseType, tautologyGuard, public _) {
    requestFor {
      user =>
        if ( !request.isUserInRole("admin")  ) {
          FailResponse("You need to belong to the admin role to force the unpublishing of the public repository", new BasicDBObject, user)
        }
        else {
          val msg:BasicDBObject =
            """{
                  "msg" : "shutdown",
                  "type" :"request",
                  "source" : "%s"
            }""" format (snareMon.uuid)
          snareMon.broadcast(msg)
          val res = new BasicDBObject
          res.put("shutdown cluster",msg)
          log.severe("Requesting cluster immediate shutdown")
          OKResponse(res, user)
        }
    }
  }

  // ---------------------------------------------------------------------------

  get("""/services/logs/global\.(json|xml|html)""".r, canonicalResponseType, tautologyGuard, public _) {
    requestFor {
      user =>
        if ( !request.isUserInRole("admin")  ) {
          FailResponse("You need to belong to the admin role to force the unpublishing of the public repository", new BasicDBObject, user)
        }
        else {
          val q = "{}"
          val skip = if (params.contains("offset")) safeParseInt(params("offset")) else 0
          val limit = if (params.contains("limit")) safeParseInt(params("limit"))  else 250
          val res = new BasicDBObject
          res.put("log",log.getLogEntries(q,skip,limit))
          OKResponse(res, user)
        }
    }
  }

  // ---------------------------------------------------------------------------

  get("""/services/logs/server\.(json|xml|html)""".r, canonicalResponseType, tautologyGuard, public _) {
    requestFor {
      user =>
        if ( !request.isUserInRole("admin")  ) {
          FailResponse("You need to belong to the admin role to force the unpublishing of the public repository", new BasicDBObject, user)
        }
        else {
          val skip = if (params.contains("offset")) safeParseInt(params("offset")) else 0
          val limit = if (params.contains("limit")) safeParseInt(params("limit"))  else 250
          val uuids = params.contains("uuid") match {
            case false =>
              "\""+snareMon.uuid.toString+"\""
            case true  =>
              val vals = paramsMap("uuid").toList.filter(_!="").foldLeft("")((a,b)=>a+"\""+b+"\",")
              vals.substring(0,vals.length-1)
          }

          val q = """{"%s":{"$in":[%s]}}""".format("uuid",uuids)
          val res = new BasicDBObject
          res.put("log",log.getLogEntries(q,skip,limit))
          OKResponse(res, user)
        }
    }
  }


  // ---------------------------------------------------------------------------

  get("""/services/jobs/submit\.(json|xml|html)""".r, canonicalResponseType, tautologyGuard, public _) {
    requestFor {
      user =>
        val st = Store(cnf, user, false)
        val res = new BasicDBObject
        val replicas = if (params.contains("replicas"))
                          try { Integer.parseInt(params("replicas"))} catch { case _ => 1 }
                       else 1
        val wrapper = if (params.contains("wrapper") &&
                          ExecutionWrapper.validExecutionWrapper.contains(params("wrapper")) )
                        params("wrapper")
                      else
                        ExecutionWrapper.defaultExecutionWrapper
        
        params.contains("uri") match {
          case false =>
            FailResponse("Missing parammeter uri", new BasicDBObject, user)
          case true =>
            st.descriptorFor(params("uri")) match {
              case None =>
                FailResponse(params("uri") + " not a known URI", new BasicDBObject, user)
              case Some(desc) =>
                desc match {
                  case cmp:ComponentDescriptor =>
                    FailResponse(params("uri") + " is a component. Only flows are executable", new BasicDBObject, user)
                  case flow:FlowDescriptor =>
                    val rdfs = flow.instances.map(
                         ci => (ci.componentUri,st.getRDFForURI(ci.componentUri,"nt"))
                      )
                    rdfs.filter(_._2.isEmpty) match {
                      case Nil =>
                        // All components were found
                        val baos = new ByteArrayOutputStream
                        rdfs.foreach( uba => { baos write uba._2.get ; baos write "\n".getBytes } )
                        // Submit a job
                        val queue = new JobQueue(cnf)
                        val jobs = (1 to replicas).toList.map( i => queue.push(
                              baos.toByteArray,
                              user,
                              snareMon.uuid.toString,
                              wrapper,
                              """{"uri":"%s"}""" format params("uri"),
                              cnf.MEANDRE_QUEUE_ATTEMPTS
                            ))
                        jobs.exists(_.isEmpty) match {
                          case true =>
                            jobs.filter(j => !(j.isEmpty) ) match {
                              case l if l.length==0 => FailResponse("Could not submit job for "+params("uri"), new BasicDBObject, user)
                              case l if l.length>0  =>
                                val listSucc = new BasicDBList
                                l.foreach(
                                  j => {
                                    val msg:BasicDBObject =
                                    """{
                                          "msg" : "job",
                                          "type" :"submitted",
                                          "jobID" : "%s",
                                          "source" : "%s"
                                    }""" format (j.get.getString("jobID"),snareMon.uuid)
                                    snareMon.broadcast(msg)
                                    listSucc add j.get
                                  }
                                )
                                val (so,sf) = (new BasicDBObject,new BasicDBObject)
                                so.put("submitted",listSucc)
                                PartialFailResponse("Could submit all the requested number of jobs", so, sf,user)
                              case _ => FailResponse("Unexpected condition while processing submissions for "+params("uri"), new BasicDBObject, user)
                            }
                          case false =>
                            val listSucc = new BasicDBList
                            jobs.foreach(
                              j => {
                                  val msg:BasicDBObject =
                                  """{
                                        "msg" : "job",
                                        "type" :"submitted",
                                        "jobID" : "%s",
                                        "source" : "%s"
                                  }""" format (j.get.getString("jobID"),snareMon.uuid)
                                  snareMon.broadcast(msg)
                                  listSucc add j.get
                              }
                            )
                            val res = new BasicDBObject
                            res.put("submitted",listSucc)
                            OKResponse(res, user)
                        }
                      case _ =>
                        // Missing components for the flows
                        val muris = new BasicDBList
                        rdfs.foreach( _ match {
                          case (uri,None) => muris add uri
                          case _ =>
                        })
                        val res = new BasicDBObject
                        res.put("missing",muris)
                        FailResponse(params("uri") + " flow is missing some required components", res, user)
                    }
              }
            }
        }
    }
  }


  // ---------------------------------------------------------------------------

  get("""/services/jobs/list\.(json|xml|html)""".r, canonicalResponseType, tautologyGuard, public _) {
    requestFor {
      user =>
        val queue = new JobQueue(cnf)
        var sortCnd = """{"ts":-1}"""
        val skip = if (params.contains("offset")) safeParseInt(params("offset")) else 0
        val limit = if (params.contains("limit")) safeParseInt(params("limit"))  else 250
        if (params.contains("order")) {
          sortCnd = params("order") match {
            case "ts" => """{"ts":-1}"""
            case _ => """{"ts":1}"""
          }
        }
        var cndURIs = if ( params.contains("jobID")) {
                        """ "_id":"%s" """ format params("jobID")
                      }
                      else ""
        var cndStatus = if (params.contains("status")) """ "status":"%s" """ format params("status").capitalize
                        else ""
        var cndAdmin =  if (!request.isUserInRole("admin") ) """ "owner":"%s" """ format user
                        else ""
        var cnd = "{ %s %s %s }" format (
                if (cndAdmin.trim.length>0) cndAdmin+"," else "",
                if (cndURIs.trim.length>0) cndURIs+"," else "",
                cndStatus )

        val jobs = queue.queryQueue(cnd,sortCnd,skip,limit)
        val list = new BasicDBList
        jobs foreach ( j => list add j )
        val labeled = new BasicDBObject
        labeled.put("jobs", list)
        OKResponse(labeled,user)
    }
  }

  // ---------------------------------------------------------------------------

  get("""/services/jobs/ids\.(json|xml|html)""".r, canonicalResponseType, tautologyGuard, public _) {
    requestFor {
      user =>
        val queue = new JobQueue(cnf)
        var sortCnd = "{}"
        val skip = if (params.contains("offset")) safeParseInt(params("offset")) else 0
        val limit = if (params.contains("limit")) safeParseInt(params("limit"))  else Math.MAX_INT
        if (params.contains("order")) {
          sortCnd = params("order") match {
            case "ts" => """{"ts":-1}"""
            case _ => "{}"
          }
        }
        var cndStatus = if (params.contains("status")) """ "status":"%s" """ format params("status").capitalize
                        else ""
        var cndAdmin =  if (!request.isUserInRole("admin") ) """ "owner":"%s" """ format user
                        else ""
        var cnd = "{ %s %s }" format (if (cndAdmin.trim.length>0) cndAdmin+"," else "", cndStatus )

        val jobs = queue.queryQueue(cnd,sortCnd,skip,limit)
        val list = new BasicDBList
        jobs foreach ( j => list add j.get("jobID") )
        val labeled = new BasicDBObject
        labeled.put("jobIDs", list)
        OKResponse(labeled,user)
    }
  }


  // ---------------------------------------------------------------------------

  get("""/services/jobs/console\.(txt)""".r, "text/plain", tautologyGuard, public _) {
    requestForOutput {
      user =>
        val queue = new JobQueue(cnf)
        val jobID = if (params.contains("jobID")) params("jobID") else ""
        val jobs = queue.queryQueue("""{"_id":"%s"}""".format(jobID),"{}",0,Integer.MAX_VALUE)
        jobs match {
          case job::Nil =>
            if (job.getString("owner")==user) {
              val baos = new ByteArrayOutputStream(10000)
              queue.dumpConsoleFor(jobID,baos)
              new String(baos.toByteArray)
            }
            else {
              val msg = "Unauthorized access to job console %s by user %s".format(jobID,user)
              log.warning(msg)
              "[WARNING] "+msg
            }

          case Nil =>
            val msg = "Unknown job %s".format(jobID)
            log.warning(msg)
            "[WARNING] "+msg

          case l =>
            val msg = "Inconsitent job console for %s. Mutiple consoles %d".format(jobID,l.length)
            log.warning(msg)
            "[WARNING] "+msg
        }
    }
  }

  // ---------------------------------------------------------------------------

  get("""/services/jobs/log\.(txt)""".r, "text/plain", tautologyGuard, public _) {
    requestForOutput {
      user =>
        val queue = new JobQueue(cnf)
        val jobID = if (params.contains("jobID")) params("jobID") else ""
        val jobs = queue.queryQueue("""{"_id":"%s"}""".format(jobID),"{}",0,Integer.MAX_VALUE)
        jobs match {
          case job::Nil =>
            if (job.getString("owner")==user) {
              val baos = new ByteArrayOutputStream(10000)
              queue.dumpLogFor(jobID,baos)
              new String(baos.toByteArray)
            }
            else {
              val msg = "Unauthorized access to job console %s by user %s".format(jobID,user)
              log.warning(msg)
              "[WARNING] "+msg
            }

          case Nil =>
            val msg = "Unknown job %s".format(jobID)
            log.warning(msg)
            "[WARNING] "+msg

          case l =>
            val msg = "Inconsitent job console for %s. Mutiple consoles %d".format(jobID,l.length)
            log.warning(msg)
            "[WARNING] "+msg
        }
    }
  }



  // ---------------------------------------------------------------------------

  get("""/services/jobs/clean\.(json|xml|html)""".r, canonicalResponseType, tautologyGuard, public _) {
    requestFor {
      user =>
        if ( !request.isUserInRole("admin")  ) {
          FailResponse("You need to belong to the admin role to force the unpublishing of the public repository", new BasicDBObject, user)
        }
        else {
          val msg:BasicDBObject = """{
            "msg" : "job",
            "type" :"clean",
            "source" : "%s",
            "target" : "%s"
          }""" format (snareMon.uuid,snareMon.uuid)
          snareMon.broadcast(msg)
          val resp = new BasicDBObject
          resp.put("notification",msg)
          OKResponse(resp,user)
        }
    }
  }

  // ---------------------------------------------------------------------------

  get("""/services/jobs/kill\.(json|xml|html)""".r, canonicalResponseType, tautologyGuard, public _) {
    requestFor {
      user =>
        if ( !params.contains("jobID")  ) {
          FailResponse("Job ID required on a kill request", new BasicDBObject, user)
        }
        else {
          val resMsg = new BasicDBList
          paramsMap("jobID").foreach(
            job => {
              val msg:BasicDBObject =
              """{
                    "msg" : "job",
                    "type" :"kill",
                    "source" : "%s",
                    "target" : "%s"
              }""" format (snareMon.uuid,job)
              snareMon.broadcast(msg)
              resMsg add msg
            }
          )
          val res = new BasicDBObject
          res.put("kill",resMsg)
          log.severe(
            "Requesting job kill termination for [%s] by %s".format(
              paramsMap("jobID").foldLeft(" ")((a,b)=>a+b+" "),
              snareMon.uuid
            )
          )
          OKResponse(res, user)
        }
    }
  }

  // ---------------------------------------------------------------------------


}