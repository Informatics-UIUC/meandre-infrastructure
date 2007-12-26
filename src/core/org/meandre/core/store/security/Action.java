package org.meandre.core.store.security;

import java.util.HashSet;
import java.util.Set;

import org.meandre.core.store.security.local.SecurityStoreImpl;

/** The basic meandre actions available.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class Action {

	/** The base URL for actions */
	public static final String BASE_ACTION_URL = SecurityStoreImpl.BASE_SECURITY_URL_PROPERTY+"action";

	/** The base web URL for actions */
	public static final String BASE_WEB_ACTION_URL = SecurityStoreImpl.BASE_SECURITY_URL_PROPERTY+"action/web";

	/** The array of basic actions available */
	public static final String [] ALL_BASIC_ACTION_URLS = {
		BASE_ACTION_URL+"/Home",
		BASE_ACTION_URL+"/Repository",
		BASE_ACTION_URL+"/Components",
		BASE_ACTION_URL+"/Flows",
		BASE_ACTION_URL+"/Workbench",
		BASE_ACTION_URL+"/Publish",
		BASE_ACTION_URL+"/Execution",
		BASE_ACTION_URL+"/Credits",
		BASE_ACTION_URL+"/Profile",
		BASE_ACTION_URL+"/Admin"
	};
	
	/** The array of basic actions names */
	public static final String [] ALL_BASIC_ACTION_NAMES = {
		"Home",
		"Repository",
		"Components",
		"Flows",
		"Workbench",
		"Publish",
		"Execution",
		"Credits",
		"Profile",
		"Site admin"
	};
	
	/** The set of available actions. Populated from ALL_BASIC_ACTION_URLS */
	public static final Set<String> setBasicActionURLs = new HashSet<String>();
	// Populate the set
	static {
		for ( String sURL:ALL_BASIC_ACTION_URLS )
			setBasicActionURLs.add(sURL);
	}
}
