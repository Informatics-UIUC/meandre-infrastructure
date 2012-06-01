package org.meandre.core.repository;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;

/** The definition of a property description.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class PropertiesDescriptionDefinition extends PropertiesDescription {

	/** The property description. */
	private Hashtable<String,String> htDescriptions = null;
	
	/** Other properties that may been allocated to this property. */
	private Hashtable<String, Hashtable<String, String>> htOthers;
	
	/** Create a property description definition.
	 * 
	 * @param htValues The values
	 * @param htDescriptions The descriptions
	 */
	public PropertiesDescriptionDefinition(
			Hashtable<String,String> htValues, 
			Hashtable<String,String> htDescriptions) {
		super(htValues);
		this.htDescriptions = htDescriptions;
		this.htOthers = new Hashtable<String,Hashtable<String,String>>() ;
	}
	
	/** Create a property description definition.
	 * 
	 * @param htValues The values
	 * @param htDescriptions The descriptions
	 * @param htOthers The other properties attached to this property
	 */
	public PropertiesDescriptionDefinition(
			Hashtable<String,String> htValues, 
			Hashtable<String,String> htDescriptions, 
			Hashtable<String,Hashtable<String,String>> htOthers ) {
		super(htValues);
		this.htDescriptions = htDescriptions;
		this.htOthers = htOthers;
	}

	
	/** Returns the description of the property.
	 * 
	 * @return The descriptions of the stored properties
	 */
	public Collection<String> getDescriptions () {
		return htDescriptions.values();
	}
	
	/** Return the description for a given key.
	 * 
	 * @param sKey The key of the property to retrieve
	 * @return The description value
	 */
	public String getDescription ( String sKey ) {
		return htDescriptions.get(sKey);
	}
	
	/** Returns the description map
	 * 
	 * @return The description map
	 */
	public Map<String,String> getDescriptionMap () {
		return htDescriptions;
	}
	
	/** Returns the other properties that may have been attached to 
	 * a property description.
	 * 
	 * @param sKey The property key
	 * @return The map for the other properties
	 */
	public Map<String, String> getOtherProperties ( String sKey ) {
		Map<String,String> map = htOthers.get(sKey);
		if ( map==null ) map = new Hashtable<String,String>();
		return map;
	}
}
