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

    "be able to store flows and components" in {
      val repo  = Repository(Configuration(),"test_user")
      repo.removeAll
      repo.size must beEqualTo(0)
      repo addFlows flows
      repo.size must beEqualTo(1)
      repo.removeAllFlows
      repo.size must beEqualTo(0)
      repo addComponents components
      repo.size must beEqualTo(6)
      repo.removeAllComponents
      repo.size must beEqualTo(0)
      repo add repository
      repo.size must beEqualTo(7)
      repo.removeAll
    }

    "be able to store and retrieve components" in {
      val repo  = Repository(Configuration(),"test_user")
      repo.removeAll
      repo.size must beEqualTo(0)
      repo add repository
      repo.size must beEqualTo(7)
      val cds = repo.components
      cds.size must beEqualTo(components.size)
      repo.removeAll
      repo.size must beEqualTo(0)
    }

    "be able to store and retrieve flows" in {
      val repo  = Repository(Configuration(),"test_user")
      repo.removeAll
      repo.size must beEqualTo(0)
      repo add repository
      repo.size must beEqualTo(7)
      val fds = repo.flows
      fds.size must beEqualTo(flows.size)
      repo.removeAll
      repo.size must beEqualTo(0)
    }


    "be able to store and retrieve components metadata" in {
      val repo  = Repository(Configuration(),"test_user")
      repo.removeAll
      repo.size must beEqualTo(0)
      repo add repository
      repo.size must beEqualTo(7)
      val cds = repo.componentsMedatada
      cds.size must beEqualTo(components.size)
      repo.removeAll
      repo.size must beEqualTo(0)
    }

    "be able to store and retrieve flows metadata" in {
      val repo  = Repository(Configuration(),"test_user")
      repo.removeAll
      repo.size must beEqualTo(0)
      repo add repository
      repo.size must beEqualTo(7)
      val fds = repo.flowsMetadata
      fds.size must beEqualTo(flows.size)
      repo.removeAll
      repo.size must beEqualTo(0)
    }
  }
}