package org.meandre.core.store.security;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.meandre.core.security.Role;
import org.meandre.core.security.SecurityManager;
import org.meandre.core.security.SecurityStoreException;
import org.meandre.core.security.User;
/**
 * Provides stand alone tests for classes implementing the
 * SecurityManager interface. Like in an exam where a Proctor gives
 * a test. 
 *
 * This design (a set of static methods) is used to steer clear of 
 * junit's introspection and annotation weirdness that doesn't always 
 * play well with inheritance. (A more logical design would be for
 * these methods to be in a superclass that each *Test class would
 * extend, but what are you gonna do?)
 *
 * These test methods are destructive, in that they will change the
 * state of the securitymanager (add, delete accounts, etc) and not
 * attempt to return them to their state at input.
 *
 * @author Peter Groves
 */

public class SecurityManagerProctor{

    /** tests to see if a new SecurityManager has the expected admin user
     * account properly set up.
     */
    public static void testInitialState(SecurityManager secMan){
        try{
            //check admin account is there
            User usrA = secMan.getUser("admin");
            Assert.assertEquals("admin", usrA.getNickName());
            Assert.assertEquals("Admin Istrator", usrA.getName());

            //check admin is the only account
            Set<User> allUsers = secMan.getUsers();
            Assert.assertEquals(1, allUsers.size());

            //check admin has all roles
            Set<Role> observedRoles = secMan.getRolesOfUser(usrA);
            Set<Role> expectedRoles = Role.getStandardRoles();
            Assert.assertTrue("! all observed in expected",
                    expectedRoles.containsAll(observedRoles));
            Assert.assertTrue("! all expected in observed",
                    observedRoles.containsAll(expectedRoles));
        }catch(Exception e){
            Assert.fail(e.toString());
        }
    }

    /** just creates a user and checks that it got a valid response.
     */
    public static void testCreateUser(SecurityManager secMan){

        
        User usr1 = makeUser1(secMan);
        try{
            Set<User> observedUsers = secMan.getUsers();
            Assert.assertTrue("! created user in observed users", 
                    observedUsers.contains(usr1));
            
        }catch(SecurityStoreException sse){
            Assert.fail("failed getting users: \n" + sse.toString());
        }
        return;
    }

    /**
     * creates a user then removes it. checks that the user is or is
     * not there using getUser.
     */
    public static void testRemoveUser(SecurityManager secMan){
    	//User usr1 = makeUser1(secMan);
    	makeUser1(secMan);
        removeUser1(secMan);
        return;
    }

    /**
     * creates some users and removes some of them, checking that
     * getUsers returns the correct set of users at all times.
     */
    public static void testGetUsers(SecurityManager secMan){
        try{
            Set<User> initialUsers = secMan.getUsers();
            User usr1 = makeUser1(secMan);
            Set<User> observedUsers = secMan.getUsers();
            Assert.assertTrue("! initial users survived (A)", 
                    observedUsers.containsAll(initialUsers));
            Assert.assertTrue("! created user exists (1)", 
                    observedUsers.contains(usr1));

            User usr2 = makeUser2(secMan);
            removeUser1(secMan);
            observedUsers = secMan.getUsers();

            Assert.assertTrue("! initial users survived (B)", 
                    observedUsers.containsAll(initialUsers));
            Assert.assertTrue("! created user exists (2)", 
                    observedUsers.contains(usr2));
            Assert.assertFalse("! removed user is gone (3)", 
                    observedUsers.contains(usr1));
        }catch(Exception e){
            Assert.fail(e.toString());
        }
    }

    /**
     * creates a user, retrieves it and checks the returned value.
     */
    public static void testGetUser(SecurityManager secMan){
        try{
            User usr1a = makeUser1(secMan);
            User usr1b = secMan.getUser(_userName1);
            Assert.assertEquals(usr1a.getNickName(), usr1b.getNickName());
            Assert.assertEquals(usr1a.getName(), usr1b.getName());
        }catch(Exception e){
            Assert.fail(e.toString());
        }

    }
    
    /** 
     * adds two users, checks that they are added appropriately to the
     * list of returned nicknames.
     */
  /*  public static void testGetUsersNickNames(SecurityManager secMan){
        try{
            Set<String> expectedNicks = new HashSet<String>();
            expectedNicks.add("admin");
            Set<String> observedNicks = secMan.getUsersNickNames();
            Assert.assertEquals(expectedNicks, observedNicks);

            // add another user, then check
            expectedNicks.add(_userName1);
            makeUser1(secMan);
            observedNicks = secMan.getUsersNickNames();
            Assert.assertEquals(expectedNicks, observedNicks);

            //add one more, then check again
            expectedNicks.add(_userName2);
            makeUser2(secMan);
            observedNicks = secMan.getUsersNickNames();
            Assert.assertEquals(expectedNicks, observedNicks);

            //remove one of the users, check again
            expectedNicks.remove(_userName1);
            removeUser1(secMan);
            observedNicks = secMan.getUsersNickNames();
            Assert.assertEquals(expectedNicks, observedNicks);


        }catch(Exception e){
            Assert.fail(e.toString());
        }

    }
*/
    /**
     * creates a user, gives it a role. just makes sure the call succeeds.
     */
    public static void testGrantRole(SecurityManager secMan){
        try{
            User usr1 = makeUser1(secMan);
            secMan.grantRole(usr1, Role.FLOWS);
        }catch(Exception e){
            Assert.fail(e.toString());
        }
    }
    /**
     * same as testGrantRole, but uses multiple roles.
     */
    public static void testGrantRoles(SecurityManager secMan){
        try{
            User usr1 = makeUser1(secMan);
            Set<Role> rolesToGrant = new HashSet<Role>();
            rolesToGrant.add(Role.FLOWS);
            rolesToGrant.add(Role.COMPONENTS);
            secMan.grantRoles(usr1, rolesToGrant);
        }catch(Exception e){
            Assert.fail(e.toString());
        }
    }


    /**
     * creates a user, tests the secman says it doesn't have a role, 
     * gives it the role, tests if the secMan says it has the role.
     */
    public static void testHasGrantedRole(SecurityManager secMan){
        Role testRole = Role.FLOWS;
        try{
            User usr1 = makeUser1(secMan);

            //check it doesn't have the testRole
            boolean observedHasRole = secMan.hasRoleGranted(usr1, testRole);
            Assert.assertFalse("has role but shouldn't", observedHasRole);

            //grant the test role and check that it does have it
            secMan.grantRole(usr1, testRole);
            observedHasRole = secMan.hasRoleGranted(usr1, testRole);
            Assert.assertTrue("! has role", observedHasRole);

        }catch(Exception e){
            Assert.fail(e.toString());
        }      
    }

    /**
     * creates  a user, gives it some roles, tests if getRoles returns
     * the expected roles.
     */
    public static void testGetRolesOfUser(SecurityManager secMan){
        try{
            User usr1 = makeUser1(secMan);
            Set<Role> observedRolesBefore = secMan.getRolesOfUser(usr1);

            Set<Role> rolesToGrant = new HashSet<Role>();
            rolesToGrant.add(Role.FLOWS);
            rolesToGrant.add(Role.COMPONENTS);
            rolesToGrant.add(Role.PUBLISH);
            secMan.grantRoles(usr1, rolesToGrant);

            Set<Role> observedRolesAfter = secMan.getRolesOfUser(usr1);
            
            for(Role rl : observedRolesBefore){
                log("observedRoleBefore : " + rl.toString());
            }
            for(Role rl : observedRolesAfter){
                log("observedRoleAfter : " + rl.toString());
            }            
            Assert.assertTrue("! retained initial roles", 
                    observedRolesAfter.containsAll(observedRolesBefore));
            Assert.assertTrue("! has granted roles", 
                    observedRolesAfter.containsAll(rolesToGrant));

        }catch(Exception e){
            Assert.fail(e.toString());
        }  
    }
    /**
     * creates a user, gives it a role, checks hasRoleGranted, revokes
     * the role, checks hasRoleGranted.
     */
    public static void testRevokeRole(SecurityManager secMan){
        Role testRole = Role.FLOWS;
        try{
            User usr1 = makeUser1(secMan);

            //check it doesn't have the testRole
            boolean observedHasRole = secMan.hasRoleGranted(usr1, testRole);
            Assert.assertFalse("has role but shouldn't", observedHasRole);

            //grant the test role and check that it does have it
            secMan.grantRole(usr1, testRole);
            observedHasRole = secMan.hasRoleGranted(usr1, testRole);
            Assert.assertTrue("! has role", observedHasRole);

            //revoke the role, check it doesn't have it
            secMan.revokeRole(usr1, testRole);
            observedHasRole = secMan.hasRoleGranted(usr1, testRole);
            Assert.assertFalse("has role but it was revoked", observedHasRole);

        }catch(Exception e){
            Assert.fail(e.toString());
        }     
    }
    /**
     * same as testRevokeRole but does multiple roles.
     */
    public static void testRevokeRoles(SecurityManager secMan){
        try{

            User usr1 = makeUser1(secMan);
            Set<Role> observedRolesBefore = secMan.getRolesOfUser(usr1);

            Set<Role> rolesToGrant = new HashSet<Role>();
            rolesToGrant.add(Role.FLOWS);
            rolesToGrant.add(Role.COMPONENTS);
            secMan.grantRoles(usr1, rolesToGrant);

            Set<Role> observedRolesAfterGrant = secMan.getRolesOfUser(usr1);
            Assert.assertTrue("! retained initial roles", 
                    observedRolesAfterGrant.containsAll(observedRolesBefore));
            Assert.assertTrue("! has granted roles", 
                    observedRolesAfterGrant.containsAll(rolesToGrant));

            secMan.revokeRoles(usr1, rolesToGrant);
            Set<Role> observedRolesAfterRevoke = secMan.getRolesOfUser(usr1);
            Assert.assertTrue("! retained initial roles", 
                    observedRolesAfterRevoke.containsAll(observedRolesBefore));

            //check that the observed roles doesn't contain any of the
            //revoked roles
            for(Role revokedRole: rolesToGrant){
                Assert.assertFalse(
                    "still has revoked role:" + revokedRole.toString(), 
                    observedRolesAfterRevoke.contains(revokedRole));
            }
        }catch(Exception e){
            Assert.fail(e.toString());
        }
    }
    /**
     * creates a user, gives it some roles, calls revoke all roles, calls
     * getRoles on the user and makes sure it's empty.
     */
    public static void testRevokeAllRoles(SecurityManager secMan){
        try{
            //making the user gives it some default roles, too
            User usr1 = makeUser1(secMan);
            secMan.grantRoles(usr1, SecurityStore.getDefaultRoles());
            
            //make sure there are roles in there to revoke
            Set<Role> observedRolesBefore = secMan.getRolesOfUser(usr1);
            Assert.assertFalse("initial role set is empty", 
                observedRolesBefore.isEmpty());

            //revoke all and check there are now no roles
            secMan.revokeAllRoles(usr1);
            Set<Role> observedRolesAfter = secMan.getRolesOfUser(usr1);
            log("RolesAfter RevokeAll:" + observedRolesAfter.toString());
            Assert.assertTrue("role set is not empty", 
                observedRolesAfter.isEmpty());
        
        }catch(Exception e){
            Assert.fail(e.toString());
        }

    }
    public static void testGetValidRoles(SecurityManager secMan){
    	Set<Role> expectedRoles = Role.getStandardRoles();
    	try{
    		Set<Role> observedRoles = secMan.getValidRoles();
    		Assert.assertEquals(expectedRoles, observedRoles);
    		
    	}catch(Exception e){
            Assert.fail(e.toString());
        }
    	
    	
    }
    /**
     * Part 1 of the persistence test. Creates some users with some 
     * roles. After calling this, the securityManager should be shut
     * down.
     */
    public static void testPersistencePart1(SecurityManager secMan){
        try{
            User usr1 = makeUser1(secMan);
            secMan.grantRoles(usr1, SecurityStore.getDefaultRoles());

            Set<Role> rolesToGrant = new HashSet<Role>();
            rolesToGrant.add(Role.FLOWS);
            rolesToGrant.add(Role.COMPONENTS);
            secMan.grantRoles(usr1, rolesToGrant);

            User usr2 = makeUser2(secMan);
            secMan.grantRoles(usr2, SecurityStore.getDefaultRoles());
            validatePersistenceState(secMan);
        }catch(Exception e){
            Assert.fail(e.toString());
        }


    }
    /**
     * Part 2 of the persistence test. Restart the SecurityManager after
     * part 1 and then call this method. Checks to see if the users and
     * their roles are in the expected state given what changes were
     * made in part 1.
     *
     */
    public static void testPersistencePart2(SecurityManager secMan){
        validatePersistenceState(secMan);
    }

    /**
     * used by the testPersistence* methods. the assertions in this
     * method should be valid when the security manager is about to
     * be shut down and right after it is started back up.
     */
    private static void validatePersistenceState(SecurityManager secMan){
        try{
            ////
            //user1 checks
            User usr1 = secMan.getUser(_userName1);
            //check user1 basic info
            Assert.assertEquals("! userName1", _userName1, usr1.getNickName());
            Assert.assertEquals("! userFullName1", _userFullName1, usr1.getName());
            //check user1 roles
               Set<Role> expectedRoles1 = SecurityStore.getDefaultRoles();
            expectedRoles1.add(Role.FLOWS);
            expectedRoles1.add(Role.COMPONENTS);
            Set<Role> observedRoles1 = secMan.getRolesOfUser(usr1);
            Assert.assertTrue("! user1 roles",
                observedRoles1.equals(expectedRoles1));

            ////
            //user2 checks
            User usr2 = secMan.getUser(_userName2);
            //check user1 basic info
            Assert.assertEquals("! userName2", _userName2, usr2.getNickName());
            Assert.assertEquals("! userFullName2", _userFullName2, usr2.getName());
            //check user2 roles
            Set<Role> expectedRoles2 = SecurityStore.getDefaultRoles();
            Set<Role> observedRoles2 = secMan.getRolesOfUser(usr2);
            Assert.assertTrue("! user2 roles",
                observedRoles2.equals(expectedRoles2));

        }catch(Exception e){
            Assert.fail(e.toString());
        }
    }

    /** attempts to reset a security manager back to a clean state by deleting
     * any user accounts that are the default admin account. For use in
     * tearDown methods.
     * @param secMan
     */
    public static void removeAllButAdmin(SecurityManager secMan){
        log(".removeAllButAdmin begin.");
        try{
            Set<User> allUsers = secMan.getUsers();
            for(User usr : allUsers){
                if(!usr.getNickName().equals("admin")){
                    log(".removeAllButAdmin: removing user = " +
                            usr.toString());
                    secMan.removeUser(usr);
                }
            }
        
        }catch(SecurityStoreException sse){
            log(".removeAllButAdmin failed. \n" +
                    sse.toString());
        }
        log(".removeAllButAdmin end.");
    }
    
    private static void log(String msg){
        System.out.println("SecurityManagerProctor:\n" + msg);
    }
    
    //user 1
    private static String _userName1 = "user1";
    private static String _userFullName1 = "Mr. User One";
    private static String _userPass1 = "passOfOne";

    /**
     * creates a test user 1 in the securityManager and returns it.
     */
    private static User makeUser1(SecurityManager secMan){
        User usr = null;
        try{
        	log("making user1");
            usr = secMan.createUser(_userName1, _userFullName1, _userPass1);
            log("user1 made");
            //Model mod = ((SecurityStore)secMan).getModel();
            //StringWriter sw = new StringWriter();
            //mod.write(sw);
            //log("model after user created:");
            //log(sw.toString());
            //secMan.grantRoles(usr, SecurityStore.getDefaultRoles());
            log("user 1 roles granted");
        }catch(SecurityStoreException sse){
            Assert.fail("Problem creating user 1:\n" + sse.toString());
        }
        Assert.assertEquals("! _userName1", _userName1, usr.getNickName());
        Assert.assertEquals("! _userFullName1", _userFullName1, usr.getName());

        return usr;
    }

    private static void removeUser1(SecurityManager secMan){
        try{
            User usr = secMan.getUser(_userName1);
            Assert.assertEquals("Removing: ! _userName1", 
                    _userName1, usr.getNickName());
            Assert.assertEquals("Removing: ! _userFullName1", 
                    _userFullName1, usr.getName());
            secMan.removeUser(usr);
        }catch(SecurityStoreException sse){
            Assert.fail("Problem removing user 1:\n" + sse.toString());
        }

        return;
    }

    //user 1
    private static String _userName2 = "user2";
    private static String _userFullName2 = "Mr. User Two";
    private static String _userPass2 = "passOfTwo";

    /**
     * creates a test user 1 in the securityManager and returns it.
     */
    private static User makeUser2(SecurityManager secMan){
        User usr = null;
        try{
            usr = secMan.createUser(_userName2, _userFullName2, _userPass2);
            //secMan.grantRoles(usr, SecurityStore.getDefaultRoles());
        }catch(SecurityStoreException sse){
            Assert.fail("Problem creating user 2:\n" + sse.toString());
        }
        Assert.assertEquals("! _userName2", _userName2, usr.getNickName());
        Assert.assertEquals("! _userFullName2", _userFullName2, usr.getName());

        return usr;
    }

    @SuppressWarnings("unused")
	private static void removeUser2(SecurityManager secMan){
        try{
            User usr = secMan.getUser(_userName2);
            Assert.assertEquals("Removing: !_userName2", _userName2, 
                    usr.getNickName());
            Assert.assertEquals("Removing: ! _userFullName2", 
                    _userFullName2, usr.getName());
            secMan.removeUser(usr);
        }catch(SecurityStoreException sse){
            Assert.fail("Problem removing user 2:\n" + sse.toString());
        }

        return;
    }
    

}
