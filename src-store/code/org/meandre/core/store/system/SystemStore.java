package org.meandre.core.store.system;

import java.util.Hashtable;
import java.util.Set;

public interface SystemStore {

	/** The repository location property */
    public static final String REPOSITORY_LOCATION = "repository-location";

	/** Flushes the current system model to disk.
	 *
	 *
	 */
	public abstract void flush();

	/** Add a property to the system store.
	 *
	 * @param sKey The key
	 * @param sValue The value
	 * @param sDesc The description of the property
	 */
	public abstract void setProperty(String sKey, String sValue, String sDesc);

	/** Returns the values for the requested property.
	 *
	 * @param sKey The property name
	 */
	public abstract Set<Hashtable<String, String>> getProperty(String sKey);

	/** Returns the set of stored properties.
	 *
	 * @return The set of property keys
	 */
	public abstract Set<String> getPropertyKeys();

	/** Removes a property from the system model.
	 *
	 * @param sKey The key
	 * @param sValue The value to remove
	 */
	public abstract void removeProperty(String sKey, String sValue);

}