package org.meandre.engine.terracotta.datatypes;

import java.util.Hashtable;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.Resource;

/** The objects of this class describe firing events.
 * 
 * @author Xavier Llor&agrave
 */
public class FiringEvent {

	/** The newline separator */
	private static final String sNL = System.getProperty("line.separator");
	
	/** The firing event unique sequence id for the given flow instance ID */
	private static Map<Resource,Long> mapFireEvent = new Hashtable<Resource,Long>();
	
	/** The current executing flow instance ID */
	private Resource resFID;
	
	/** The target instance */
	protected Resource resECID;
	
	/** The map containing the ports to be populated by this event and
	 * the object values for them.
	 */
	protected Map<Resource,Object> mapPortValues;

	/** The firing event sequence ID */
	protected Long lID;

	
	/** Creates an empty firing event */
	public FiringEvent() {
		
	}
	
	/** Initialize the firing event for this flow instance ID.
	 * 
	 * @param resFID The resource for the generating flow instance
	 * @param resECID The targeted executable component instance of the firing event
	 * @throws InterruptedException The firing event class could not be properly initialized
	 */
	public void init ( Resource resFID, Resource resECID ) throws InterruptedException {
		synchronized ( mapFireEvent ) {
			lID = mapFireEvent.get(resFID);
			if ( lID!=null ) 
				mapFireEvent.put(resFID, mapFireEvent.get(resFID)+1);
			else {
				lID = 0L;
				mapFireEvent.put(resFID,1L);
			}
		}
		this.resFID = resFID;
		this.resECID = resECID;
		mapPortValues = new Hashtable<Resource,Object>();
	}
	
	/** Returns the firing sequence ID.
	 * 
	 * @return The unique firing sequence ID.
	 */
	public long getFiringSequenceID() {
		return lID;
	}
	
	/** Binds the port to given value for this firing event.
	 * 
	 * @param res The resource of the port to bind
	 * @param obj The object to bind
	 */
	public void bindPort ( Resource res, Object obj ) {
		mapPortValues.put(res, obj);
	}
	
	/** Returns the number of binded ports for this firing event.
	 * 
	 * @return The number of binded ports.
	 */
	public int getNumberOfBindedPorts () {
		return mapPortValues.size();
	}
	
	/** Converts the firing event into a stringfied version.
	 * 
	 * @return The string representing the firing event
	 */
	public String toString () {
		StringBuffer sb = new StringBuffer();
	
		sb.append("FlowID: "+resFID+sNL);
		sb.append("TrgtID: "+resECID+sNL);
		sb.append("FireID: "+lID+sNL);
		sb.append("FESize: "+mapPortValues.size()+sNL);
		for ( Resource res:mapPortValues.keySet() ) 
			sb.append("\t"+res+": "+mapPortValues.get(res)+sNL);
	
		return sb.toString();
	}
	
}
