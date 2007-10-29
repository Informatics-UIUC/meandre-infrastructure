package org.meandre.core.store.repository;

import java.util.Set;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

/** This interface describes the basic methods to query a repository.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public interface QueryableRepository {

	/** Refreshes the cache of component descriptions available.
	 * 
	 */
	public void refreshCache () ;
	
	/** Refreshes the cache of component descriptions available in the provided model.
	 * 
	 * @param modNew The new model to use as reprository
	 */
	public void refreshCache ( Model modNew );
	
	/** Returns the current model used as a repository.
	 * 
	 * @return The model used as reprository
	 */
	public Model getModel ();
	
	/** Returns the set of available executable components stored
	 * int the repository.
	 * 
	 * @return The set of executable components
	 */
	public Set<Resource> getAvailableExecutableComponents ();
	
	/** Returns the set of available executable components stored
	 * in the repository that match the search criteria. The queries
	 * are based on Lucene syntax.
	 * 
	 * @param sQuery The query string
	 * @return The set of executable components
	 */
	public Set<Resource> getAvailableExecutableComponents ( String sQuery );
	
	/** Returns the executable component description for the given resource.
	 * 
	 * @param res The resource
	 * @return The description
	 * @throws CorruptedDescriptionException Corrupted description found
	 */
	public ExecutableComponentDescription getExecutableComponentDescription ( Resource res );
	
	/** Returns the set of availabble flows in the repository.
	 * 
	 * @return The set of resources describing the available flows
	 */
	public Set<Resource> getAvailableFlows();
	
	/** Returns the set of availabble flows in the repository that match the search criteria. The queries
	 * are based on Lucene syntax.
	 * 
	 * @param sQuery The query string
	 * @return The set of resources describing the available flows
	 */
	public Set<Resource> getAvailableFlows ( String sQuery );
	
	/** Returns a description of the given resource.
	 * 
	 * @param res The flow description to retrieve to retrieve
	 * @return The flow description
	 * @throws CorruptedDescriptionException The desctiption is corrupted
	 */
	public FlowDescription getFlowDescription(Resource res);
	
	/** Returns the list of available tags in the repository.
	 * 
	 * @return The set of available tags.
	 */
	public Set<String> getTags ();
	
	/** Returns the list of available component tags in the repository.
	 * 
	 * @return The set of available component tags.
	 */
	public Set<String> getComponentTags ();
	
	/** Returns the list of available flow tags in the repository.
	 * 
	 * @return The set of available flow tags.
	 */
	public Set<String> getFlowTags ();
	

	/** Returns the set of executable components associated with this tag.
	 * 
	 * @param sTag The tag
	 * @return The set of components
	 */
	public Set<ExecutableComponentDescription> getComponentsByTag ( String sTag );

	/** Returns the set of flows associated with this tag.
	 * 
	 * @param sTag The tag
	 * @return The set of flows
	 */
	public Set<FlowDescription> getFlowsByTag ( String sTag );


}
