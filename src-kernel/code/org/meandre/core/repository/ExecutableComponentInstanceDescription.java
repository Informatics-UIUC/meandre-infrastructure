package org.meandre.core.repository;

import com.hp.hpl.jena.rdf.model.Resource;

/** This class wraps the description of an executable component instance
 *
 * @author Xavier Llor&agrave;
 *
 */
public class ExecutableComponentInstanceDescription {


	/** The resource for the executable component */
	private Resource resExecutableComponentInstance = null;

	/** The module pointing to the resource */
	private Resource resComponent = null;

	/** The name of the executable component */
	private String sName = null;

	/** The description of the executable component */
	private String sDescription = null;

	/** The instance properties */
	private PropertiesDescription pdProperties = null;

	/** Create an empty executable component instance description instance
	 *
	 *
	 */
	public ExecutableComponentInstanceDescription () {
		this.resExecutableComponentInstance = null;
		this.resComponent = null;
		this.sName = "";
		this.sDescription = "";
		this.pdProperties = null;
	}

	/** Create a executable component instance description instance
	 *
	 * @param resExecutableComponentInstance The resource identifying this instance
	 * @param resComponent The component this instance belongs to
	 * @param sName The name of the flow
	 * @param sDescription The description of the flow
	 * @param description The instance properties
	 */
	public  ExecutableComponentInstanceDescription (
				Resource resExecutableComponentInstance,
				Resource resComponent,
				String sName,
				String sDescription,
				PropertiesDescription pdProperties
			) {
		this.resExecutableComponentInstance = resExecutableComponentInstance;
		this.resComponent = resComponent;
		this.sName = sName;
		this.sDescription = sDescription;
		this.pdProperties = pdProperties;
	}


	/** Sets the instance resource.
	 *
	 * @param res The instance resources
	 */
	public void setExecutableComponentInstance ( Resource res ) {
		resExecutableComponentInstance = res;
	}

	/** Returns the instance resource.
	 *
	 * @return The instance resources
	 */
	public Resource getExecutableComponentInstance() {
		return resExecutableComponentInstance;
	}

	/** Set the executable component resource.
	 *
	 * @param res The resource
	 */
	public void  setExecutableComponent ( Resource res ) {
		resComponent = res;
	}

	/** Returns the executable component resource.
	 *
	 * @return The resource
	 */
	public Resource getExecutableComponent() {
		return resComponent;
	}

	/** Sets the components name.
	 *
	 * @param sName The name
	 */
	public void setName ( String sName ) {
		this.sName=sName;
	}

	/** Returns the components name.
	 *
	 * @return The name
	 */
	public String getName() {
		return sName;
	}

	/** Sets the executable component description.
	 *
	 * @param sDesc The description
	 */
	public void  setDescription ( String sDesc ) {
		this.sDescription=sDesc;
	}

	/** Returns the executable component description.
	 *
	 * @return The description
	 */
	public String getDescription () {
		return sDescription;
	}

	/** Sets the properties for the instance.
	 *
	 * @param props The property description
	 */
	public void setProperties ( PropertiesDescription props ) {
		pdProperties=props;
	}

	/** Return the properties for the instance.
	 *
	 * @return The property description
	 */
	public PropertiesDescription getProperties () {
		return pdProperties;
	}

}
