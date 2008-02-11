package org.meandre.core;

import java.util.logging.Logger;

/** The component context contains methods to access the context environment
 * properties of a component.
 *
 * @author Xavier Llor&agrave;
 *
 */
public interface ComponentContextProperties {

	/** The name of the available inputs.
	 *
	 * @return The array containing the names
	 */
	public String [] getInputNames ();

	/** The name of the available outputs.
	 *
	 * @return The array containing the names
	 */
	public String [] getOutputNames ();

	/** Returns the list of property names.
	 *
	  * @return The array of property names
	 */
	public String[] getPropertyNames( );

	/** Check a given component property value. If the property does not exist
	 * the call returns null.
	 *
	 * @param sKey The property key
	 * @return The property value (null if property does not exist)
	 */
	public String getProperty ( String sKey );


	/** Returns the logging facility.
	 *
	 * @return The logger object
	 */
	public Logger getLogger();
}
