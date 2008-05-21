package org.meandre.client;

import java.util.Set;

import org.meandre.core.store.security.Role;
import org.meandre.core.store.security.User;


public class MeandreAdminClient{

    /** retrieve a list of all users from the remote Meandre server. 
     * @throws Exception */
    public Set<User> retrieveUsers() throws Exception {
        throw new Exception("Not implemented");
        //return null;
    }

    /** add a new user to the remote Meandre server. will overwrite an existing
     * user with the same name
     */
    public boolean addUser(User user) throws Exception {
        throw new Exception("Not implemented");
        //return false;
    }

    /**
     * deletes a user account and all roles assigned to it from the server. 
     */
    public boolean removeUser(User user) throws Exception {
        throw new Exception("Not implemented");
        //return false;
    }
    
    /**
     * retrieves what access priveleges the input user has assigned.
     */
    public Set<Role> retrieveRolesOfUser(User user) throws Exception {
        throw new Exception("Not implemented");
        //return null;
    }

    /**
     * a list of all roles the server supports.
     */
    public Set<Role> retrieveAvailableRoles() throws Exception {
        throw new Exception("Not implemented");
        //return null;
    }

    /**
     * adds access priveleges of a role to a user account.
     */
    public boolean assignRoleToUser(User user, Role role) throws Exception {
        throw new Exception("Not implemented");
        //return false;
    }

    /**
     * removes access privelages of a role from a user account.
     */
    public boolean revokeRoleOfUser(User user, Role role) throws Exception {
        throw new Exception("Not implemented");
        //return false;
    }

    /**
     * checks if a user has a role. actually calls retrieveRolesOfUsers
     * and performs a lookup.
     */
    public boolean userHasRoleGranted(User user, Role role) throws Exception {
        throw new Exception("Not implemented");
        //return false;
    }
    
    /**
     * returns true if this instance of a MeandreClient has sufficient priveleges
     * to modify user roles.
     */
    public boolean canModifyRoles() throws Exception {
        throw new Exception("Not implemented");
        //return false;
    }

}
