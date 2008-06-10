package org.meandre.core.store.security;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.meandre.core.logger.KernelLoggerFactory;
import org.meandre.core.security.Role;
import org.meandre.core.security.SecurityManager;
import org.meandre.core.security.SecurityStoreException;
import org.meandre.core.security.User;
import org.meandre.core.store.Store;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.larq.IndexBuilderString;
import com.hp.hpl.jena.query.larq.IndexLARQ;
import com.hp.hpl.jena.query.larq.LARQ;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.RDF;

/** Implements a secury store. Sensitive information should be store
 * here. For instance, it maintains all the privileges accounting for the 
 * Meandre web app.
 * 
 * @author Xavier Llor&agrave;
 * @author Peter Groves
 *
 */

public class SecurityStore implements SecurityManager {

    /** Query all nicknames */
    public static final String QUERY_ALL_NICKNAMES = 
    	"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
    	"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
    	"PREFIX meandre: <http://www.meandre.org/ontology/> \n" +
    	"PREFIX meandreSec: <" + BASE_SECURITY_URL + "> \n"+
    	"PREFIX meandreSecProp: <" + BASE_SECURITY_URL_PROPERTY + "> \n" +
    	"PREFIX meandreSecRole: <" + Role.BASE_ROLE_URL + "> \n" +
	  	"PREFIX dc: <http://purl.org/dc/elements/1.1/> \n" +
	  	"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n" +
		"SELECT DISTINCT ?nickname  " +
		"WHERE {"+
		"    ?user rdf:type meandreSec:user ." +
		"    ?user meandreSecProp:nickname ?nickname "+
		"}";

    /** Query all users */
    public static final String QUERY_ALL_USERS = 
    	"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"+
    	"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"+
    	"PREFIX meandre: <http://www.meandre.org/ontology/> \n"+
    	"PREFIX meandreSec: <"+BASE_SECURITY_URL+"> \n"+
    	"PREFIX meandreSecProp: <"+BASE_SECURITY_URL_PROPERTY+"> \n"+
    	"PREFIX meandreSecRole: <"+Role.BASE_ROLE_URL+"> \n"+
	  	"PREFIX dc: <http://purl.org/dc/elements/1.1/> \n"+
	  	"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n"+
		//"SELECT DISTINCT ?nickname ?name ?password ?date ?action "+
	  	"SELECT DISTINCT ?nickname ?name ?password ?date ?role "+
		"WHERE {"+
		"    ?user rdf:type meandreSec:user ." +
		"    ?user meandreSecProp:nickname ?nickname . "+
		"    ?user meandreSecProp:name ?name . "+
		"    ?user meandreSecProp:password ?password . "+
		"    ?user dc:date ?date . "+
		"    OPTIONAL {" +
		//"		?user meandreSecRole:grant ?action  "+
		"     ?user meandreSecRole:grant ?role  "+
		"    }"+
		"}" +
		"ORDER BY ?nickname ?name ?password ?date ?role";
		//"ORDER BY ?nickname ?name ?password ?date ?action";


    /** The model repository */
    protected Model _model = null;

    /** The core root logger */
	protected static Logger _log = KernelLoggerFactory.getCoreLogger();

    /** The text search indexer */
	private IndexBuilderString _larqBuilder = null;

	/** The current text model */
	private IndexLARQ _index = null;

	/** The parent store */
	private Store _store;

    /** Create a security store for accounting and authentication.
     *
     * @param model The model
     * @param store The store
     * @throws SecurityStoreException The password encryption ran into 
     *          trouble encrypting the admin password
     */
    public SecurityStore(Model model, Store store ) throws SecurityStoreException {
        
    	_store = store;
        _model = model;

        _model.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
        _model.setNsPrefix("meandreSec", BASE_SECURITY_URL);
        _model.setNsPrefix("meandreSecProp", BASE_SECURITY_URL_PROPERTY);
        _model.setNsPrefix("meandreSecRole", Role.BASE_ROLE_URL);
        _model.setNsPrefix("rdf", 
                "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        _model.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        _model.setNsPrefix("dc", "http://purl.org/dc/elements/1.1/");

        if ( model.size()==0 ) {
            _log.info("Empty security store. Starting a clean one.");
            Resource resRoot = _model.createResource(BASE_SECURITY_URL);
            //create the admin account
            String adminUserName = _store.getAdminUserNickName();
            String adminPass = PasswordEncryptor.encrypt(adminUserName);
            String adminFullName = "Admin Istrator";
            log("creating admin");
            User admin = createUser(adminUserName, adminFullName, adminPass);
            //give admin all available roles
            log(".initializeAccounting: adding admin's roles");
            grantRoles(admin, Role.getStandardRoles());
        }

        flush();
    }

    /** Creates the basic default accounting information. Adds info on
     * available roles to the rdf store, creates an admin user.
     * 
     * @throws SecurityStoreException The password encryptor failed
     * 
     */
/*    private void initializeAccounting() throws SecurityStoreException {
        log("initialzeAccounting begin");
		Resource resRoot = _model.createResource(BASE_SECURITY_URL);
		//log("resRoot created");
		// Create the actions
		/*Resource resBaseAct = _model.createResource(Role.BASE_WEB_ROLE_URL);
		for (Role role : Role.getStandardRoles()) {
            String sRoleUrl = role.getUrl();
            Resource resRole = _model.createResource(sRoleUrl);
			resBaseAct.addProperty(RDFS.subClassOf, resRole);
        }
		//log("web roles added");
		
		Property propRoleBase = null;
		//try{
		    propRoleBase = ResourceFactory.createProperty(Role.BASE_WEB_ROLE_URL);
		//}catch(Exception e){
		 //   log(e.toString());
		//}
        //log("roles base prop made");
		resRoot.addProperty(propRoleBase, resBaseAct);/
		//log("base role added");
		// Creates the admin user and grant all roles
        String adminUserName = _store.getAdminUserNickName();
        String adminPass = PasswordEncryptor.encrypt(adminUserName);
        String adminFullName = "Admin Istrator";
        log("creating admin");
        User admin = createUser(adminUserName, adminFullName, adminPass);
        //give admin all available roles
        log(".initializeAccounting: adding admin's roles");
        grantRoles(admin, Role.getStandardRoles());
	}
*/

 



    public User createUser(String sNickName, String sName, String sPassword) 
    throws SecurityStoreException {
        //create storedUser instance
        StoredUser stUser = new StoredUser(sNickName, sName, sPassword);

        //add the model of the storedUser to the rdf store
        _model.add(stUser.getModel());
        log(".createUser: added model of new user:" + sNickName);
        flush();

        //return the storedUser
        return stUser;
    }


    /** Removes a user form the system.
     * 
     * @param usr The user to delete
     * @throws SecurityStoreException Admin user cannot be deleted from the system
     */
    public void removeUser(User user) throws SecurityStoreException {
    	StoredUser stUser = userToStoredUser(user);
    	if (user.getNickName().equals(_store.getAdminUserNickName()) )
    		throw new SecurityStoreException(
                    "Admin user cannot be deleted from the system.");
    	else {
    		Model modUser = stUser.getModel();
    		_model.remove(modUser);
    		flush();    	
    	}
    }
    /** Return the nick name of every user in the store.
     * 
     * @return The user nick names
     */
    public Set<String> getUsersNickNames(){
   
    	Query query = QueryFactory.create(QUERY_ALL_NICKNAMES) ;
		QueryExecution exec = QueryExecutionFactory.create(query, _model, null);
		ResultSet results =  exec.execSelect();
		Set<String> setNickNames = new HashSet<String>();
		while ( results.hasNext() ) {
			QuerySolution sol = results.nextSolution();
			// ?nickname
			setNickNames.add(sol.getLiteral("nickname").getLexicalForm());
		}
    	return setNickNames;
    }
    
    public Set<User> getUsers() throws SecurityStoreException{
        //not sure why this cast is necessary
        Set<User> users = new HashSet<User>();
        StringBuffer sb = new StringBuffer();
        sb.append("SecurityStore.getUsers: ");
        for(StoredUser stu: getStoredUsers()){
            sb.append("<" + stu.getNickName() + "> ");
            users.add(stu);
        }
        //log(sb.toString());
        return users;
    }
    
    public User getUser(String sNickName) throws SecurityStoreException {
        return getStoredUser(sNickName);
    }


   /** Check if the given role has been granted to the given user.
     * 
     * @param sRole The role
     * @param sNickName The user nickname
     * @return True if role granted, false otherwhise
     */
    public boolean hasRoleGranted (User usr, Role roleToCheck) {

        boolean bHasRole = false;
        try{
        	//get the user with the requested nickname
        	StoredUser stUsr = userToStoredUser(usr);
            //find the role in it's set of roles
            bHasRole = stUsr.getRoles().contains(roleToCheck);
        }
	    catch (SecurityStoreException e) {
			_log.warning("Security store violation " + e);
		}
    	return bHasRole;
    }

    /**
     * looks up a user in the store and returns its roles.
     */
    public Set<Role> getRolesOfUser(User usr)
    throws SecurityStoreException{
        StoredUser stUser = userToStoredUser(usr);
        return stUser.getRoles();
    }

    public void grantRole(User usr, Role roleToGrant)
            throws SecurityStoreException {
        log("grantRole begin. user = " + usr.getNickName() + ", role = " +
                roleToGrant.toString());
        StoredUser stUser = userToStoredUser(usr);
        //log("got storedUser instance");
		if (roleToGrant.isStandard()) {
			Resource resUser = _model.createResource(stUser.getUrl());	
            Property propRole = ResourceFactory.createProperty(
                    Role.ROLE_GRANT_PROPERTY_URL);
            String sRoleUrl = roleToGrant.getUrl();
            Resource resRole = ResourceFactory.createResource(sRoleUrl);
			resUser.addProperty(propRole, resRole);
            flush();
		}
		else { 
			throw new SecurityStoreException("Role \'" + 
                    roleToGrant.toString() + 
                    "\' is not a valid one understood by the local store.");
        }
    }
    
    /**
     * associates a user with a set of roles being granted to it and
     * commits the change to the Store.
     *
     * @param usr User getting new privileges
     * @param rolesToGrant the new privilege levels to assign to usr
     */
    public void grantRoles(User usr, Set<Role> rolesToGrant)
    throws SecurityStoreException {
        for(Role role : rolesToGrant){
               grantRole(usr, role);
        }
    }
    
    public void revokeRole(User usr, Role roleToRevoke)
            throws SecurityStoreException {
        log("revokeRole begin. user = " + usr.getNickName() + ", role = " +
                roleToRevoke.toString());
        StoredUser stUser = userToStoredUser(usr);
        if (roleToRevoke.isStandard()) {
			Model modTmp = ModelFactory.createDefaultModel();
			Resource resUser = modTmp.createResource(stUser.getUrl());	
            Property propGrantRole = ResourceFactory.createProperty(
                    Role.ROLE_GRANT_PROPERTY_URL);
            String sRoleUrl = roleToRevoke.getUrl();
            Resource resRole = ResourceFactory.createResource(sRoleUrl);
			resUser.addProperty(propGrantRole, resRole);
			_model.remove(modTmp);
            flush();
		}
		else { 
			throw new SecurityStoreException("Role \'" + 
                    roleToRevoke.toString() + 
                    "\' is not a valid one understood by the local store.");
		}  
    }


    public void revokeRoles(User usr, Set<Role> rolesToRevoke)
    throws SecurityStoreException {
        for(Role role: rolesToRevoke ) {
               revokeRole(usr, role);
        }  
    }

    public void revokeAllRoles(User usr) throws SecurityStoreException {
        Set<Role> roles = getRolesOfUser(usr);
        log(roles.toString());
        revokeRoles(usr, roles);
    }
    
	public Set<Role> getValidRoles() throws SecurityStoreException {
		return Role.getStandardRoles();
	}
    /*public User updateUser(String nickName, String name, String password)
    throws SecurityStoreException {
       StoredUser stUser = getStoredUser(nickName);
       Set<Role> existingRoles = stUser.getRoles();
       removeUser(stUser);
       User newUser = createUser(nickName, name, password);
       grantRoles(newUser, existingRoles); 
       return newUser;
    }*/


    
    /**
     * return the internal rdf model of this security store
     */
    public Model getModel(){
        return _model;
    }
    
    /**
     * get the default roles given to new users. They are: "Home" , "Profile",
     * and "Credits"
     *
     * @return the default roles in an array
     */
    public static Set<Role> getDefaultRoles(){
        Set<Role> roles = new HashSet<Role>(3);
        roles.add(Role.HOME);
        roles.add(Role.PROFILE);
        roles.add(Role.CREDITS);
        return roles;
    }    
    
    private Set<StoredUser> getStoredUsers()
    throws SecurityStoreException{
        //log("getUsers begin");
        Query query = QueryFactory.create(QUERY_ALL_USERS) ;
        QueryExecution exec = QueryExecutionFactory.create(query, _model, null);
        ResultSet results =  exec.execSelect();
        
        Set<StoredUser> setUsers = new HashSet<StoredUser>();
        String sLastNickName = null;
        StoredUser usr = null;
        
        while ( results.hasNext() ) {
            QuerySolution sol = results.nextSolution();
            //log("Query record:" + sol.toString());
            // ?nickname ?name ?password ?date ?action
            String sNickName = sol.getLiteral("nickname").getString();
            String sName = sol.getLiteral("name").getString();
            String sPassword = sol.getLiteral("password").getString();
            String sDate = sol.getLiteral("date").getString();
            
            Resource resAction = sol.getResource("role");
            
            if ((sLastNickName == null) || (!sNickName.equals(sLastNickName))){
                //log("new last nickname");
                usr = new StoredUser(sNickName, sName, sPassword, sDate);
                setUsers.add(usr);
            } 
            if(resAction != null){
                //log("resAction was not null, adding role");
                usr.addRole(Role.fromResource(resAction));
            }
            sLastNickName = sNickName;
        }
        //log("getUsers end");
        return setUsers;
    }   
    /**
     * get the local representation of a user given the generic 
     * object representation. Currently this is the same
     * as getStoredUser(user.getNickName()), but it may be
     * optimized to do more caching and casting in the future.
     *
     */
    private StoredUser userToStoredUser(User user) 
    throws SecurityStoreException{
        return getStoredUser(user.getNickName());
    }

    /**
     * Finds the User in the store that has the input nickname and returns
     * it (as a StoredUser instance).
     */
    private StoredUser getStoredUser(String sNickName) 
    throws SecurityStoreException {
        //get all users
        Set<StoredUser> stUsers = getStoredUsers();
        //find one with the input nickname
        StoredUser stUser = null;
        //log("getStoredUser: listing available users");
        for(StoredUser stuCandidate : stUsers){
            //log("got user:" + stuCandidate.getNickName());
            if(stuCandidate.getNickName().equals(sNickName)){
                stUser = stuCandidate;
            }
        }
        //log("getStoredUser: done listing available users");
        if(stUser == null){
            throw new SecurityStoreException("Requested user \'" +
                sNickName + "\' does not exist");
        }
        return stUser;
    }
    
    private void log(String msg){
        //_log.finer(msg);
        _log.info(msg + "\n");
    }
    
    /** Flushes the current system model to disk.
    *
    */
    private void flush() {
       // Dump the realm file
       // 
       // The realm file is dump to the place were the engine was started
       try {
           File realmFile = new File(_store.getRealmFilename());
           PrintStream ps = new PrintStream(new FileOutputStream(realmFile));
           
           // username: password[,rolename ...]
           for (StoredUser usr : getStoredUsers()) {
               ps.print(usr.getNickName() + ": CRYPT:" + usr.getPassword());
               for (Role role : usr.getRoles() )
                   ps.print("," + role.getUrl());
               ps.println();
           }
                              
           ps.flush();
           ps.close();
           
       } catch (IOException e) {
           _log.severe("Could not update realm file: ." + 
               File.separator + _store.getRealmFilename());
       } 
       catch (SecurityStoreException e) {
           _log.severe("Could not update realm because security exception " +
                   "was thrown");
       }
       // Read and index all literal strings.
       _larqBuilder = new IndexBuilderString();
       // Create an index based on existing statements
       _larqBuilder.indexStatements(_model.listStatements());
       // Finish indexing
       _larqBuilder.closeForWriting();
       // Create the access index  
       _index = _larqBuilder.getIndex() ;
       // Make globally available
       LARQ.setDefaultIndex(_index);
   }
    private class StoredUser extends User{

    	/** The creation date */
	    private String _sDate = null;
	
    	/** The user password */
	    private String _sPassword = null;
	
    	/** The set of role actions assigned to the user */
	    private Set<Role> _actionRoles = null;       


   
        
        public StoredUser(String sNickName, String sName, String sPassword,
                String sDate) throws SecurityStoreException{
            super(sNickName, sName);
            _sDate = sDate;
            _sPassword = sPassword;
            _actionRoles = new HashSet<Role>();
            //_actionRoles.add(Role.HOME);
        }

        /**
         * Constructor typically used for brand new user accounts. The date
         * is set to the current time this method is called.
         */
        public StoredUser(String sNickName, String sName, String sPassword) 
        throws SecurityStoreException {
            super(sNickName, sName);
            _sPassword = sPassword;
            Date dateNow = new Date();
            Format dateFormatter=  new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	    	String sNow = dateFormatter.format(dateNow);
	    	_sDate = sNow;
            _actionRoles = new HashSet<Role>();
            //_actionRoles.add(Role.HOME);
        }


        public String getPassword(){ 
            return _sPassword;
        }

        public String getDate(){
            return _sDate;
        }

        public Set<Role> getRoles(){
            return _actionRoles;
        }

        public void addRole(Role role){
            _actionRoles.add(role);
        }
        
        /** gives this user all of the default roles */
        public void assignDefaultRoles(){
            _actionRoles.addAll(getDefaultRoles());
            
        }

        private String getUrl(){
            return User.BASE_USER_PROPERTY_URL + "/" + this.getNickName();
        }
        public Model getModel(){
            Model model = ModelFactory.createDefaultModel();
    		model.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
            model.setNsPrefix("meandreSec", 
                    SecurityManager.BASE_SECURITY_URL);
            model.setNsPrefix("meandreSecProp",
                    SecurityManager.BASE_SECURITY_URL_PROPERTY);
            model.setNsPrefix("meandreSecRole", Role.BASE_ROLE_URL);
            model.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
            model.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
            model.setNsPrefix("dc", "http://purl.org/dc/elements/1.1/");

            Resource resRoot = model.createResource(
                    SecurityManager.BASE_SECURITY_URL);
       	    Resource resUser = model.createResource(this.getUrl());
		
    		resUser.addProperty(ResourceFactory.createProperty(
                    SecurityStore.BASE_SECURITY_URL_PROPERTY+"name"), 
                    ResourceFactory.createTypedLiteral(this.getName()));

			resUser.addProperty(ResourceFactory.createProperty(
			        SecurityStore.BASE_SECURITY_URL_PROPERTY+"nickname"), 
                    ResourceFactory.createTypedLiteral(this.getNickName()));

			resUser.addProperty(ResourceFactory.createProperty(
                    SecurityStore.BASE_SECURITY_URL_PROPERTY+"password"), 
                    ResourceFactory.createTypedLiteral(this.getPassword()));

			resUser.addProperty(DC.date, 
                    ResourceFactory.createTypedLiteral(this.getDate(), 
                        XSDDatatype.XSDdateTime));

			resUser.addProperty(RDF.type, 
                    model.createResource(User.BASE_USER_URL));
		
		for(Role actionRole : _actionRoles){
			resUser.addProperty(
                ResourceFactory.createProperty(Role.ROLE_GRANT_PROPERTY_URL), 
                ResourceFactory.createResource(actionRole.getUrl()));
		}	
		
		resRoot.addProperty(ResourceFactory.createProperty(BASE_USER_PROPERTY_URL),
            resUser);
		
		return model;
        }
    }



}
