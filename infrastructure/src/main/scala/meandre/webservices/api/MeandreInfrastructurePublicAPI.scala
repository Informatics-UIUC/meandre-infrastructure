package meandre.webservices.api

import meandre.kernel.Implicits._
import com.mongodb.BasicDBObject
import meandre.webservices.api.Templating._
import meandre.kernel.Configuration
import java.io.{StringReader, ByteArrayOutputStream}
import meandre.kernel.state.Store
import com.hp.hpl.jena.rdf.model.ModelFactory

/**
 * The Meandre infrastructure implementation of the services API
 *
 * @author Xavier Llora
 * @date Mar 1, 2010 at 9:26:33 AM
 *
 */

class MeandreInfrastructurePublicAPI(cnf: Configuration) extends MeandreInfrastructureAbstractAPI(cnf) {

  // ---------------------------------------------------------------------------

  //
  // The well known ping
  //
  get("""/public/services/ping\.(json|xml|html)""".r, canonicalResponseType, tautologyGuard, public _) {
    val res: BasicDBObject = """{
         "status":"OK",
         "success":{"message":"pong"}
    }"""
    res serializeTo elements(0)
  }

  get("""/public/services/version\.(json|xml|html)""".r, canonicalResponseType, tautologyGuard, public _) {
    val res: BasicDBObject = """{
         "status":"OK",
         "success":{"version":"%s"}
    }""" format Configuration.INFRASTRUCTURE_VERSION
    res serializeTo elements(0)
  }
  // ---------------------------------------------------------------------------

  get("""/public/services/repository\.(rdf|ttl|nt)""".r, canonicalResponseType, tautologyGuard, public _) {
    val rdfType = elements(0) match {
      case "ttl" => "TURTLE"
      case "nt"  => "N3"
      case _     => "RDF/XML-ABBREV"
    }
    val st = Store(cnf, "admin", false)  // Admin user is not used since once the public collection is accessed
    val res = new BasicDBObject
    val rdf = new StringBuffer(10000)
    st.getAllPublicRDF(None,"nt").foldLeft(rdf)((a,b)=>a.append(new String(b)+'\n'))
    val rdfOut = rdfType match {
      case "N3" => res.put(elements(0),uriRewrite(rdf.toString)) ; OKResponse(res,"anonymous")
      case _    => val model = ModelFactory.createDefaultModel
                   model.read(new StringReader(rdf.toString),null,"N-TRIPLE")
                   val baos = new ByteArrayOutputStream
                   model.write(baos,rdfType)
                   res.put(elements(0),uriRewrite(baos.toString)) ; OKResponse(res,"anonymous")
    }
    rdfOut serializeTo elements(0)
 }


 // ---------------------------------------------------------------------------

 get("""/public/services/demo_repository\.(rdf|ttl|nt)""".r, canonicalResponseType, tautologyGuard, public _) {
   val rdf = elements(0) match {
     case "ttl" => pubRepTTL
     case "nt"  => pubRepNT
     case _     => pubRepRDF
   }
   val res = new BasicDBObject
   res.put(elements(0),uriRewrite(rdf))
   OKResponse(res,"anonymous") serializeTo elements(0)
 }

 // ---------------------------------------------------------------------------

  val pubRepTTL = """@prefix xsd:     <http://www.w3.org/2001/XMLSchema#> .
@prefix dc:      <http://purl.org/dc/elements/1.1/> .
@prefix rdfs:    <http://www.w3.org/2000/01/rdf-schema#> .
@prefix rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
@prefix :        <http://www.meandre.org/ontology/> .

<meandre://test.org/flow/test-hello-world-with-python-and-lisp/connector/4>
      rdf:type :data_connector_configuration ;
      :connector_instance_data_port_source
              <meandre://test.org/component/pass-through/output/string> ;
      :connector_instance_data_port_target
              <meandre://test.org/component/print-object/input/object> ;
      :connector_instance_source
              <meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/pass-through/4> ;
      :connector_instance_target
              <meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/print-object/5> .

<meandre://test.org/component/pass-through/input/string>
      rdf:type :data_port ;
      dc:description "The string to convert"^^xsd:string ;
      dc:identifier "meandre://test.org/component/pass-through/input/string"^^xsd:string ;
      :name   "string"^^xsd:string .

<meandre://test.org/component/to-uppercase/output/string>
      rdf:type :data_port ;
      dc:description "The uppercased string"^^xsd:string ;
      dc:identifier "meandre://test.org/component/to-uppercase/output/string"^^xsd:string ;
      :name   "string"^^xsd:string .

<meandre://test.org/component/print-object/property/count>
      rdf:type :property ;
      dc:description "If set to true prints the count of printed objects"^^xsd:string ;
      :key    "count"^^xsd:string ;
      :value  "true"^^xsd:string .

<meandre://test.org/component/fork-2-by-reference>
      rdf:type :executable_component ;
      dc:creator "Xavier Llor&agrave;"^^xsd:string ;
      dc:date "2007-09-11T21:06:03"^^xsd:dateTime ;
      dc:description "Pushes the string stored into the properties to the output"^^xsd:string ;
      dc:format "java/class"^^xsd:string ;
      dc:rights "University of Illinois/NCSA open source license"^^xsd:string ;
      :execution_context <meandre://test.org/component/> ;
      :firing_policy "all"^^xsd:string ;
      :input_data_port <meandre://test.org/component/fork-2-by-reference/input/object> ;
      :mode   <http://www.meandre.org/ontology/component/type/compute> ;
      :name   "Fork 2 by reference"^^xsd:string ;
      :output_data_port <meandre://test.org/component/fork-2-by-reference/output/object-ref-one> , <meandre://test.org/component/fork-2-by-reference/output/object-ref-two> ;
      :resource_location <meandre://test.org/component/org.meandre.demo.components.ForkByReference> ;
      :runnable "java"^^xsd:string ;
      :tag    "reference"^^xsd:string , "demo"^^xsd:string , "fork"^^xsd:string .

<meandre://test.org/flow/test-hello-world-with-python-and-lisp/connector/set>
      rdf:type :connector_set ;
      :data_connector <meandre://test.org/flow/test-hello-world-with-python-and-lisp/connector/4> , <meandre://test.org/flow/test-hello-world-with-python-and-lisp/connector/2> , <meandre://test.org/flow/test-hello-world-with-python-and-lisp/connector/1> , <meandre://test.org/flow/test-hello-world-with-python-and-lisp/connector/3> , <meandre://test.org/flow/test-hello-world-with-python-and-lisp/connector/0> .

<meandre://test.org/flow/test-hello-world-with-python-and-lisp/connector/1>
      rdf:type :data_connector_configuration ;
      :connector_instance_data_port_source
              <meandre://test.org/component/push-string/output/string> ;
      :connector_instance_data_port_target
              <meandre://test.org/component/concatenate-strings/input/string_one> ;
      :connector_instance_source
              <meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/push-string/1> ;
      :connector_instance_target
              <meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/concatenate_string/2> .

<meandre://test.org/component/push-string>
      rdf:type :executable_component ;
      dc:creator "Xavier Llor&agrave;"^^xsd:string ;
      dc:date "2007-09-11T21:06:03"^^xsd:dateTime ;
      dc:description "Pushes the string stored into the properties to the output"^^xsd:string ;
      dc:format "java/class"^^xsd:string ;
      dc:rights "University of Illinois/NCSA open source license"^^xsd:string ;
      :execution_context <meandre://test.org/component/> ;
      :firing_policy "all"^^xsd:string ;
      :mode   <http://www.meandre.org/ontology/component/type/compute> ;
      :name   "Push String"^^xsd:string ;
      :output_data_port <meandre://test.org/component/push-string/output/string> ;
      :property_set <meandre://test.org/component/push-string/property/times> , <meandre://test.org/component/push-string/property/message> ;
      :resource_location <meandre://test.org/component/org.meandre.demo.components.PushStringComponent> ;
      :runnable "java"^^xsd:string ;
      :tag    "string"^^xsd:string , "hello_world"^^xsd:string , "demo"^^xsd:string .

<meandre://test.org/component/concatenate-strings/output/concatenated_string>
      rdf:type :data_port ;
      dc:description "The concatenated string"^^xsd:string ;
      dc:identifier "meandre://test.org/component/concatenate-strings/output/concatenated_string"^^xsd:string ;
      :name   "concatenated_string"^^xsd:string .

<meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/push-string/1>
      rdf:type :instance_configuration ;
      dc:description "Push hello world"^^xsd:string ;
      :instance_name "Push String 1"^^xsd:string ;
      :instance_resource <meandre://test.org/component/push-string> .

<meandre://test.org/component/push-string/property/message>
      rdf:type :property ;
      dc:description "The string message to be pushed trough the output port"^^xsd:string ;
      :key    "message"^^xsd:string ;
      :value  "Hello World!!! Happy Meandring!!!"^^xsd:string .

<meandre://test.org/component/concatenate-strings/input/string_two>
      rdf:type :data_port ;
      dc:description "The second string to concatenate"^^xsd:string ;
      dc:identifier "meandre://test.org/component/concatenate-strings/input/string_two"^^xsd:string ;
      :name   "string_two"^^xsd:string .

<meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/push-string/0>
      rdf:type :instance_configuration ;
      dc:description "Push hello world"^^xsd:string ;
      :instance_name "Push String 0"^^xsd:string ;
      :instance_resource <meandre://test.org/component/push-string> .

<meandre://test.org/component/print-object/input/object>
      rdf:type :data_port ;
      dc:description "The object to print"^^xsd:string ;
      dc:identifier "meandre://test.org/component/print-object/input/object"^^xsd:string ;
      :name   "object"^^xsd:string .

<meandre://test.org/flow/test-hello-world-with-python-and-lisp/connector/3>
      rdf:type :data_connector_configuration ;
      :connector_instance_data_port_source
              <meandre://test.org/component/to-uppercase/output/string> ;
      :connector_instance_data_port_target
              <meandre://test.org/component/pass-through/input/string> ;
      :connector_instance_source
              <meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/to-uppercase/3> ;
      :connector_instance_target
              <meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/pass-through/4> .

<meandre://test.org/component/fork-2-by-reference/input/object>
      rdf:type :data_port ;
      dc:description "The object to fork"^^xsd:string ;
      dc:identifier "meandre://test.org/component/fork-2-by-reference/input/object"^^xsd:string ;
      :name   "object"^^xsd:string .

<meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/to-uppercase/3>
      rdf:type :instance_configuration ;
      dc:description "Converts a strings to uppercase"^^xsd:string ;
      :instance_name "To Uppercasde 0"^^xsd:string ;
      :instance_resource <meandre://test.org/component/to-uppercase> .

<meandre://test.org/component/to-uppercase/input/string>
      rdf:type :data_port ;
      dc:description "The string to convert"^^xsd:string ;
      dc:identifier "meandre://test.org/component/to-uppercase/input/string"^^xsd:string ;
      :name   "string"^^xsd:string .

<meandre://test.org/component/push-string/property/times>
      rdf:type :property ;
      dc:description "Number of time to push the string"^^xsd:string ;
      :key    "times"^^xsd:string ;
      :value  "1"^^xsd:string .

<meandre://test.org/flow/test-hello-world-with-python-and-lisp/>
      rdf:type :flow_component ;
      dc:creator "Xavier Llor&agrave;"^^xsd:string ;
      dc:date "2007-09-11T21:06:03"^^xsd:dateTime ;
      dc:description "A simple hello world test"^^xsd:string ;
      dc:rights "University of Illinois/NCSA open source license"^^xsd:string ;
      :components_instances
              <meandre://test.org/flow/test-hello-world-with-python-and-lisp/components/set> ;
      :connectors <meandre://test.org/flow/test-hello-world-with-python-and-lisp/connector/set> ;
      :name   "Hello World With Java, Python, and Lisp Components!!!"^^xsd:string ;
      :tag    "hello_world"^^xsd:string , "demo"^^xsd:string .

<meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/concatenate_string/2>
      rdf:type :instance_configuration ;
      dc:description "Concatenates two strings"^^xsd:string ;
      :instance_name "Concatenate String 0"^^xsd:string ;
      :instance_resource <meandre://test.org/component/concatenate-strings> .

<meandre://test.org/component/fork-2-by-reference/output/object-ref-one>
      rdf:type :data_port ;
      dc:description "The first object"^^xsd:string ;
      dc:identifier "meandre://test.org/component/fork-2-by-reference/output/object-ref-one"^^xsd:string ;
      :name   "object_ref_one"^^xsd:string .

<meandre://test.org/component/concatenate-strings/input/string_one>
      rdf:type :data_port ;
      dc:description "The first string to concatenate"^^xsd:string ;
      dc:identifier "meandre://test.org/component/concatenate-strings/input/string_one"^^xsd:string ;
      :name   "string_one"^^xsd:string .

<meandre://test.org/component/fork-2-by-reference/output/object-ref-two>
      rdf:type :data_port ;
      dc:description "The first object"^^xsd:string ;
      dc:identifier "meandre://test.org/component/fork-2-by-reference/output/object-ref-two"^^xsd:string ;
      :name   "object_ref_two"^^xsd:string .

<meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/print-object/5>
      rdf:type :instance_configuration ;
      dc:description "Prints the concatenated object"^^xsd:string ;
      :instance_name "Print Object 0"^^xsd:string ;
      :instance_resource <meandre://test.org/component/print-object> .

<meandre://test.org/component/print-object>
      rdf:type :executable_component ;
      dc:creator "Xavier Llor&agrave;"^^xsd:string ;
      dc:date "2007-09-11T21:06:03"^^xsd:dateTime ;
      dc:description "Prints the object in the input to the standard output"^^xsd:string ;
      dc:format "java/class"^^xsd:string ;
      dc:rights "University of Illinois/NCSA open source license"^^xsd:string ;
      :execution_context <meandre://test.org/component/> ;
      :firing_policy "all"^^xsd:string ;
      :input_data_port <meandre://test.org/component/print-object/input/object> ;
      :mode   <http://www.meandre.org/ontology/component/type/compute> ;
      :name   "Print Object"^^xsd:string ;
      :property_set <meandre://test.org/component/print-object/property/count> ;
      :resource_location <meandre://test.org/component/org.meandre.demo.components.PrintObjectComponent> ;
      :runnable "java"^^xsd:string ;
      :tag    "print"^^xsd:string , "object"^^xsd:string , "demo"^^xsd:string .

<meandre://test.org/flow/test-hello-world-with-python-and-lisp/components/set>
      rdf:type :instance_set ;
      :executable_component_instance
              <meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/to-uppercase/3> , <meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/pass-through/4> , <meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/push-string/1> , <meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/concatenate_string/2> , <meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/push-string/0> , <meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/print-object/5> .

<meandre://test.org/component/push-string/output/string>
      rdf:type :data_port ;
      dc:description "The string being pushed"^^xsd:string ;
      dc:identifier "meandre://test.org/component/push-string/output/string"^^xsd:string ;
      :name   "string"^^xsd:string .

<meandre://test.org/flow/test-hello-world-with-python-and-lisp/connector/2>
      rdf:type :data_connector_configuration ;
      :connector_instance_data_port_source
              <meandre://test.org/component/concatenate-strings/output/concatenated_string> ;
      :connector_instance_data_port_target
              <meandre://test.org/component/to-uppercase/input/string> ;
      :connector_instance_source
              <meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/concatenate_string/2> ;
      :connector_instance_target
              <meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/to-uppercase/3> .

<meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/pass-through/4>
      rdf:type :instance_configuration ;
      dc:description "Passes the object through"^^xsd:string ;
      :instance_name "Pass Through 0"^^xsd:string ;
      :instance_resource <meandre://test.org/component/pass-through> .

<meandre://test.org/component/concatenate-strings>
      rdf:type :executable_component ;
      dc:creator "Xavier Llor&agrave;"^^xsd:string ;
      dc:date "2007-09-11T21:06:03"^^xsd:dateTime ;
      dc:description "Concatenates the to input string to the output"^^xsd:string ;
      dc:format "java/class"^^xsd:string ;
      dc:rights "University of Illinois/NCSA open source license"^^xsd:string ;
      :execution_context <meandre://test.org/component/> ;
      :firing_policy "all"^^xsd:string ;
      :input_data_port <meandre://test.org/component/concatenate-strings/input/string_two> , <meandre://test.org/component/concatenate-strings/input/string_one> ;
      :mode   <http://www.meandre.org/ontology/component/type/compute> ;
      :name   "Concatenate Strings"^^xsd:string ;
      :output_data_port <meandre://test.org/component/concatenate-strings/output/concatenated_string> ;
      :resource_location <meandre://test.org/component/org.meandre.demo.components.ConcatenateStringsComponent> ;
      :runnable "java"^^xsd:string ;
      :tag    "string"^^xsd:string , "concatenate"^^xsd:string , "demo"^^xsd:string .

<meandre://test.org/component/to-uppercase>
      rdf:type :executable_component ;
      dc:creator "Xavier Llor&agrave;"^^xsd:string ;
      dc:date "2007-09-11T21:06:03"^^xsd:dateTime ;
      dc:description "Turns the input string into an upper case and pushes it to the output"^^xsd:string ;
      dc:format "jython"^^xsd:string ;
      dc:rights "University of Illinois/NCSA open source license"^^xsd:string ;
      :execution_context <meandre://test.org/component/> , "def initialize(ccp):\n   pass\n\ndef execute(cc):\n   s = cc.getDataComponentFromInput(\"string\") \n   cc.pushDataComponentToOutput(\"string\",s.upper()) \n\ndef dispose(ccp):\n   pass\n\n"^^xsd:string ;
      :firing_policy "all"^^xsd:string ;
      :input_data_port <meandre://test.org/component/to-uppercase/input/string> ;
      :mode   <http://www.meandre.org/ontology/component/type/compute> ;
      :name   "To Uppercase"^^xsd:string ;
      :output_data_port <meandre://test.org/component/to-uppercase/output/string> ;
      :resource_location <meandre://test.org/component/> ;
      :runnable "python"^^xsd:string ;
      :tag    "string"^^xsd:string , "demo"^^xsd:string , "uppercase"^^xsd:string .

<meandre://test.org/component/pass-through>
      rdf:type :executable_component ;
      dc:creator "Xavier Llor&agrave;"^^xsd:string ;
      dc:date "2007-09-11T21:06:03"^^xsd:dateTime ;
      dc:description "Passes the input string to the output string with no modifications"^^xsd:string ;
      dc:format "clojure"^^xsd:string ;
      dc:rights "University of Illinois/NCSA open source license"^^xsd:string ;
      :execution_context <meandre://test.org/component/> , "(defn initialize [x] 1 )\n(defn execute [x] \n        (. x (pushDataComponentToOutput \n                 \"string\"\n                 (. x (getDataComponentFromInput \"string\")))) )\n(defn dispose [x] 1 )\n"^^xsd:string ;
      :firing_policy "all"^^xsd:string ;
      :input_data_port <meandre://test.org/component/pass-through/input/string> ;
      :mode   <http://www.meandre.org/ontology/component/type/compute> ;
      :name   "Pass Through"^^xsd:string ;
      :output_data_port <meandre://test.org/component/pass-through/output/string> ;
      :resource_location <meandre://test.org/component/> ;
      :runnable "lisp"^^xsd:string ;
      :tag    "string"^^xsd:string , "demo"^^xsd:string , "pass through"^^xsd:string .

<meandre://test.org/component/pass-through/output/string>
      rdf:type :data_port ;
      dc:description "The converted string"^^xsd:string ;
      dc:identifier "meandre://test.org/component/pass-through/output/string"^^xsd:string ;
      :name   "string"^^xsd:string .

<meandre://test.org/flow/test-hello-world-with-python-and-lisp/connector/0>
      rdf:type :data_connector_configuration ;
      :connector_instance_data_port_source
              <meandre://test.org/component/push-string/output/string> ;
      :connector_instance_data_port_target
              <meandre://test.org/component/concatenate-strings/input/string_two> ;
      :connector_instance_source
              <meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/push-string/0> ;
      :connector_instance_target
              <meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/concatenate_string/2> .
"""

  val pubRepRDF = """<rdf:RDF
    xmlns="http://www.meandre.org/ontology/"
    xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
    xmlns:xsd="http://www.w3.org/2001/XMLSchema#"
    xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
    xmlns:dc="http://purl.org/dc/elements/1.1/">
  <data_connector_configuration rdf:about="meandre://test.org/flow/test-hello-world-with-python-and-lisp/connector/4">
    <connector_instance_source>
      <instance_configuration rdf:about="meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/pass-through/4">
        <instance_resource>
          <executable_component rdf:about="meandre://test.org/component/pass-through">
            <dc:date rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime"
            >2007-09-11T21:06:03</dc:date>
            <name rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
            >Pass Through</name>
            <execution_context rdf:resource="meandre://test.org/component/"/>
            <mode rdf:resource="http://www.meandre.org/ontology/component/type/compute"/>
            <tag rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
            >pass through</tag>
            <output_data_port>
              <data_port rdf:about="meandre://test.org/component/pass-through/output/string">
                <dc:identifier rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
                >meandre://test.org/component/pass-through/output/string</dc:identifier>
                <name rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
                >string</name>
                <dc:description rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
                >The converted string</dc:description>
              </data_port>
            </output_data_port>
            <firing_policy rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
            >all</firing_policy>
            <resource_location rdf:resource="meandre://test.org/component/"/>
            <dc:format rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
            >clojure</dc:format>
            <tag rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
            >string</tag>
            <dc:description rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
            >Passes the input string to the output string with no modifications</dc:description>
            <runnable rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
            >lisp</runnable>
            <tag rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
            >demo</tag>
            <dc:rights rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
            >University of Illinois/NCSA open source license</dc:rights>
            <execution_context rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
            >(defn initialize [x] 1 )
(defn execute [x]
        (. x (pushDataComponentToOutput
                 "string"
                 (. x (getDataComponentFromInput "string")))) )
(defn dispose [x] 1 )
</execution_context>
            <dc:creator rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
            >Xavier Llor&amp;agrave;</dc:creator>
            <input_data_port>
              <data_port rdf:about="meandre://test.org/component/pass-through/input/string">
                <dc:identifier rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
                >meandre://test.org/component/pass-through/input/string</dc:identifier>
                <name rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
                >string</name>
                <dc:description rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
                >The string to convert</dc:description>
              </data_port>
            </input_data_port>
          </executable_component>
        </instance_resource>
        <instance_name rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
        >Pass Through 0</instance_name>
        <dc:description rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
        >Passes the object through</dc:description>
      </instance_configuration>
    </connector_instance_source>
    <connector_instance_data_port_source rdf:resource="meandre://test.org/component/pass-through/output/string"/>
    <connector_instance_target>
      <instance_configuration rdf:about="meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/print-object/5">
        <instance_resource>
          <executable_component rdf:about="meandre://test.org/component/print-object">
            <firing_policy rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
            >all</firing_policy>
            <execution_context rdf:resource="meandre://test.org/component/"/>
            <tag rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
            >demo</tag>
            <tag rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
            >object</tag>
            <runnable rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
            >java</runnable>
            <name rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
            >Print Object</name>
            <mode rdf:resource="http://www.meandre.org/ontology/component/type/compute"/>
            <dc:date rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime"
            >2007-09-11T21:06:03</dc:date>
            <input_data_port>
              <data_port rdf:about="meandre://test.org/component/print-object/input/object">
                <dc:identifier rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
                >meandre://test.org/component/print-object/input/object</dc:identifier>
                <name rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
                >object</name>
                <dc:description rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
                >The object to print</dc:description>
              </data_port>
            </input_data_port>
            <resource_location rdf:resource="meandre://test.org/component/org.meandre.demo.components.PrintObjectComponent"/>
            <dc:creator rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
            >Xavier Llor&amp;agrave;</dc:creator>
            <tag rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
            >print</tag>
            <dc:description rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
            >Prints the object in the input to the standard output</dc:description>
            <dc:format rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
            >java/class</dc:format>
            <property_set>
              <property rdf:about="meandre://test.org/component/print-object/property/count">
                <key rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
                >count</key>
                <value rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
                >true</value>
                <dc:description rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
                >If set to true prints the count of printed objects</dc:description>
              </property>
            </property_set>
            <dc:rights rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
            >University of Illinois/NCSA open source license</dc:rights>
          </executable_component>
        </instance_resource>
        <instance_name rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
        >Print Object 0</instance_name>
        <dc:description rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
        >Prints the concatenated object</dc:description>
      </instance_configuration>
    </connector_instance_target>
    <connector_instance_data_port_target rdf:resource="meandre://test.org/component/print-object/input/object"/>
  </data_connector_configuration>
  <data_port rdf:about="meandre://test.org/component/to-uppercase/output/string">
    <dc:identifier rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
    >meandre://test.org/component/to-uppercase/output/string</dc:identifier>
    <name rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
    >string</name>
    <dc:description rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
    >The uppercased string</dc:description>
  </data_port>
  <executable_component rdf:about="meandre://test.org/component/fork-2-by-reference">
    <tag rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
    >demo</tag>
    <resource_location rdf:resource="meandre://test.org/component/org.meandre.demo.components.ForkByReference"/>
    <runnable rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
    >java</runnable>
    <dc:rights rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
    >University of Illinois/NCSA open source license</dc:rights>
    <mode rdf:resource="http://www.meandre.org/ontology/component/type/compute"/>
    <name rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
    >Fork 2 by reference</name>
    <dc:format rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
    >java/class</dc:format>
    <execution_context rdf:resource="meandre://test.org/component/"/>
    <dc:description rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
    >Pushes the string stored into the properties to the output</dc:description>
    <tag rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
    >reference</tag>
    <dc:date rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime"
    >2007-09-11T21:06:03</dc:date>
    <input_data_port>
      <data_port rdf:about="meandre://test.org/component/fork-2-by-reference/input/object">
        <dc:identifier rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
        >meandre://test.org/component/fork-2-by-reference/input/object</dc:identifier>
        <name rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
        >object</name>
        <dc:description rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
        >The object to fork</dc:description>
      </data_port>
    </input_data_port>
    <output_data_port>
      <data_port rdf:about="meandre://test.org/component/fork-2-by-reference/output/object-ref-two">
        <dc:identifier rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
        >meandre://test.org/component/fork-2-by-reference/output/object-ref-two</dc:identifier>
        <name rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
        >object_ref_two</name>
        <dc:description rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
        >The first object</dc:description>
      </data_port>
    </output_data_port>
    <firing_policy rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
    >all</firing_policy>
    <tag rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
    >fork</tag>
    <output_data_port>
      <data_port rdf:about="meandre://test.org/component/fork-2-by-reference/output/object-ref-one">
        <dc:identifier rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
        >meandre://test.org/component/fork-2-by-reference/output/object-ref-one</dc:identifier>
        <name rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
        >object_ref_one</name>
        <dc:description rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
        >The first object</dc:description>
      </data_port>
    </output_data_port>
    <dc:creator rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
    >Xavier Llor&amp;agrave;</dc:creator>
  </executable_component>
  <connector_set rdf:about="meandre://test.org/flow/test-hello-world-with-python-and-lisp/connector/set">
    <data_connector>
      <data_connector_configuration rdf:about="meandre://test.org/flow/test-hello-world-with-python-and-lisp/connector/3">
        <connector_instance_source>
          <instance_configuration rdf:about="meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/to-uppercase/3">
            <instance_resource>
              <executable_component rdf:about="meandre://test.org/component/to-uppercase">
                <name rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
                >To Uppercase</name>
                <dc:date rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime"
                >2007-09-11T21:06:03</dc:date>
                <firing_policy rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
                >all</firing_policy>
                <execution_context rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
                >def initialize(ccp):
   pass

def execute(cc):
   s = cc.getDataComponentFromInput("string")
   cc.pushDataComponentToOutput("string",s.upper())

def dispose(ccp):
   pass

</execution_context>
                <tag rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
                >string</tag>
                <tag rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
                >uppercase</tag>
                <input_data_port>
                  <data_port rdf:about="meandre://test.org/component/to-uppercase/input/string">
                    <dc:identifier rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
                    >meandre://test.org/component/to-uppercase/input/string</dc:identifier>
                    <name rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
                    >string</name>
                    <dc:description rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
                    >The string to convert</dc:description>
                  </data_port>
                </input_data_port>
                <dc:rights rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
                >University of Illinois/NCSA open source license</dc:rights>
                <resource_location rdf:resource="meandre://test.org/component/"/>
                <runnable rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
                >python</runnable>
                <dc:format rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
                >jython</dc:format>
                <output_data_port rdf:resource="meandre://test.org/component/to-uppercase/output/string"/>
                <mode rdf:resource="http://www.meandre.org/ontology/component/type/compute"/>
                <dc:creator rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
                >Xavier Llor&amp;agrave;</dc:creator>
                <tag rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
                >demo</tag>
                <execution_context rdf:resource="meandre://test.org/component/"/>
                <dc:description rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
                >Turns the input string into an upper case and pushes it to the output</dc:description>
              </executable_component>
            </instance_resource>
            <instance_name rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
            >To Uppercasde 0</instance_name>
            <dc:description rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
            >Converts a strings to uppercase</dc:description>
          </instance_configuration>
        </connector_instance_source>
        <connector_instance_data_port_source rdf:resource="meandre://test.org/component/to-uppercase/output/string"/>
        <connector_instance_target rdf:resource="meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/pass-through/4"/>
        <connector_instance_data_port_target rdf:resource="meandre://test.org/component/pass-through/input/string"/>
      </data_connector_configuration>
    </data_connector>
    <data_connector rdf:resource="meandre://test.org/flow/test-hello-world-with-python-and-lisp/connector/4"/>
    <data_connector>
      <data_connector_configuration rdf:about="meandre://test.org/flow/test-hello-world-with-python-and-lisp/connector/1">
        <connector_instance_source>
          <instance_configuration rdf:about="meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/push-string/1">
            <instance_resource>
              <executable_component rdf:about="meandre://test.org/component/push-string">
                <dc:rights rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
                >University of Illinois/NCSA open source license</dc:rights>
                <dc:date rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime"
                >2007-09-11T21:06:03</dc:date>
                <name rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
                >Push String</name>
                <property_set>
                  <property rdf:about="meandre://test.org/component/push-string/property/message">
                    <dc:description rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
                    >The string message to be pushed trough the output port</dc:description>
                    <value rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
                    >Hello World!!! Happy Meandring!!!</value>
                    <key rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
                    >message</key>
                  </property>
                </property_set>
                <runnable rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
                >java</runnable>
                <output_data_port>
                  <data_port rdf:about="meandre://test.org/component/push-string/output/string">
                    <dc:description rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
                    >The string being pushed</dc:description>
                    <name rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
                    >string</name>
                    <dc:identifier rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
                    >meandre://test.org/component/push-string/output/string</dc:identifier>
                  </data_port>
                </output_data_port>
                <dc:description rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
                >Pushes the string stored into the properties to the output</dc:description>
                <execution_context rdf:resource="meandre://test.org/component/"/>
                <property_set>
                  <property rdf:about="meandre://test.org/component/push-string/property/times">
                    <dc:description rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
                    >Number of time to push the string</dc:description>
                    <value rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
                    >1</value>
                    <key rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
                    >times</key>
                  </property>
                </property_set>
                <dc:creator rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
                >Xavier Llor&amp;agrave;</dc:creator>
                <firing_policy rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
                >all</firing_policy>
                <tag rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
                >string</tag>
                <tag rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
                >demo</tag>
                <dc:format rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
                >java/class</dc:format>
                <tag rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
                >hello_world</tag>
                <mode rdf:resource="http://www.meandre.org/ontology/component/type/compute"/>
                <resource_location rdf:resource="meandre://test.org/component/org.meandre.demo.components.PushStringComponent"/>
              </executable_component>
            </instance_resource>
            <instance_name rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
            >Push String 1</instance_name>
            <dc:description rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
            >Push hello world</dc:description>
          </instance_configuration>
        </connector_instance_source>
        <connector_instance_data_port_source rdf:resource="meandre://test.org/component/push-string/output/string"/>
        <connector_instance_target>
          <instance_configuration rdf:about="meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/concatenate_string/2">
            <instance_resource>
              <executable_component rdf:about="meandre://test.org/component/concatenate-strings">
                <tag rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
                >string</tag>
                <resource_location rdf:resource="meandre://test.org/component/org.meandre.demo.components.ConcatenateStringsComponent"/>
                <dc:date rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime"
                >2007-09-11T21:06:03</dc:date>
                <mode rdf:resource="http://www.meandre.org/ontology/component/type/compute"/>
                <execution_context rdf:resource="meandre://test.org/component/"/>
                <dc:creator rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
                >Xavier Llor&amp;agrave;</dc:creator>
                <runnable rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
                >java</runnable>
                <dc:rights rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
                >University of Illinois/NCSA open source license</dc:rights>
                <tag rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
                >demo</tag>
                <input_data_port>
                  <data_port rdf:about="meandre://test.org/component/concatenate-strings/input/string_one">
                    <dc:identifier rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
                    >meandre://test.org/component/concatenate-strings/input/string_one</dc:identifier>
                    <name rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
                    >string_one</name>
                    <dc:description rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
                    >The first string to concatenate</dc:description>
                  </data_port>
                </input_data_port>
                <firing_policy rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
                >all</firing_policy>
                <output_data_port>
                  <data_port rdf:about="meandre://test.org/component/concatenate-strings/output/concatenated_string">
                    <dc:identifier rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
                    >meandre://test.org/component/concatenate-strings/output/concatenated_string</dc:identifier>
                    <name rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
                    >concatenated_string</name>
                    <dc:description rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
                    >The concatenated string</dc:description>
                  </data_port>
                </output_data_port>
                <tag rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
                >concatenate</tag>
                <name rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
                >Concatenate Strings</name>
                <dc:description rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
                >Concatenates the to input string to the output</dc:description>
                <input_data_port>
                  <data_port rdf:about="meandre://test.org/component/concatenate-strings/input/string_two">
                    <dc:identifier rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
                    >meandre://test.org/component/concatenate-strings/input/string_two</dc:identifier>
                    <name rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
                    >string_two</name>
                    <dc:description rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
                    >The second string to concatenate</dc:description>
                  </data_port>
                </input_data_port>
                <dc:format rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
                >java/class</dc:format>
              </executable_component>
            </instance_resource>
            <instance_name rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
            >Concatenate String 0</instance_name>
            <dc:description rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
            >Concatenates two strings</dc:description>
          </instance_configuration>
        </connector_instance_target>
        <connector_instance_data_port_target rdf:resource="meandre://test.org/component/concatenate-strings/input/string_two"/>
      </data_connector_configuration>
    </data_connector>
    <data_connector>
      <data_connector_configuration rdf:about="meandre://test.org/flow/test-hello-world-with-python-and-lisp/connector/0">
        <connector_instance_source>
          <instance_configuration rdf:about="meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/push-string/0">
            <instance_resource rdf:resource="meandre://test.org/component/push-string"/>
            <instance_name rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
            >Push String 0</instance_name>
            <dc:description rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
            >Push hello world</dc:description>
          </instance_configuration>
        </connector_instance_source>
        <connector_instance_data_port_source rdf:resource="meandre://test.org/component/push-string/output/string"/>
        <connector_instance_target rdf:resource="meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/concatenate_string/2"/>
        <connector_instance_data_port_target rdf:resource="meandre://test.org/component/concatenate-strings/input/string_one"/>
      </data_connector_configuration>
    </data_connector>
    <data_connector>
      <data_connector_configuration rdf:about="meandre://test.org/flow/test-hello-world-with-python-and-lisp/connector/2">
        <connector_instance_source rdf:resource="meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/concatenate_string/2"/>
        <connector_instance_data_port_source rdf:resource="meandre://test.org/component/concatenate-strings/output/concatenated_string"/>
        <connector_instance_target rdf:resource="meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/to-uppercase/3"/>
        <connector_instance_data_port_target rdf:resource="meandre://test.org/component/to-uppercase/input/string"/>
      </data_connector_configuration>
    </data_connector>
  </connector_set>
  <flow_component rdf:about="meandre://test.org/flow/test-hello-world-with-python-and-lisp/">
    <dc:description rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
    >A simple hello world test</dc:description>
    <tag rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
    >demo</tag>
    <connectors rdf:resource="meandre://test.org/flow/test-hello-world-with-python-and-lisp/connector/set"/>
    <dc:date rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime"
    >2007-09-11T21:06:03</dc:date>
    <dc:creator rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
    >Xavier Llor&amp;agrave;</dc:creator>
    <components_instances>
      <instance_set rdf:about="meandre://test.org/flow/test-hello-world-with-python-and-lisp/components/set">
        <executable_component_instance rdf:resource="meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/print-object/5"/>
        <executable_component_instance rdf:resource="meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/push-string/1"/>
        <executable_component_instance rdf:resource="meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/to-uppercase/3"/>
        <executable_component_instance rdf:resource="meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/pass-through/4"/>
        <executable_component_instance rdf:resource="meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/concatenate_string/2"/>
        <executable_component_instance rdf:resource="meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/push-string/0"/>
      </instance_set>
    </components_instances>
    <dc:rights rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
    >University of Illinois/NCSA open source license</dc:rights>
    <tag rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
    >hello_world</tag>
    <name rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
    >Hello World With Java, Python, and Lisp Components!!!</name>
  </flow_component>
</rdf:RDF>"""

  val pubRepNT = """<meandre://test.org/flow/test-hello-world-with-python-and-lisp/> <http://purl.org/dc/elements/1.1/description> "A simple hello world test"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/> <http://www.meandre.org/ontology/tag> "demo"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/> <http://www.meandre.org/ontology/connectors> <meandre://test.org/flow/test-hello-world-with-python-and-lisp/connector/set> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/> <http://purl.org/dc/elements/1.1/date> "2007-09-11T21:06:03"^^<http://www.w3.org/2001/XMLSchema#dateTime> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/> <http://purl.org/dc/elements/1.1/creator> "Xavier Llor&agrave;"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/> <http://www.meandre.org/ontology/components_instances> <meandre://test.org/flow/test-hello-world-with-python-and-lisp/components/set> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/> <http://purl.org/dc/elements/1.1/rights> "University of Illinois/NCSA open source license"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.meandre.org/ontology/flow_component> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/> <http://www.meandre.org/ontology/tag> "hello_world"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/> <http://www.meandre.org/ontology/name> "Hello World With Java, Python, and Lisp Components!!!"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/push-string/property/message> <http://purl.org/dc/elements/1.1/description> "The string message to be pushed trough the output port"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/push-string/property/message> <http://www.meandre.org/ontology/value> "Hello World!!! Happy Meandring!!!"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/push-string/property/message> <http://www.meandre.org/ontology/key> "message"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/push-string/property/message> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.meandre.org/ontology/property> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/push-string/1> <http://www.meandre.org/ontology/instance_resource> <meandre://test.org/component/push-string> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/push-string/1> <http://www.meandre.org/ontology/instance_name> "Push String 1"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/push-string/1> <http://purl.org/dc/elements/1.1/description> "Push hello world"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/push-string/1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.meandre.org/ontology/instance_configuration> .
<meandre://test.org/component/push-string/output/string> <http://purl.org/dc/elements/1.1/description> "The string being pushed"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/push-string/output/string> <http://www.meandre.org/ontology/name> "string"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/push-string/output/string> <http://purl.org/dc/elements/1.1/identifier> "meandre://test.org/component/push-string/output/string"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/push-string/output/string> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.meandre.org/ontology/data_port> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/connector/4> <http://www.meandre.org/ontology/connector_instance_source> <meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/pass-through/4> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/connector/4> <http://www.meandre.org/ontology/connector_instance_data_port_source> <meandre://test.org/component/pass-through/output/string> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/connector/4> <http://www.meandre.org/ontology/connector_instance_target> <meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/print-object/5> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/connector/4> <http://www.meandre.org/ontology/connector_instance_data_port_target> <meandre://test.org/component/print-object/input/object> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/connector/4> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.meandre.org/ontology/data_connector_configuration> .
<meandre://test.org/component/concatenate-strings/input/string_two> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.meandre.org/ontology/data_port> .
<meandre://test.org/component/concatenate-strings/input/string_two> <http://purl.org/dc/elements/1.1/identifier> "meandre://test.org/component/concatenate-strings/input/string_two"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/concatenate-strings/input/string_two> <http://www.meandre.org/ontology/name> "string_two"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/concatenate-strings/input/string_two> <http://purl.org/dc/elements/1.1/description> "The second string to concatenate"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/print-object/5> <http://www.meandre.org/ontology/instance_resource> <meandre://test.org/component/print-object> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/print-object/5> <http://www.meandre.org/ontology/instance_name> "Print Object 0"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/print-object/5> <http://purl.org/dc/elements/1.1/description> "Prints the concatenated object"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/print-object/5> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.meandre.org/ontology/instance_configuration> .
<meandre://test.org/component/concatenate-strings/input/string_one> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.meandre.org/ontology/data_port> .
<meandre://test.org/component/concatenate-strings/input/string_one> <http://purl.org/dc/elements/1.1/identifier> "meandre://test.org/component/concatenate-strings/input/string_one"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/concatenate-strings/input/string_one> <http://www.meandre.org/ontology/name> "string_one"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/concatenate-strings/input/string_one> <http://purl.org/dc/elements/1.1/description> "The first string to concatenate"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/pass-through/input/string> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.meandre.org/ontology/data_port> .
<meandre://test.org/component/pass-through/input/string> <http://purl.org/dc/elements/1.1/identifier> "meandre://test.org/component/pass-through/input/string"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/pass-through/input/string> <http://www.meandre.org/ontology/name> "string"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/pass-through/input/string> <http://purl.org/dc/elements/1.1/description> "The string to convert"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/push-string> <http://purl.org/dc/elements/1.1/rights> "University of Illinois/NCSA open source license"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/push-string> <http://purl.org/dc/elements/1.1/date> "2007-09-11T21:06:03"^^<http://www.w3.org/2001/XMLSchema#dateTime> .
<meandre://test.org/component/push-string> <http://www.meandre.org/ontology/name> "Push String"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/push-string> <http://www.meandre.org/ontology/property_set> <meandre://test.org/component/push-string/property/message> .
<meandre://test.org/component/push-string> <http://www.meandre.org/ontology/runnable> "java"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/push-string> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.meandre.org/ontology/executable_component> .
<meandre://test.org/component/push-string> <http://www.meandre.org/ontology/output_data_port> <meandre://test.org/component/push-string/output/string> .
<meandre://test.org/component/push-string> <http://purl.org/dc/elements/1.1/description> "Pushes the string stored into the properties to the output"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/push-string> <http://www.meandre.org/ontology/execution_context> <meandre://test.org/component/> .
<meandre://test.org/component/push-string> <http://www.meandre.org/ontology/property_set> <meandre://test.org/component/push-string/property/times> .
<meandre://test.org/component/push-string> <http://purl.org/dc/elements/1.1/creator> "Xavier Llor&agrave;"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/push-string> <http://www.meandre.org/ontology/firing_policy> "all"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/push-string> <http://www.meandre.org/ontology/tag> "string"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/push-string> <http://www.meandre.org/ontology/tag> "demo"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/push-string> <http://purl.org/dc/elements/1.1/format> "java/class"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/push-string> <http://www.meandre.org/ontology/tag> "hello_world"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/push-string> <http://www.meandre.org/ontology/mode> <http://www.meandre.org/ontology/component/type/compute> .
<meandre://test.org/component/push-string> <http://www.meandre.org/ontology/resource_location> <meandre://test.org/component/org.meandre.demo.components.PushStringComponent> .
<meandre://test.org/component/concatenate-strings/output/concatenated_string> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.meandre.org/ontology/data_port> .
<meandre://test.org/component/concatenate-strings/output/concatenated_string> <http://purl.org/dc/elements/1.1/identifier> "meandre://test.org/component/concatenate-strings/output/concatenated_string"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/concatenate-strings/output/concatenated_string> <http://www.meandre.org/ontology/name> "concatenated_string"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/concatenate-strings/output/concatenated_string> <http://purl.org/dc/elements/1.1/description> "The concatenated string"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/connector/2> <http://www.meandre.org/ontology/connector_instance_source> <meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/concatenate_string/2> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/connector/2> <http://www.meandre.org/ontology/connector_instance_data_port_source> <meandre://test.org/component/concatenate-strings/output/concatenated_string> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/connector/2> <http://www.meandre.org/ontology/connector_instance_target> <meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/to-uppercase/3> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/connector/2> <http://www.meandre.org/ontology/connector_instance_data_port_target> <meandre://test.org/component/to-uppercase/input/string> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/connector/2> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.meandre.org/ontology/data_connector_configuration> .
<meandre://test.org/component/pass-through/output/string> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.meandre.org/ontology/data_port> .
<meandre://test.org/component/pass-through/output/string> <http://purl.org/dc/elements/1.1/identifier> "meandre://test.org/component/pass-through/output/string"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/pass-through/output/string> <http://www.meandre.org/ontology/name> "string"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/pass-through/output/string> <http://purl.org/dc/elements/1.1/description> "The converted string"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/push-string/property/times> <http://purl.org/dc/elements/1.1/description> "Number of time to push the string"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/push-string/property/times> <http://www.meandre.org/ontology/value> "1"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/push-string/property/times> <http://www.meandre.org/ontology/key> "times"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/push-string/property/times> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.meandre.org/ontology/property> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/concatenate_string/2> <http://www.meandre.org/ontology/instance_resource> <meandre://test.org/component/concatenate-strings> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/concatenate_string/2> <http://www.meandre.org/ontology/instance_name> "Concatenate String 0"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/concatenate_string/2> <http://purl.org/dc/elements/1.1/description> "Concatenates two strings"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/concatenate_string/2> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.meandre.org/ontology/instance_configuration> .
<meandre://test.org/component/fork-2-by-reference/input/object> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.meandre.org/ontology/data_port> .
<meandre://test.org/component/fork-2-by-reference/input/object> <http://purl.org/dc/elements/1.1/identifier> "meandre://test.org/component/fork-2-by-reference/input/object"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/fork-2-by-reference/input/object> <http://www.meandre.org/ontology/name> "object"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/fork-2-by-reference/input/object> <http://purl.org/dc/elements/1.1/description> "The object to fork"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/pass-through> <http://purl.org/dc/elements/1.1/date> "2007-09-11T21:06:03"^^<http://www.w3.org/2001/XMLSchema#dateTime> .
<meandre://test.org/component/pass-through> <http://www.meandre.org/ontology/name> "Pass Through"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/pass-through> <http://www.meandre.org/ontology/execution_context> <meandre://test.org/component/> .
<meandre://test.org/component/pass-through> <http://www.meandre.org/ontology/mode> <http://www.meandre.org/ontology/component/type/compute> .
<meandre://test.org/component/pass-through> <http://www.meandre.org/ontology/tag> "pass through"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/pass-through> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.meandre.org/ontology/executable_component> .
<meandre://test.org/component/pass-through> <http://www.meandre.org/ontology/output_data_port> <meandre://test.org/component/pass-through/output/string> .
<meandre://test.org/component/pass-through> <http://www.meandre.org/ontology/firing_policy> "all"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/pass-through> <http://www.meandre.org/ontology/resource_location> <meandre://test.org/component/> .
<meandre://test.org/component/pass-through> <http://purl.org/dc/elements/1.1/format> "clojure"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/pass-through> <http://www.meandre.org/ontology/tag> "string"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/pass-through> <http://purl.org/dc/elements/1.1/description> "Passes the input string to the output string with no modifications"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/pass-through> <http://www.meandre.org/ontology/runnable> "lisp"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/pass-through> <http://www.meandre.org/ontology/tag> "demo"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/pass-through> <http://purl.org/dc/elements/1.1/rights> "University of Illinois/NCSA open source license"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/pass-through> <http://www.meandre.org/ontology/execution_context> "(defn initialize [x] 1 )\n(defn execute [x] \n        (. x (pushDataComponentToOutput \n                 \"string\"\n                 (. x (getDataComponentFromInput \"string\")))) )\n(defn dispose [x] 1 )\n"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/pass-through> <http://purl.org/dc/elements/1.1/creator> "Xavier Llor&agrave;"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/pass-through> <http://www.meandre.org/ontology/input_data_port> <meandre://test.org/component/pass-through/input/string> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/connector/0> <http://www.meandre.org/ontology/connector_instance_source> <meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/push-string/0> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/connector/0> <http://www.meandre.org/ontology/connector_instance_data_port_source> <meandre://test.org/component/push-string/output/string> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/connector/0> <http://www.meandre.org/ontology/connector_instance_target> <meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/concatenate_string/2> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/connector/0> <http://www.meandre.org/ontology/connector_instance_data_port_target> <meandre://test.org/component/concatenate-strings/input/string_one> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/connector/0> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.meandre.org/ontology/data_connector_configuration> .
<meandre://test.org/component/fork-2-by-reference> <http://www.meandre.org/ontology/tag> "demo"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/fork-2-by-reference> <http://www.meandre.org/ontology/resource_location> <meandre://test.org/component/org.meandre.demo.components.ForkByReference> .
<meandre://test.org/component/fork-2-by-reference> <http://www.meandre.org/ontology/runnable> "java"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/fork-2-by-reference> <http://purl.org/dc/elements/1.1/rights> "University of Illinois/NCSA open source license"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/fork-2-by-reference> <http://www.meandre.org/ontology/mode> <http://www.meandre.org/ontology/component/type/compute> .
<meandre://test.org/component/fork-2-by-reference> <http://www.meandre.org/ontology/name> "Fork 2 by reference"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/fork-2-by-reference> <http://purl.org/dc/elements/1.1/format> "java/class"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/fork-2-by-reference> <http://www.meandre.org/ontology/execution_context> <meandre://test.org/component/> .
<meandre://test.org/component/fork-2-by-reference> <http://purl.org/dc/elements/1.1/description> "Pushes the string stored into the properties to the output"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/fork-2-by-reference> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.meandre.org/ontology/executable_component> .
<meandre://test.org/component/fork-2-by-reference> <http://www.meandre.org/ontology/tag> "reference"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/fork-2-by-reference> <http://purl.org/dc/elements/1.1/date> "2007-09-11T21:06:03"^^<http://www.w3.org/2001/XMLSchema#dateTime> .
<meandre://test.org/component/fork-2-by-reference> <http://www.meandre.org/ontology/input_data_port> <meandre://test.org/component/fork-2-by-reference/input/object> .
<meandre://test.org/component/fork-2-by-reference> <http://www.meandre.org/ontology/output_data_port> <meandre://test.org/component/fork-2-by-reference/output/object-ref-two> .
<meandre://test.org/component/fork-2-by-reference> <http://www.meandre.org/ontology/firing_policy> "all"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/fork-2-by-reference> <http://www.meandre.org/ontology/tag> "fork"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/fork-2-by-reference> <http://www.meandre.org/ontology/output_data_port> <meandre://test.org/component/fork-2-by-reference/output/object-ref-one> .
<meandre://test.org/component/fork-2-by-reference> <http://purl.org/dc/elements/1.1/creator> "Xavier Llor&agrave;"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/concatenate-strings> <http://www.meandre.org/ontology/tag> "string"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/concatenate-strings> <http://www.meandre.org/ontology/resource_location> <meandre://test.org/component/org.meandre.demo.components.ConcatenateStringsComponent> .
<meandre://test.org/component/concatenate-strings> <http://purl.org/dc/elements/1.1/date> "2007-09-11T21:06:03"^^<http://www.w3.org/2001/XMLSchema#dateTime> .
<meandre://test.org/component/concatenate-strings> <http://www.meandre.org/ontology/mode> <http://www.meandre.org/ontology/component/type/compute> .
<meandre://test.org/component/concatenate-strings> <http://www.meandre.org/ontology/execution_context> <meandre://test.org/component/> .
<meandre://test.org/component/concatenate-strings> <http://purl.org/dc/elements/1.1/creator> "Xavier Llor&agrave;"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/concatenate-strings> <http://www.meandre.org/ontology/runnable> "java"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/concatenate-strings> <http://purl.org/dc/elements/1.1/rights> "University of Illinois/NCSA open source license"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/concatenate-strings> <http://www.meandre.org/ontology/tag> "demo"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/concatenate-strings> <http://www.meandre.org/ontology/input_data_port> <meandre://test.org/component/concatenate-strings/input/string_one> .
<meandre://test.org/component/concatenate-strings> <http://www.meandre.org/ontology/firing_policy> "all"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/concatenate-strings> <http://www.meandre.org/ontology/output_data_port> <meandre://test.org/component/concatenate-strings/output/concatenated_string> .
<meandre://test.org/component/concatenate-strings> <http://www.meandre.org/ontology/tag> "concatenate"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/concatenate-strings> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.meandre.org/ontology/executable_component> .
<meandre://test.org/component/concatenate-strings> <http://www.meandre.org/ontology/name> "Concatenate Strings"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/concatenate-strings> <http://purl.org/dc/elements/1.1/description> "Concatenates the to input string to the output"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/concatenate-strings> <http://www.meandre.org/ontology/input_data_port> <meandre://test.org/component/concatenate-strings/input/string_two> .
<meandre://test.org/component/concatenate-strings> <http://purl.org/dc/elements/1.1/format> "java/class"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/to-uppercase> <http://www.meandre.org/ontology/name> "To Uppercase"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/to-uppercase> <http://purl.org/dc/elements/1.1/date> "2007-09-11T21:06:03"^^<http://www.w3.org/2001/XMLSchema#dateTime> .
<meandre://test.org/component/to-uppercase> <http://www.meandre.org/ontology/firing_policy> "all"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/to-uppercase> <http://www.meandre.org/ontology/execution_context> "def initialize(ccp):\n   pass\n\ndef execute(cc):\n   s = cc.getDataComponentFromInput(\"string\") \n   cc.pushDataComponentToOutput(\"string\",s.upper()) \n\ndef dispose(ccp):\n   pass\n\n"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/to-uppercase> <http://www.meandre.org/ontology/tag> "string"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/to-uppercase> <http://www.meandre.org/ontology/tag> "uppercase"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/to-uppercase> <http://www.meandre.org/ontology/input_data_port> <meandre://test.org/component/to-uppercase/input/string> .
<meandre://test.org/component/to-uppercase> <http://purl.org/dc/elements/1.1/rights> "University of Illinois/NCSA open source license"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/to-uppercase> <http://www.meandre.org/ontology/resource_location> <meandre://test.org/component/> .
<meandre://test.org/component/to-uppercase> <http://www.meandre.org/ontology/runnable> "python"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/to-uppercase> <http://purl.org/dc/elements/1.1/format> "jython"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/to-uppercase> <http://www.meandre.org/ontology/output_data_port> <meandre://test.org/component/to-uppercase/output/string> .
<meandre://test.org/component/to-uppercase> <http://www.meandre.org/ontology/mode> <http://www.meandre.org/ontology/component/type/compute> .
<meandre://test.org/component/to-uppercase> <http://purl.org/dc/elements/1.1/creator> "Xavier Llor&agrave;"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/to-uppercase> <http://www.meandre.org/ontology/tag> "demo"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/to-uppercase> <http://www.meandre.org/ontology/execution_context> <meandre://test.org/component/> .
<meandre://test.org/component/to-uppercase> <http://purl.org/dc/elements/1.1/description> "Turns the input string into an upper case and pushes it to the output"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/to-uppercase> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.meandre.org/ontology/executable_component> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/components/set> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.meandre.org/ontology/instance_set> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/components/set> <http://www.meandre.org/ontology/executable_component_instance> <meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/push-string/1> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/components/set> <http://www.meandre.org/ontology/executable_component_instance> <meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/pass-through/4> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/components/set> <http://www.meandre.org/ontology/executable_component_instance> <meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/print-object/5> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/components/set> <http://www.meandre.org/ontology/executable_component_instance> <meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/push-string/0> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/components/set> <http://www.meandre.org/ontology/executable_component_instance> <meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/concatenate_string/2> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/components/set> <http://www.meandre.org/ontology/executable_component_instance> <meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/to-uppercase/3> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/pass-through/4> <http://www.meandre.org/ontology/instance_resource> <meandre://test.org/component/pass-through> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/pass-through/4> <http://www.meandre.org/ontology/instance_name> "Pass Through 0"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/pass-through/4> <http://purl.org/dc/elements/1.1/description> "Passes the object through"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/pass-through/4> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.meandre.org/ontology/instance_configuration> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/push-string/0> <http://www.meandre.org/ontology/instance_resource> <meandre://test.org/component/push-string> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/push-string/0> <http://www.meandre.org/ontology/instance_name> "Push String 0"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/push-string/0> <http://purl.org/dc/elements/1.1/description> "Push hello world"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/push-string/0> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.meandre.org/ontology/instance_configuration> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/to-uppercase/3> <http://www.meandre.org/ontology/instance_resource> <meandre://test.org/component/to-uppercase> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/to-uppercase/3> <http://www.meandre.org/ontology/instance_name> "To Uppercasde 0"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/to-uppercase/3> <http://purl.org/dc/elements/1.1/description> "Converts a strings to uppercase"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/to-uppercase/3> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.meandre.org/ontology/instance_configuration> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/connector/3> <http://www.meandre.org/ontology/connector_instance_source> <meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/to-uppercase/3> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/connector/3> <http://www.meandre.org/ontology/connector_instance_data_port_source> <meandre://test.org/component/to-uppercase/output/string> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/connector/3> <http://www.meandre.org/ontology/connector_instance_target> <meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/pass-through/4> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/connector/3> <http://www.meandre.org/ontology/connector_instance_data_port_target> <meandre://test.org/component/pass-through/input/string> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/connector/3> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.meandre.org/ontology/data_connector_configuration> .
<meandre://test.org/component/fork-2-by-reference/output/object-ref-two> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.meandre.org/ontology/data_port> .
<meandre://test.org/component/fork-2-by-reference/output/object-ref-two> <http://purl.org/dc/elements/1.1/identifier> "meandre://test.org/component/fork-2-by-reference/output/object-ref-two"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/fork-2-by-reference/output/object-ref-two> <http://www.meandre.org/ontology/name> "object_ref_two"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/fork-2-by-reference/output/object-ref-two> <http://purl.org/dc/elements/1.1/description> "The first object"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/print-object/property/count> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.meandre.org/ontology/property> .
<meandre://test.org/component/print-object/property/count> <http://www.meandre.org/ontology/key> "count"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/print-object/property/count> <http://www.meandre.org/ontology/value> "true"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/print-object/property/count> <http://purl.org/dc/elements/1.1/description> "If set to true prints the count of printed objects"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/fork-2-by-reference/output/object-ref-one> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.meandre.org/ontology/data_port> .
<meandre://test.org/component/fork-2-by-reference/output/object-ref-one> <http://purl.org/dc/elements/1.1/identifier> "meandre://test.org/component/fork-2-by-reference/output/object-ref-one"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/fork-2-by-reference/output/object-ref-one> <http://www.meandre.org/ontology/name> "object_ref_one"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/fork-2-by-reference/output/object-ref-one> <http://purl.org/dc/elements/1.1/description> "The first object"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/connector/set> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.meandre.org/ontology/connector_set> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/connector/set> <http://www.meandre.org/ontology/data_connector> <meandre://test.org/flow/test-hello-world-with-python-and-lisp/connector/2> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/connector/set> <http://www.meandre.org/ontology/data_connector> <meandre://test.org/flow/test-hello-world-with-python-and-lisp/connector/0> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/connector/set> <http://www.meandre.org/ontology/data_connector> <meandre://test.org/flow/test-hello-world-with-python-and-lisp/connector/3> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/connector/set> <http://www.meandre.org/ontology/data_connector> <meandre://test.org/flow/test-hello-world-with-python-and-lisp/connector/4> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/connector/set> <http://www.meandre.org/ontology/data_connector> <meandre://test.org/flow/test-hello-world-with-python-and-lisp/connector/1> .
<meandre://test.org/component/print-object> <http://www.meandre.org/ontology/firing_policy> "all"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/print-object> <http://www.meandre.org/ontology/execution_context> <meandre://test.org/component/> .
<meandre://test.org/component/print-object> <http://www.meandre.org/ontology/tag> "demo"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/print-object> <http://www.meandre.org/ontology/tag> "object"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/print-object> <http://www.meandre.org/ontology/runnable> "java"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/print-object> <http://www.meandre.org/ontology/name> "Print Object"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/print-object> <http://www.meandre.org/ontology/mode> <http://www.meandre.org/ontology/component/type/compute> .
<meandre://test.org/component/print-object> <http://purl.org/dc/elements/1.1/date> "2007-09-11T21:06:03"^^<http://www.w3.org/2001/XMLSchema#dateTime> .
<meandre://test.org/component/print-object> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.meandre.org/ontology/executable_component> .
<meandre://test.org/component/print-object> <http://www.meandre.org/ontology/input_data_port> <meandre://test.org/component/print-object/input/object> .
<meandre://test.org/component/print-object> <http://www.meandre.org/ontology/resource_location> <meandre://test.org/component/org.meandre.demo.components.PrintObjectComponent> .
<meandre://test.org/component/print-object> <http://purl.org/dc/elements/1.1/creator> "Xavier Llor&agrave;"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/print-object> <http://www.meandre.org/ontology/tag> "print"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/print-object> <http://purl.org/dc/elements/1.1/description> "Prints the object in the input to the standard output"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/print-object> <http://purl.org/dc/elements/1.1/format> "java/class"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/print-object> <http://www.meandre.org/ontology/property_set> <meandre://test.org/component/print-object/property/count> .
<meandre://test.org/component/print-object> <http://purl.org/dc/elements/1.1/rights> "University of Illinois/NCSA open source license"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/connector/1> <http://www.meandre.org/ontology/connector_instance_source> <meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/push-string/1> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/connector/1> <http://www.meandre.org/ontology/connector_instance_data_port_source> <meandre://test.org/component/push-string/output/string> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/connector/1> <http://www.meandre.org/ontology/connector_instance_target> <meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/concatenate_string/2> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/connector/1> <http://www.meandre.org/ontology/connector_instance_data_port_target> <meandre://test.org/component/concatenate-strings/input/string_two> .
<meandre://test.org/flow/test-hello-world-with-python-and-lisp/connector/1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.meandre.org/ontology/data_connector_configuration> .
<meandre://test.org/component/to-uppercase/input/string> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.meandre.org/ontology/data_port> .
<meandre://test.org/component/to-uppercase/input/string> <http://purl.org/dc/elements/1.1/identifier> "meandre://test.org/component/to-uppercase/input/string"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/to-uppercase/input/string> <http://www.meandre.org/ontology/name> "string"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/to-uppercase/input/string> <http://purl.org/dc/elements/1.1/description> "The string to convert"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/print-object/input/object> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.meandre.org/ontology/data_port> .
<meandre://test.org/component/print-object/input/object> <http://purl.org/dc/elements/1.1/identifier> "meandre://test.org/component/print-object/input/object"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/print-object/input/object> <http://www.meandre.org/ontology/name> "object"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/print-object/input/object> <http://purl.org/dc/elements/1.1/description> "The object to print"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/to-uppercase/output/string> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.meandre.org/ontology/data_port> .
<meandre://test.org/component/to-uppercase/output/string> <http://purl.org/dc/elements/1.1/identifier> "meandre://test.org/component/to-uppercase/output/string"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/to-uppercase/output/string> <http://www.meandre.org/ontology/name> "string"^^<http://www.w3.org/2001/XMLSchema#string> .
<meandre://test.org/component/to-uppercase/output/string> <http://purl.org/dc/elements/1.1/description> "The uppercased string"^^<http://www.w3.org/2001/XMLSchema#string> ."""
}
