package org.meandre.core.services.coordinator;

/** This interfaces is provided to be able to get a call back to ping the
 * arbitrated services.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public interface CoordinatorServiceCallBack {

	/** Returns the description of the service being coordinated.
	 * 
	 * @return The description
	 */
	public String getDescription ();
	
	/** Should be implemented to check if the monitored service is alive.
	 * 
	 * @param sIP The IP running the service
	 * @param iPort The port where the service is binded
	 * @return True is the service is up, false otherwise.
	 */
	public boolean ping(String sIP, int iPort);
	
}
