package org.meandre.core.repository;

import com.hp.hpl.jena.rdf.model.Resource;

/** A description of a given data port
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class DataPortDescription {

	/** The resource ID of the data port */
	private Resource resDataPort = null;
	
	/** The relative identifier of the port */
	private String sIdentifier = null;
	
	/** The pretty name of the data port */
	private String sName = null;
	
	/** The description of the data port */
	private String sDescription = null;
	
	/** Creates a data port description based on the given information.
	 * 
	 * @param res The resource locator
	 * @param sIdent The relative port identifier
	 * @param sName The name of the port
	 * @param sDesc the description of the port
	 * @throws CorruptedDescriptionException The resource and identifier are different
	 */
	public DataPortDescription(Resource res, String sIdent, String sName,
			String sDesc) throws CorruptedDescriptionException {
		this.resDataPort  = res;
		this.sIdentifier  = sIdent;
		this.sName        = sName;
		this.sDescription = sDesc;
		
		if ( !resDataPort.toString().equals(sIdentifier) )
			throw new CorruptedDescriptionException("Data port description resource different from the identifier: "+res+"!+"+sIdent);
	}
	
	/** Returns the resource of this data port.
	 * 
	 * @return The resource
	 */
	public Resource getResource () {
		return resDataPort;
	}
	
	/** Returns the identifier of the data port
	 * 
	 * @return The identifier
	 */
	public String getIdentifier () {
		return sIdentifier;
	}
	
	/** Returns the name of the data port
	 * 
	 * @return The name
	 */
	public String getName() {
		return sName;
	}
	
	/** Returns the description of the data port
	 * 
	 * @return The description
	 */
	public String getDescription () {
		return sDescription;
	}

	
}
