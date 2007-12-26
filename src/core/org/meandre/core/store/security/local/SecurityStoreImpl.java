package org.meandre.core.store.security.local;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.meandre.WSCoreBootstrapper;
import org.meandre.core.store.Store;
import org.meandre.core.store.security.Action;
import org.meandre.core.store.security.PasswordEncryptor;
import org.meandre.core.store.security.Role;
import org.meandre.core.store.security.SecurityStore;
import org.meandre.core.store.security.SecurityStoreException;
import org.meandre.core.store.security.User;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.larq.IndexBuilderString;
import com.hp.hpl.jena.query.larq.IndexLARQ;
import com.hp.hpl.jena.query.larq.LARQ;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/** Implements a secury store. Sensitive information should be store
 * here. For instance, it maintains all the accounting for the Meandre
 * web app.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class SecurityStoreImpl implements SecurityStore {

    /** The base system url */
    public static final String BASE_SECURITY_URL =
            "http://www.meandre.org/accounting/";

    /** The base system url */
    public static final String BASE_SECURITY_URL_PROPERTY =
            "http://www.meandre.org/accounting/property/";

    /** Query all nicknames */
    public static final String QUERY_ALL_NICKNAMES = 
    	"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"+
    	"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"+
    	"PREFIX meandre: <http://www.meandre.org/ontology/> \n"+
    	"PREFIX meandreSec: <"+BASE_SECURITY_URL+"> \n"+
    	"PREFIX meandreSecProp: <"+BASE_SECURITY_URL_PROPERTY+"> \n"+
    	"PREFIX meandreSecRole: <"+Role.BASE_ROLE_URL+"> \n"+
	  	"PREFIX dc: <http://purl.org/dc/elements/1.1/> \n"+
	  	"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n"+
		"SELECT DISTINCT ?nickname  "+
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
		"SELECT DISTINCT ?nickname ?name ?password ?date ?action "+
		"WHERE {"+
		"    ?user rdf:type meandreSec:user ." +
		"    ?user meandreSecProp:nickname ?nickname . "+
		"    ?user meandreSecProp:name ?name . "+
		"    ?user meandreSecProp:password ?password . "+
		"    ?user dc:date ?date . "+
		"    OPTIONAL {" +
		"		?user meandreSecRole:grant ?action  "+
		"    }"+
		"}" +
		"ORDER BY ?nickname ?name ?password ?date ?action";

    /** The model repository */
    protected Model model = null;

    /** The logger for the bootstrapper */
    protected static Logger log = null;

    // Initializing the logger and its handlers
    static {
        log = Logger.getLogger(WSCoreBootstrapper.class.getName());
        log.setLevel(Level.CONFIG);
        log.addHandler(WSCoreBootstrapper.handler);
    }

    /** The text search indexer */
	private IndexBuilderString larqBuilder = null;

	/** the current text model */
	private IndexLARQ index = null;
	

    /** Create a security store for accounting and authentication.
     *
     * @param model The model
     * @throws SecurityStoreException The password encryption ran into trouble encrypting the admin password
     */
    public SecurityStoreImpl(Model model) throws SecurityStoreException {
        
        this.model = model;

        this.model.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
        this.model.setNsPrefix("meandreSec",BASE_SECURITY_URL);
        this.model.setNsPrefix("meandreSecProp",BASE_SECURITY_URL_PROPERTY);
        this.model.setNsPrefix("meandreSecRole",Role.BASE_ROLE_URL);
        this.model.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        this.model.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        this.model.setNsPrefix("dc", "http://purl.org/dc/elements/1.1/");

        if ( model.size()==0 ) {
            log.info("Empty security store. Starting a clean one.");
            initializeAccounting();
        }

        flush();
    }

    /** Creates the basic default accounting information.
     * 
     * @throws SecurityStoreException The password encryptor failed
     * 
     */
    private void initializeAccounting() throws SecurityStoreException {
		Resource resRoot = model.createResource(BASE_SECURITY_URL);
		Date     dateNow = new Date();
		String      sNow = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(dateNow);
		
		// Create the actions
		Resource resBaseAct = model.createResource(Action.BASE_WEB_ACTION_URL);
		for ( String sURL:Action.ALL_BASIC_ACTION_URLS)
			resBaseAct.addProperty(ResourceFactory.createProperty("http://www.w3.org/2000/01/rdf-schema#subClassOf"), model.createResource(sURL));
		resRoot.addProperty(ResourceFactory.createProperty(Action.BASE_ACTION_URL),resBaseAct);
		
		// Creates the admin user and grant all roles
		new UserImpl(Store.getAdminUserNickName(),"Admin Istrator",PasswordEncryptor.encrypt(Store.getAdminUserNickName()),sNow,Action.ALL_BASIC_ACTION_URLS,this,true);
	
	}

 
	/** Flushes the current system model to disk.
     *
     *
     */
    public void flush() {
  
		// Dump the realm file
		try {
			PrintStream ps = new PrintStream(new FileOutputStream(new File(
					WSCoreBootstrapper.JETTY_HOME + File.separator
							+ Store.getRealmFilename())));
			
			// username: password[,rolename ...]
			for ( User usr:getUsers() ) {
				ps.print(usr.getNickName()+": CRYPT:"+usr.getPassword());
				for ( String sRole:usr.getGrantedActionRoles() )
					ps.print(","+sRole);
				ps.println();
			}
			                   
			ps.flush();
	        ps.close();
	        
		} catch (IOException e) {
			log.severe("Could not update realm file: "+WSCoreBootstrapper.JETTY_HOME+File.separator+Store.getRealmFilename());
		} 
		catch (SecurityStoreException e) {
			log.severe("Could not update realm because security exception was thrown");
		}
        
		
        // Read and index all literal strings.
		this.larqBuilder = new IndexBuilderString();
		
		// Create an index based on existing statements
		larqBuilder.indexStatements(model.listStatements());
		
		// Finish indexing
		larqBuilder.closeForWriting();
		
		// Create the access index  
		this.index = larqBuilder.getIndex() ;
		
		// Make globally available
		LARQ.setDefaultIndex(index);
		


    }


    /** Returns the current model used as a system repository.
     *
     * @return The model used as reprository
     */
    public Model getModel() {
        return this.model;
    }
     
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
    throws SecurityStoreException {
    	if ( this.getUsersNickNames().contains(sNickName) )
    		throw new SecurityStoreException("Nickname "+sNickName+" already exists!");
    	
    	Date       dateNow = new Date();
		String        sNow = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(dateNow);
		String [] saDefaultActionRole = { Action.BASE_ACTION_URL+"/Home",Action.BASE_ACTION_URL+"/Profile",Action.BASE_ACTION_URL+"/Credits"};
		
		// Encrypt the password
		String sEncryptedPassword = null;
		sEncryptedPassword = PasswordEncryptor.encrypt(sPassword);
		
		// Return the new user
		return new UserImpl(sNickName,sName,sEncryptedPassword,sNow,saDefaultActionRole,this,true);
    }
    
    /** Removes a user form the system.
     * 
     * @param usr The user to delete
     * @throws SecurityStoreException Admin user cannot be deleted from the system
     */
    public void removeUser ( User user ) throws SecurityStoreException {
    	 UserImpl usr = (UserImpl)user;
    	if ( usr.getNickName().equals(Store.getAdminUserNickName()) )
    		throw new SecurityStoreException("Admin user cannot be deleted from the system.");
    	else {
    		Model modUser = usr.getModel();
    		model.remove(modUser);
    		flush();    	
    	}
    }
    
    /** Return all the user nick names.
     * 
     * @return The user nick names
     */
    public Set<String> getUsersNickNames () {
   
    	Query query = QueryFactory.create(QUERY_ALL_NICKNAMES) ;
		QueryExecution exec = QueryExecutionFactory.create(query, model, null);
		ResultSet results =  exec.execSelect();
		
		Set<String> setNickNames = new HashSet<String>();
		while ( results.hasNext() ) {
			QuerySolution sol = results.nextSolution();
			// ?nickname
			setNickNames.add(sol.getLiteral("nickname").getLexicalForm());
		}
		
    	return setNickNames;
    }
    
    /** Gets the current users in the systems.
     * 
     * @return The set of curren users
     * @throws SecurityStoreException Could not create the new users
     */
    public Set<User> getUsers () throws SecurityStoreException {
    	
		Query query = QueryFactory.create(QUERY_ALL_USERS) ;
		QueryExecution exec = QueryExecutionFactory.create(query, model, null);
		ResultSet results =  exec.execSelect();
		
		Set<User> setUsers = new HashSet<User>();
		String sLastNickName = null;
		UserImpl usr = null;
		while ( results.hasNext() ) {
			QuerySolution sol = results.nextSolution();
			// ?nickname ?name ?password ?date ?action
			String sNickName = sol.getLiteral("nickname").getString();
			String sName = sol.getLiteral("name").getString();
			String sPassword = sol.getLiteral("password").getString();
			String sDate = sol.getLiteral("date").getString();
			
			Resource resAction = sol.getResource("action");
			
			if ( sLastNickName==null ) {
				sLastNickName=sNickName;
				usr = new UserImpl(sNickName,sName,sPassword,sDate,(resAction!=null)?resAction.toString():null,this,false);
				setUsers.add(usr);
			}
			else {
				if ( sNickName.equals(sLastNickName) )
					usr.grantActionRole(resAction.toString());
				else {
					sLastNickName=sNickName;
					usr = new UserImpl(sNickName,sName,sPassword,sDate,resAction.toString(),this,false);
					setUsers.add(usr);
				}
			}
			
		}
		
    	return setUsers;
    }
    
    /** Returns the user associated with this nickname.
     * 
     * @param sNickName The nickname
     * @return The user
     * @throws SecurityStoreException A problem arose while retrieving the user
     */
    public User getUser ( String sNickName ) throws SecurityStoreException {
    	
    	// Query the bindings
		QuerySolutionMap qsmBindings = new QuerySolutionMap();
		qsmBindings.add("nickname", ResourceFactory.createTypedLiteral(sNickName));

    	Query query = QueryFactory.create(QUERY_ALL_USERS) ;
		QueryExecution exec = QueryExecutionFactory.create(query, model, qsmBindings);
		ResultSet results =  exec.execSelect();
		
		Set<UserImpl> setUsers = new HashSet<UserImpl>();
		UserImpl usr = null;
		while ( results.hasNext() ) {
			QuerySolution sol = results.nextSolution();
			// ?nickname ?name ?password ?date ?action
			String sName = sol.getLiteral("name").getString();
			String sPassword = sol.getLiteral("password").getString();
			String sDate = sol.getLiteral("date").getString();
			
			Resource resAction = sol.getResource("action");
			
			if ( usr!=null )
				usr.grantActionRole(resAction.toString());
			else {
				usr = new UserImpl(sNickName,sName,sPassword,sDate,(resAction!=null)?resAction.toString():null,this,false);
				setUsers.add(usr);
			}
		}
		
		return usr;
	}
    

    /** Check if the given role has been granted to the given user.
     * 
     * @param sRole The role
     * @param sNickName The user nickname
     * @return True if role granted, false otherwhise
     */
    public boolean hasGrantedRoleToUser ( String sRole, String sNickName ) {
    	
    	boolean bRes = false;
    	try {
	    	User usr = getUser(sNickName);
	    	if ( usr!=null ) {
	    		bRes = usr.getGrantedActionRoles().contains(sRole);
	    	}
    	}
	    catch (SecurityStoreException e) {
			log.warning("Security store violation "+e);
		}
    	
    	return bRes;
    }

    
    /** Updates the information about a given user.
     * 
     * @param usr The new user information
     * @throws SecurityStoreException The user could not be updated
     */
    public void updateUser ( User usr ) throws SecurityStoreException {
    	UserImpl usri = (UserImpl)usr;
    	UserImpl usrOld = (UserImpl) getUser(usri.getNickName());
    	model.remove(usrOld.getModel());
    	model.add(usr.getModel());
    	flush();
	}

    /** Search for users that match the given query string.
     * 
     * @param sQuery The query
     * @return The set of users
     * @throws SecurityStoreException An exception ocurred during the execution
     */
    public Set<User> searchForUser ( String sQuery ) 
    throws SecurityStoreException {
		String QUERY_GET_ALL_SEARCHED_USERS = 
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"+
	    	"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"+
	    	"PREFIX meandre: <http://www.meandre.org/ontology/> \n"+
	    	"PREFIX meandreSec: <"+BASE_SECURITY_URL+"> \n"+
	    	"PREFIX meandreSecProp: <"+BASE_SECURITY_URL_PROPERTY+"> \n"+
	    	"PREFIX meandreSecRole: <"+Role.BASE_ROLE_URL+"> \n"+
		  	"PREFIX dc: <http://purl.org/dc/elements/1.1/> \n"+
		  	"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n"+
		  	"PREFIX pf: <http://jena.hpl.hp.com/ARQ/property#>\n"+
			"SELECT DISTINCT ?nickname ?name ?password ?date ?action "+
			"WHERE {" +
			"	{"+
			"    ?user rdf:type meandreSec:user ." +
			"    ?user meandreSecProp:nickname ?nickname . "+
			"    ?nickname pf:textMatch '"+sQuery+"' . "+
			"    ?user meandreSecProp:name ?name . "+
			"    ?user meandreSecProp:password ?password . "+
			"    ?user dc:date ?date . "+
			"    OPTIONAL {" +
			"		?user meandreSecRole:grant ?action  "+
			"    }" +
			"	}" +
			"	UNION" +
			"	{"+
			"    ?user rdf:type meandreSec:user ." +
			"    ?user meandreSecProp:nickname ?nickname . "+
			"    ?user meandreSecProp:name ?name . "+
			"    ?name pf:textMatch '"+sQuery+"' . "+
			"    ?user meandreSecProp:password ?password . "+
			"    ?user dc:date ?date . "+
			"    OPTIONAL {" +
			"		?user meandreSecRole:grant ?action  "+
			"    }" +
			"	}" +
			"}" +
			"ORDER BY ?nickname ?name ?password ?date ?action";
		
			
		Query query = QueryFactory.create(QUERY_GET_ALL_SEARCHED_USERS) ;
		QueryExecution exec = QueryExecutionFactory.create(query, model, null);
		ResultSet results =  exec.execSelect();
		
		Set<User> setUsers = new HashSet<User>();
		String sLastNickName = null;
		UserImpl usr = null;
		while ( results.hasNext() ) {
			QuerySolution sol = results.nextSolution();
			// ?nickname ?name ?password ?date ?action
			String sNickName = sol.getLiteral("nickname").getString();
			String sName = sol.getLiteral("name").getString();
			String sPassword = sol.getLiteral("password").getString();
			String sDate = sol.getLiteral("date").getString();
			
			Resource resAction = sol.getResource("action");
			
			if ( sLastNickName==null ) {
				sLastNickName=sNickName;
				usr = new UserImpl(sNickName,sName,sPassword,sDate,resAction.toString(),this,false);
				setUsers.add(usr);
			}
			else {
				if ( sNickName.equals(sLastNickName) )
					usr.grantActionRole(resAction.toString());
				else {
					sLastNickName=sNickName;
					usr = new UserImpl(sNickName,sName,sPassword,sDate,resAction.toString(),this,false);
					setUsers.add(usr);
				}
			}
			
		}
		
    	return setUsers;
    }

}

