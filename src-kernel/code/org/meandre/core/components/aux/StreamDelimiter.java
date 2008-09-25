package org.meandre.core.components.aux;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/** Objects of this class just denotes a stream delimiter of 
 * a stream sequence.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public abstract class StreamDelimiter {
	
	/** Properties that can be attached to the stream delimiter */
	private Map<String,Object> mapValues= new HashMap<String,Object>();
	 
	/** Assigns a value to with the given key.
	 * 
	 * @param sKey The key
	 * @param oObject The value
	 */
	public void put ( String sKey, Object oObject ) {
		mapValues.put(sKey, oObject);
	}

	/** Gets the value for a given Key.
	 * 
	 * @param sKey The key
	 * @return The value
	 */
	public Object get ( String sKey ) {
		return mapValues.get(sKey);
	}
	
	/** Returns the set of available keys in this delimiter.
	 * 
	 * @return The key set
	 */
	public Set<String> keySet () {
		return mapValues.keySet();
	}
}
