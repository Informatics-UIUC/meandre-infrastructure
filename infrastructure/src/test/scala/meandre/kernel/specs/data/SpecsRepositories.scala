package meandre.kernel.specs.data

import com.hp.hpl.jena.rdf.model.ModelFactory
import java.io.StringReader

/**
 * This object contains test repositories used on the kernel specs
 *
 * @author Xavier Llora
 * @date Feb 9, 2010 at 4:55:48 PM
 * 
 */

object SpecsRepositories  {

  val testRepositoryInTTL = """@prefix xsd:     <http://www.w3.org/2001/XMLSchema#> .
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
      :data_connector <meandre://test.org/flow/test-hello-world-with-python-and-lisp/connector/2> , <meandre://test.org/flow/test-hello-world-with-python-and-lisp/connector/4> , <meandre://test.org/flow/test-hello-world-with-python-and-lisp/connector/1> , <meandre://test.org/flow/test-hello-world-with-python-and-lisp/connector/3> , <meandre://test.org/flow/test-hello-world-with-python-and-lisp/connector/0> .

<meandre://test.org/flow/test-hello-world-with-python-and-lisp/connector/1>
      rdf:type :data_connector_configuration ;
      :connector_instance_data_port_source
              <meandre://test.org/component/push-string/output/string> ;
      :connector_instance_data_port_target
              <meandre://test.org/component/concatenate-strings/input/string_two> ;
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
              <meandre://test.org/component/concatenate-strings/input/string_one> ;
      :connector_instance_source
              <meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/push-string/0> ;
      :connector_instance_target
              <meandre://test.org/flow/test-hello-world-with-python-and-lisp/instance/concatenate_string/2> .
  """

  val testRepositoryModel = ModelFactory.createDefaultModel
  testRepositoryModel.read(new StringReader(testRepositoryInTTL),null,"TTL")

  val testRemoteLocation = "http://repository.seasr.org/Meandre/Locations/1.4.8/Flows/clustering/repository.rdf"

}