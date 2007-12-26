package org.meandre.core.store.security;

import org.meandre.core.store.security.local.SecurityStoreImpl;


/** This class implements a Meandre role.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class Role {

	/** The base URL for roles */
	public static final String BASE_ROLE_URL = SecurityStoreImpl.BASE_SECURITY_URL_PROPERTY+"role/";
	
	/** The base URL for granting roles */
	public static final String ROLE_GRANT_PROPERTY_URL = BASE_ROLE_URL+"grant";
	
}
