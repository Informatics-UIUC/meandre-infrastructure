package org.meandre.core.security;

import java.util.Set;


/** This interface provides the basic functionality of a Security Store.
 * 
 * @author Xavier LLor&agrave;
 * @author Peter Groves
 *
 */
public interface SecurityManager {

    /** The base system url */
    public static final String BASE_SECURITY_URL =
            "http://www.meandre.org/accounting/";

    /** The base system url */
    public static final String BASE_SECURITY_URL_PROPERTY =
            "http://www.meandre.org/accounting/property/";


    /** Creates a new user. It adds the user to the acces control store and 
     * returns a user object.
     * 
     * @param sNickName The nickname for the new user
     * @param sName The user name
     * @param sPassword The password to set
     * @return The new User
     * @throws SecurityStoreException Something went wrong while creating the user.
     */
    public User createUser ( String sNickName, String sName, String sPassword ) 
    throws SecurityStoreException;
    
    /** Removes a user form the system.
     * 
     * @param usr The user to delete
     * @throws SecurityStoreException Admin user cannot be deleted from the system
     */
    public void removeUser ( User usr ) throws SecurityStoreException;
    
    /** Return all the user nick names.
     * 
     * @return The user nick names
     */
  //  public Set<String> getUsersNickNames ();
    
    /** Gets the current users in the systems.
     * 
     * @return The set of curren users
     * @throws SecurityStoreException Could not creat ethe new users
     */
    public Set<User> getUsers () throws SecurityStoreException;
    
    /** Returns the user associated with this nickname.
     * 
     * @param sNickName The nickname
     * @return The user
     * @throws SecurityStoreException A problem arose while retrieving the user
     */
    public User getUser (String sNickName) throws SecurityStoreException;
    

    /** Updates the information about a given user. Same as calling
     * removeUser then createUser with the same nickname, but also maintains
     * the existing roles of the user.
     * 
     * @param usr The new user information
     * @return the updated version of the user.
     * @throws SecurityStoreException The user could not be updated
     */
    /*public User updateUser ( String sNickName, String sName, String sPassword) 
    throws SecurityStoreException;*/

    /** Search for users that match the given query string.
     * 
     * @param sQuery The query
     * @return The set of users
     * @throws SecurityStoreException An exception ocurred during the execution
     */
    /*public Set<User> searchForUser ( String sQuery ) 
    throws SecurityStoreException;
    */


	/** Return the set of roles granted for the user.
	 * 
	 * @return The set of allowed roles
	 */
	public Set<Role> getRolesOfUser (User usr)
	throws SecurityStoreException;

	
	/** Grants a new action role to the user.
	 * 
     * @param usr The user to give a new role to
	 * @param role The role to grant
	 * @throws SecurityStoreException The action role is not a valid one
	 */
	public void grantRole (User usr, Role roleToGrant ) 
	throws SecurityStoreException;

	/** Grants a set of action roles to the user.
	 * 
     * @param usr The user to give  new roles to
	 * @param roles The set of roles to grant
	 * @throws SecurityStoreException The action role is not a valid one
	 */
	public void grantRoles (User usr, Set<Role> rolesToGrant ) 
	throws SecurityStoreException;


	/** Revokes the given role for this user (removes the privileges of the role).
	 * 
     * @param usr The user who is having the role taken away
	 * @param role The role to deny
	 * @throws SecurityStoreException The action role is not a valid one
	 */
	public void revokeRole (User usr,  Role roleToRevoke) 
	throws SecurityStoreException;


	/** Checks if the given action role has been granted for this user.
	 * 
     * @param usr The user to check for the role
     * @param role The role to check for
	 * @throws SecurityStoreException The role is not a valid one
	 * @return True if the role is granted to the User
	 */
	public boolean hasRoleGranted (User usr, Role role) 
	throws SecurityStoreException;

	/** Revokes the given set of roles for this user.
     *
     * @param usr The user who is having the role taken away
	 * @param setActionRole The set of roles to remove from the user
	 * @throws SecurityStoreException The action role is not a valid one
	 */
	public void revokeRoles (User usr,  Set<Role> rolesToRevoke ) 
	throws SecurityStoreException;

	/** Revokes all roles the user has been granted.
	 * 
	 * @throws SecurityStoreException The action role is not a valid one
	 */
	public void revokeAllRoles(User usr) 
	throws SecurityStoreException;

	/**
	 * returns the complete set of roles that this security manager
	 * understands.
	 *
	 * @return all permissable roles
	 * @throws SecurityStoreException
	 */
	public Set<Role> getValidRoles() throws SecurityStoreException;
}
