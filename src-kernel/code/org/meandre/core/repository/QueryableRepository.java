package org.meandre.core.repository;

import java.util.Collection;
import java.util.Map;
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
	 * in the repository.
	 * 
	 * @return The set of executable component descriptions
	 */
	public Set<Resource> getAvailableExecutableComponents ();
	

    /**
     * Creates a list of components sorted by either date or name and returns the first N (='limit') values
     * @param model  The model containing the components
     * @param sOrder The sorting order ("date" or "name" for now) - or null if no sorting required
     * @param limit  The maximum number of values to be returned (or -1 if no limit)
     * @return The list of components
     */
    public Collection<Resource> getAvailableExecutableComponentsOrderedBy(String sOrder, int limit) ;


	/** Returns the set of available executable components descriptions stored
	 * in the repository.
	 * 
	 * @return The set of executable component descriptions
	 */
	public Set<ExecutableComponentDescription> getAvailableExecutableComponentDescriptions ();

	/** Returns the map of available executable components descriptions stored
	 * in the repository.
	 * 
	 * @return The set of executable component descriptions
	 */
	public Map<String,ExecutableComponentDescription> getAvailableExecutableComponentDescriptionsMap ();

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

	/** Returns the set of available flows in the repository.
	 * 
	 * @return The set of resources describing the available flows
	 */
	public Set<Resource> getAvailableFlows();

   /**
     * Creates a list of flows sorted by either date or name and returns the first N (='limit') values
     * @param model  The model containing the flows
     * @param sOrder The sorting order ("date" or "name" for now) - or null if no sorting required
     * @param limit  The maximum number of values to be returned (or -1 if no limit)
     * @return The list of flows
     * @author Boris Capitanu
     */
    public Collection<Resource> getAvailableFlowsOrderedBy(String sOrder, int limit);
	
	/** Returns the set of available flows descriptions in the repository.
	 * 
	 * @return The set of resources describing the available flows
	 */
	public Set<FlowDescription> getAvailableFlowDescriptions();
	

	/** Returns the map of available flows descriptions in the repository.
	 * 
	 * @return The set of resources describing the available flows
	 */
	public Map<String,FlowDescription> getAvailableFlowDescriptionsMap();
	
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

	/** Returns the map of available component tags in the repository.
	 * 
	 * @return The map of available component tags.
	 */
	public Map<String,Set<ExecutableComponentDescription>> getComponentTagsMap ();

	/** Returns the list of available flow tags in the repository.
	 * 
	 * @return The set of available flow tags.
	 */
	public Set<String> getFlowTags ();
	
	/** Returns the map of available flow tags in the repository.
	 * 
	 * @return The map of available flow tags.
	 */
	public Map<String,Set<FlowDescription>> getFlowTagsMap ();

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
