package org.meandre.core.repository;

import com.hp.hpl.jena.rdf.model.Resource;

public class ConnectorDescription {

	/** The connector ID  */
	private Resource resConnector = null;

	/** The resource ID of the source instance */
	private Resource resInstanceSource = null;

	/** The resource ID of the source instance data port */
	private Resource resInstanceDataPortSource = null;

	/** The resource ID of the target instance */
	private Resource resInstanceTarget = null;

	/** The resource ID of the target instance data port */
	private Resource resInstanceDataPortTarget = null;

	/** Creates an empty connector description.
	 *
	 */
	public ConnectorDescription () {
		this.resConnector = null;
		this.resInstanceSource = null;
		this.resInstanceDataPortSource = null;
		this.resInstanceTarget = null;
		this.resInstanceDataPortTarget= null;
	}

	/** Creates a connector description object with the given information.
	 * @param resConnector The resource describing the connector
	 * @param resInstanceSource The source instance
	 * @param resInstanceDataPortSource The source instance port
	 * @param resInstanceTarget The target instance
	 * @param resInstanceDataPortTarget The target instance port
	 */
	public ConnectorDescription (
			Resource resConnector ,
			Resource resInstanceSource,
			Resource resInstanceDataPortSource,
			Resource resInstanceTarget, Resource resInstanceDataPortTarget
		) {
		this.resConnector = resConnector;
		this.resInstanceSource = resInstanceSource;
		this.resInstanceDataPortSource = resInstanceDataPortSource;
		this.resInstanceTarget = resInstanceTarget;
		this.resInstanceDataPortTarget= resInstanceDataPortTarget;
	}

	/** Sets the resource connector.
	 *
	 * @param res The source instance
 	 */
	public void  setConnector ( Resource res ) {
		resConnector = res;
	}


	/** Returns the resource connector.
	 *
	 * @return The source instance
 	 */
	public Resource getConnector () {
		return resConnector;
	}

	/** Sets the source instance.
	 *
	 * @param res The source instance
 	 */
	public void setSourceInstance ( Resource res ) {
		resInstanceSource = res;
	}

	/** Returns the source instance.
	 *
	 * @return The source instance
 	 */
	public Resource getSourceInstance () {
		return resInstanceSource;
	}

	/** Sets the source instance port.
	 *
	 * @param res The source instance port
	 */
	public void setSourceIntaceDataPort ( Resource res ) {
		resInstanceDataPortSource = res;
	}

	/** Returns the source instance port.
	 *
	 * @return The source instance port
	 */
	public Resource getSourceIntaceDataPort () {
		return resInstanceDataPortSource;
	}

	/** Sets the target instance.
	 *
	 * @param res The target instance
	 */
	public void setTargetInstance ( Resource res ) {
		resInstanceTarget = res;
	}

	/** Returns the target instance.
	 *
	 * @return The target instance
	 */
	public Resource getTargetInstance () {
		return resInstanceTarget;
	}

	/** Sets the target instance port
	 *
	 * @param res The target instance port
	 */
	public void setTargetIntaceDataPort ( Resource res ) {
		resInstanceDataPortTarget = res;
	}

	/** Returns the target instance port
	 *
	 * @return The target instance port
	 */
	public Resource getTargetIntaceDataPort () {
		return resInstanceDataPortTarget;
	}
	
	/** Check if two connectors are equal.
	 * 
	 * @param o The other connector to check
	 */
	public boolean equals ( Object o ) {
		ConnectorDescription cdOther = (ConnectorDescription) o;
		boolean bRes = false;
		
		if ( resInstanceDataPortSource.equals(cdOther.resInstanceDataPortSource) && 
			 resInstanceDataPortTarget.equals(cdOther.resInstanceDataPortTarget) &&
			 resInstanceSource.equals(cdOther.resInstanceSource) &&
			 resInstanceTarget.equals(cdOther.resInstanceTarget)
		   )
			bRes = true;
		
		return bRes;
	}
}
