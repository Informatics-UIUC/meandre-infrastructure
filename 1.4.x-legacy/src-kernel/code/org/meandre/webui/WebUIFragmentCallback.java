package org.meandre.webui;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** This interface defines the methods that need to be implemented by a 
 * component that wants to provid a webUI fragment.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public interface WebUIFragmentCallback {
	
	/** This method gets call when a request with no parameters is made to a 
	 * component webui fragment.
	 * 
	 * @param response The response object
	 * @throws WebUIException Some problem arised during execution and something went wrong
	 */
	public void emptyRequest ( HttpServletResponse response )
	throws WebUIException;
	
	/** This method gets called when a call with parameters is done to a given compoment
	 * webUI fragment
	 * 
	 * @param request The request object
	 * @param response The response object
	 * @throws WebUIException A problem arised during the call back
	 */
	public void handle(HttpServletRequest request, HttpServletResponse response )
	throws WebUIException;

}
