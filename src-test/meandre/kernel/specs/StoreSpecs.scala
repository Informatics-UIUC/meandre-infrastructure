package meandre.kernel.specs

import meandre.Implicits._
import data.SpecsRepositories
import org.specs.Specification
import meandre.kernel.Configuration
import meandre.kernel.rdf.DescriptorsFactory
import java.io.ByteArrayInputStream
import scala.Option
import meandre.kernel.state.{LocationElement, BundledElement, Store}

/**
 * This class implements the specifications for a given store
 *
 * @author Xavier Llora
 * @date Feb 17, 2010 at 8:17:53 AM
 * 
 */

object StoreSpecs extends Specification {

  val cnf = Configuration()
  cnf.MEANDRE_DB_NAME = "Meandre_Test"
  val flow      = DescriptorsFactory.buildFlowDescriptors(SpecsRepositories.testRepositoryModel).head
  val component = DescriptorsFactory.buildComponentDescriptors(SpecsRepositories.testRepositoryModel).head

  def checkAllDefined[A] ( xs:List[Either[Throwable,List[Option[A]]]]) =
    xs.forall( x => x.isRight && x.right.get.forall(_.isDefined))
  
  "A user store" should {

    "be able to store flows and components without overwriting" in {
      val resName = List("ctx1.txt","ctx2.txt","ctx3.txt")
      val mimeType = List("text/plain","text/plain","text/plain")
      val isCnt = List(new ByteArrayInputStream(SpecsRepositories.testRepositoryInTTL.getBytes),
                       new ByteArrayInputStream(SpecsRepositories.testRepositoryInTTL.getBytes),
                       new ByteArrayInputStream(SpecsRepositories.testRepositoryInTTL.getBytes))
      val store = Store(cnf,"test_user",false)
      store.removeAll
      val cnt = store.size
      val resFirstAdd = store.addElements(BundledElement(flow,Nil,Nil,Nil))
      resFirstAdd foreach ( r => checkAllDefined(r) must beTrue )
      store.exist(flow.uri) must beTrue
      store.size must beEqualTo(1)
      val resSecondAdd = store.addElements(BundledElement(flow,Nil,Nil,Nil))
      resSecondAdd foreach ( s => checkAllDefined(s) must beFalse )
      store.size must beEqualTo(1)
      store.addElements(BundledElement(component,resName,mimeType,isCnt))
      store.exist(component.uri) must beTrue
      store.removeAll
      store.size must beEqualTo(0)
    }


    "be able to store flows and components overwriting" in {
      val resName = List("ctx1.txt","ctx2.txt","ctx3.txt")
      val mimeType = List("text/plain","text/plain","text/plain")
      val isCnt = List(new ByteArrayInputStream(SpecsRepositories.testRepositoryInTTL.getBytes),
                       new ByteArrayInputStream(SpecsRepositories.testRepositoryInTTL.getBytes),
                       new ByteArrayInputStream(SpecsRepositories.testRepositoryInTTL.getBytes))
      val store = Store(cnf,"test_user",true)
      store.removeAll
      val cnt = store.size
      val resFirstAdd = store.addElements(BundledElement(flow,Nil,Nil,Nil))
      resFirstAdd foreach ( r => checkAllDefined(r) must beTrue )
      store.exist(flow.uri) must beTrue
      store.size must beEqualTo(1)
      val resSecondAdd = store.addElements(BundledElement(flow,Nil,Nil,Nil))
      resSecondAdd foreach ( s => checkAllDefined(s) must beTrue )
      store.size must beEqualTo(1)
      store.addElements(BundledElement(component,resName,mimeType,isCnt))
      store.exist(component.uri) must beTrue
      store.removeAll
      store.size must beEqualTo(0)
    }

    "be able to add locations" in {
      val store = Store(cnf,"test_user",true)
      store.removeAll
      store.isEmpty must beTrue
      val res = store.addElements(LocationElement(SpecsRepositories.testRemoteLocation))
      store.size must beEqualTo(14)
      store.removeAll
      store.isEmpty must beTrue
    }

  }

}