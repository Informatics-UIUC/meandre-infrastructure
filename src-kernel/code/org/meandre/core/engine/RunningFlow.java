package org.meandre.core.engine;

/** Structure to identify a running flow by name of the flow and the 
 * url of it's webui.
 * 
 * @author Xavier Llor&agrave;
 * updates Peter Groves
 *
 */
public class RunningFlow {

	/** The flow ID */
	private String sFlowID;
	
	/** The webui link */
	private String sWebUIURL;
	
	/** Create a bean with the information about a running flow.
	 * 
	 * @param sID The flow ID
	 * @param sURL The webUI URL
	 */
	public RunningFlow ( String sID, String sURL ) {
		sFlowID = sID;
		sWebUIURL = sURL;
	}
	
	/** Gets the flow ID.
	 * 
	 * @return The flow ID
	 */
	public String getID () {
		return sFlowID;
	}
	
	/** Returns the web UI URL
	 * 
	 * @return The web UI URL
	 */
	public String getWebUIURL ()  {
		return sWebUIURL;
	}
}
