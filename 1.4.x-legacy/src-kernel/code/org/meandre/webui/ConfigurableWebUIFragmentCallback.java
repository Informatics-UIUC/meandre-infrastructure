package org.meandre.webui;

/** This interface provides extended functionality for components looking to 
 *  implement WebUIs. If this interfaces is implemented changes the default behaviour
 *  in the following manner: (1) empty request will never be called, all requests will
 *  be diverted to handle method, (2) the component will not response to its URI, but
 *  to the context being set (unless not set which will default to the URI).
 *  
 * @author Xavier Llor&agrave;
 *
 */
public interface ConfigurableWebUIFragmentCallback extends
		WebUIFragmentCallback {

	
	/** Gets the context path that the component will reponse to.
	 * 
	 * @return The context path URL that the component will response to.
	 */
	public String getContextPath ();
	
}
