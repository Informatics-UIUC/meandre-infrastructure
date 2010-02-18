package meandre.kernel.specs

import meandre.Implicits._
import data.TestRepositories
import org.specs.Specification
import meandre.kernel.Configuration
import meandre.kernel.rdf.DescriptorsFactory
import meandre.kernel.state.{BundledElement, Store}
import java.io.ByteArrayInputStream

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
  val flow      = DescriptorsFactory.buildFlowDescriptors(TestRepositories.demoRepositoryModel).head
  val component = DescriptorsFactory.buildComponentDescriptors(TestRepositories.demoRepositoryModel).head

  "A user store" should {

    "be able to store flows without overwrite and embed" in {
      val resName = List("ctx1.txt","ctx2.txt","ctx3.txt")
      val mimeType = List("text/plain","text/plain","text/plain")
      val isCnt = List(new ByteArrayInputStream(TestRepositories.demoRepositoryInTTL.getBytes),
                       new ByteArrayInputStream(TestRepositories.demoRepositoryInTTL.getBytes),
                       new ByteArrayInputStream(TestRepositories.demoRepositoryInTTL.getBytes))
      val store = Store(cnf,"test_user",false,false)
      store.removeAll
      val cnt = store.size
      store.remove(flow.uri)
      store.addElements(List(BundledElement(flow,Nil,Nil,Nil)))
      store.exist(flow.uri) must beTrue
      store.addElements(BundledElement(component,resName,mimeType,isCnt))
      store.exist(component.uri) must beTrue
      store.removeAll
      store.size must beEqualTo(0)

    }

  }

}