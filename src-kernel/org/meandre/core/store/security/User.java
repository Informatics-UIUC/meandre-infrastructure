package org.meandre.core.store.security;

import java.util.Set;

import com.hp.hpl.jena.rdf.model.Model;

/** Basic user manipulation functions.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public interface User {

	/** Returns the nickname of the user.
	 * 
	 * @return The nick name
	 */
	public String getNickName ();
	
	/** Returns the name of the user.
	 * 
	 * @return The name
	 */
	public String getName();
	
	/** Sets the name of the user.
	 * 
	 * @param sName The name
	 */
	public void setName( String sName );
	
	/** Returns the encrypted password of a user.
	 * 
	 * @return The encrypted password
	 */
	public String getPassword();
	
	/** Sets the encrypted password of a user.
	 * 
	 * @param sPassword The encrypted password
	 * @throws SecurityStoreException The password could not be encrypted
	 */
	public void setPassword(String sPassword) throws SecurityStoreException;
;
	
	/** Return the set of action roles granted for the user.
	 * 
	 * @return The set of allowed roles
	 */
	public Set<String> getGrantedActionRoles ();
	
	/** Grants a new action role to the user.
	 * 
	 * @param sActionRole The action role to grant
	 * @throws SecurityStoreException The action role is not a valid one
	 */
	public void grantActionRole ( String sActionRole ) 
	throws SecurityStoreException;

	/** Grants a set of action roles to the user.
	 * 
	 * @param setActionRole The action role set to grant
	 * @throws SecurityStoreException The action role is not a valid one
	 */
	public void grantActionRoles ( Set<String> setActionRole ) 
	throws SecurityStoreException;

	/** Dennys the given action role for this user.
	 * 
	 * @param sActionRole The action role to denny
	 * @throws SecurityStoreException The action role is not a valid one
	 */
	public void dennyActionRole ( String sActionRole ) 
	throws SecurityStoreException;


	/** Checks if the given action role has been granted for this user.
	 * 
	 * @param sActionRole The action role to denny
	 * @throws SecurityStoreException The action role is not a valid one
	 * @return True if the action role was granted
	 */
	public boolean hasActionRoleGranted ( String sActionRole ) 
	throws SecurityStoreException;

	/** Dennys the given set of action role for this user.
	 * 
	 * @param setActionRole The action role set to deny
	 * @throws SecurityStoreException The action role is not a valid one
	 */
	public void dennyActionRole ( Set<String> setActionRole ) 
	throws SecurityStoreException;

	/** Dennys all the action role for this user.
	 * 
	 * @throws SecurityStoreException The action role is not a valid one
	 */
	public void dennyAllActionRoles () 
	throws SecurityStoreException;

	/** Returns a model containing all the information about this user.
	 * 
	 * @return The model
	 */
	public Model getModel ();
	
}