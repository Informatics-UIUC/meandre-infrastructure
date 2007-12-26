package org.meandre.core.store.security;

import java.util.Set;

/** This interface provides the basic functionality of a Security Store.
 * 
 * @author Xavier LLor&agrave;
 *
 */
public interface SecurityStore {

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
    public Set<String> getUsersNickNames ();
    
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
    public User getUser ( String sNickName ) throws SecurityStoreException;
    

    /** Updates the information about a given user.
     * 
     * @param usr The new user information
     * @throws SecurityStoreException The user could not be updated
     */
    public void updateUser ( User usr ) throws SecurityStoreException;


    /** Check if the given role has been granted to the given user.
     * 
     * @param sRole The role
     * @param sNickName The user nickname
     * @return True if role granted, false otherwise
     */
    public boolean hasGrantedRoleToUser ( String sRole, String sNickName );

    /** Search for users that match the given query string.
     * 
     * @param sQuery The query
     * @return The set of users
     * @throws SecurityStoreException An exception ocurred during the execution
     */
    public Set<User> searchForUser ( String sQuery ) 
    throws SecurityStoreException;

}