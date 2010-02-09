package meandre.kernel.specs

import data.TestRepositories
import org.specs.Specification
import meandre.kernel.Configuration
import meandre.kernel.rdf.DescriptorsFactory
import meandre.state.Repository

/**
 * The repository specs to validate
 *
 * @author Xavier Llora
 * @date Feb 9, 2010 at 8:37:14 PM
 *
 */

object RepositorySpecs extends Specification {
  val cnf = Configuration()
  cnf.MEANDRE_DB_NAME = "Meandre_Test"

  "A repository " should {
    val flows      = DescriptorsFactory.buildFlowDescriptors(TestRepositories.demoRepositoryModel)
    val components = DescriptorsFactory.buildComponentDescriptors(TestRepositories.demoRepositoryModel)
    val repository = DescriptorsFactory(TestRepositories.demoRepositoryModel)          

    "be able to store and retrieve flows" in {
      val repo  = Repository(Configuration(),"test_user")
      repo.removeAll
      repo.size must beEqualTo(0)
      repo addFlows flows
      repo.size must beEqualTo(1)

    }
  }
}