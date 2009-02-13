package org.meandre.webui;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** This class wraps the basic interaction required to properly called user 
 * defined fragment callbacks.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class WebUIFragment implements WebUIFragmentCallback {

	/** The fragment ID */
	private String eciID = null;
	
	/** The webui fragment callback */
	private WebUIFragmentCallback wuif = null;

	/** Should this behave as a configurable WebUI? */
	private boolean configurable;

	/** If it is a configurable WebUI this contains the path we should response to */
	private String path;

	/** Creates a webui fragment for the given component instance
	 * using the provided callback methods.
	 * 
	 * @param eciID The executable component ID
	 * @param wuif
	 */
	public WebUIFragment ( String eciID, WebUIFragmentCallback wuif ) {
		this.eciID = eciID;
		this.wuif = wuif;
		this.configurable = false;
		this.path = null;
	}
	
	/** Creates a webui fragment for the given component instance
	 * using the provided a configurable callback methods.
	 * 
	 * @param eciID The executable component ID
	 * @param wuif
	 */
	public WebUIFragment ( String eciID, ConfigurableWebUIFragmentCallback wuif ) {
		this.eciID = eciID;
		this.wuif = wuif;
		this.configurable = true;
		this.path = wuif.getContextPath();
	}
	
	/** Returns the fragment ID.
	 * 
	 * @return The fragment ID
	 */
	public String getFragmentID () {
		return eciID;
	}
	
	/** This method gets call when a request with no parameters is made to a 
	 * component webui fragment.
	 * 
	 * @param response The response object
	 * @throws WebUIException Some problem arised during execution and something went wrong
	 */
	public void emptyRequest ( HttpServletResponse response )
	throws WebUIException {
		wuif.emptyRequest(response);
	}
	
	/** This method gets called when a call with parameters is done to a given compoment
	 * webUI fragment
	 * 
	 * @param request The request object
	 * @param response The response object
	 * @throws WebUIException A problem arised during the call back
	 */
	public void handle(HttpServletRequest request, HttpServletResponse response )
	throws WebUIException {
		wuif.handle(request, response);
	}

	/** Based on the request URL path, this methods checks if this fragment should
	 *  response to this request.
	 *  
	 * @param sTarget The requested target
	 * @return True if this fragment should response to this fragment, false otherwise
	 */
	public boolean canHandleRequest ( String sTarget ) {
		boolean bRes = false;
		
		if ( this.configurable )
			bRes = sTarget.startsWith(this.path);
		else
			bRes = sTarget.startsWith("/"+this.eciID);
		
		return bRes;
	}
	
	/** True if this is a configurable webui fragment.
	 * 
	 * @return True if configurable, false otherwise 
	 */
	public boolean isConfigurable() {
		return this.configurable;
	}
}
