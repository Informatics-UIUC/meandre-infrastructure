package meandre.kernel.specs

import data.TestRepositories
import java.io.File

import org.specs._
import meandre.kernel.rdf.{FlowDescriptor, ComponentDescriptor, Descriptor, DescriptorsFactory}

object RDFSpecs extends Specification {

  val model = TestRepositories.demoRepositoryModel

  "A description factory" should {
	  "must extract components and flows in demo repository" in {
	    DescriptorsFactory.buildComponentDescriptors(model).size must be equalTo(6)
	    DescriptorsFactory.buildFlowDescriptors(model).size must be equalTo(1)
	    DescriptorsFactory(model).size must be equalTo(7)
	  }
  }
  
  "The descriptors in demo repository" should {
    
	   val descriptors  = DescriptorsFactory(model)
	
	  "have a common description and name" in {
	    val names = descriptors.foldLeft(Set[String]())((a:Set[String],b:Descriptor)=>a+b.description.name)
	    names.size must be equalTo(7)
	    names.contains("Push String") must beTrue
	    names.contains("Concatenate Strings") must beTrue
	    names.contains("Print Object") must beTrue
	    names.contains("Fork 2 by reference") must beTrue
	    names.contains("To Uppercase") must beTrue
	    names.contains("Pass Through") must beTrue
	    names.contains("Hello World With Java, Python, and Lisp Components!!!") must beTrue
	  }
   
	  "have 0 or more properties " in {
	    for ( component <- descriptors ) component match {
	      case c:ComponentDescriptor => c.properties.size must beGreaterThanOrEqualTo(0)
	      case _ => 
	    }
	  }
   
	  "have 0 or more ports if they represent a component " in {
	    for ( component <- descriptors ) component match {
	      case c:ComponentDescriptor => c.inputs.size must beGreaterThanOrEqualTo(0)
	      case _ => 
	    }
	  }
   
	  "contain a flow that should have 6 instances and  5 connectors" in {
	    for ( component <- descriptors ) component match {
	      case c:FlowDescriptor if c.instances.size==6 =>  {
	    	  c.instances.size must beEqual(6)
	    	  c.connectors.size must beEqual(5)
	    	 } 
	      case c:FlowDescriptor if c.instances.size==2 =>  {
	    	  c.instances.size must beEqual(2)
	    	  c.connectors.size must beEqual(1)
	    	 }
	      case _ => 
	    }
	  }
   
	  "contain a total of 3 defined properties" in {
	    descriptors.foldLeft(0)(_+_.properties.size) must beEqual(3)
	  }
  }
  

}
