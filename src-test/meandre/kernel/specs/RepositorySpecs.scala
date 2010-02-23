package meandre.kernel.specs

import data.TestRepositories
import org.specs.Specification
import meandre.kernel.Configuration
import meandre.state.Repository
import meandre.kernel.rdf.{FlowDescriptor, ComponentDescriptor, Descriptor, DescriptorsFactory}

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

  /**Initialize a repository with the give list of descriptors.
   *
   * @param repository The collection of descriptors to use
   * @return The initialized repository
   */
  def initRepo (repository:List[Descriptor]) = {
    val repo  = Repository(Configuration(),"test_user")
    repo.removeAll
    repo.size must beEqualTo(0)
    repo add repository
    repo.size must beEqualTo(7)
    repo
  }

  /**Given a repository it removes all the components and flows.
   *
   * @param repo The repository to clean
   */
  def cleanRepo ( repo:Repository ) = {
    repo.removeAll
    repo.size must beEqualTo(0)
  }

  "A repository " should {
    val flows      = DescriptorsFactory.buildFlowDescriptors(TestRepositories.testRepositoryModel)
    val components = DescriptorsFactory.buildComponentDescriptors(TestRepositories.testRepositoryModel)
    val repository = DescriptorsFactory(TestRepositories.testRepositoryModel)

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
      cleanRepo(repo)
    }

    "be able to store and retrieve components" in {
      val repo  = initRepo(repository)
      val cds = repo.components
      cds.size must beEqualTo(components.size)
      cleanRepo(repo)
    }

    "be able to store and retrieve flows" in {
      val repo  = initRepo(repository)
      val fds = repo.flows
      fds.size must beEqualTo(flows.size)
      cleanRepo(repo)
    }


    "be able to store and retrieve components metadata" in {
      val repo  = initRepo(repository)
      val cds = repo.componentsMedatada
      cds.size must beEqualTo(components.size)
      cleanRepo(repo)
    }

    "be able to store and retrieve flows metadata" in {
      val repo  = initRepo(repository)
      val fds = repo.flowsMetadata
      fds.size must beEqualTo(flows.size)
      cleanRepo(repo)
    }

    "be able to group tags and separate them by component and flows" in {
      val repo  = initRepo(repository)
      repo.tags.size must beEqualTo(10)
      repo.componentsTags.size must beEqualTo(10)
      repo.flowsTags.size must beEqualTo(2)
      cleanRepo(repo)
    }


    "be able to generate tag clouds for all the repository or by component and flows" in {
      val repo  = initRepo(repository)
      repo.tagCloud.size must beEqualTo(10)
      repo.componentsTagCloud.size must beEqualTo(10)
      repo.flowsTagCloud.size must beEqualTo(2)
      cleanRepo(repo)
    }

    "be able to query using URI metadata components and flows"in {
      val repo = initRepo(repository)
      val uris = repository.map(_.uri)
      uris.foreach( uri => repo.metadataFor(uri) match {
        case Some(m:Map[String,Any]) => uri must beEqualTo(m("uri"))
        case None => fail
      })
      cleanRepo(repo)
    }

    "be able to query using URI components and flows"in {
      val repo = initRepo(repository)
      val uris = repository.map(_.uri)
      uris.foreach( uri => repo.descriptorFor(uri) match {
        case Some(c:ComponentDescriptor) => c.uri must beEqualTo(uri)
        case Some(f:FlowDescriptor) => f.uri must beEqualTo(uri)
        case None => fail
      })
      cleanRepo(repo)
    }

    "be able to add and retreive metadata using URIs"in {
      val meta = "Metadata foo"
      val repo = initRepo(repository)
      val uris = repository.map(_.uri)
      uris.foreach( uri => {
        repo.updateMetadata(uri,meta)
        repo getMetadata uri match {
          case Some(m) => m must be equalTo(meta)
          case None    => fail
        }
      })
      cleanRepo(repo)
    }


  }
}