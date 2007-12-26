package org.meandre.core.store.security.local;

import java.util.HashSet;
import java.util.Set;

import org.meandre.core.store.security.Action;
import org.meandre.core.store.security.PasswordEncryptor;
import org.meandre.core.store.security.Role;
import org.meandre.core.store.security.SecurityStoreException;
import org.meandre.core.store.security.User;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/** This object contains a description of a system user.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class UserImpl implements User {

	/** The base url for the user properties and others. */
	public static final String BASE_USER_URL = SecurityStoreImpl.BASE_SECURITY_URL+"user";
	
	/** The base url for the user properties and others. */
	public static final String BASE_USER_PROPERTY_URL = SecurityStoreImpl.BASE_SECURITY_URL_PROPERTY+"user";
	
	/** The user nickname */
	private String sNickName = null;
	
	/** The user name */
	private String sName = null;
	
	/** The creation date */
	private String sDate = null;
	
	/** The user password */
	private String sPassword = null;
	
	/** The set of role actions assigned to the user */
	private Set<String> setActionRoles = null;

	/** The parent security store */
	private SecurityStoreImpl scParent = null;
	
	/** Creates a new user.
	 * 
	 * @param sNickName The nick name
	 * @param sName The user
	 * @param sPassword The password to set
	 * @param sSingleRole The default action roles
	 * @param ss The parent security store
	 * @param bAdd Should be added to the store
	 */
	public UserImpl(String sNickName, String sName, String sPassword, String sDate,
			String sSingleRoleAction, SecurityStoreImpl ss, boolean bAdd) {
		
		this.sNickName = sNickName;
		this.sName = sName;
		this.sPassword = sPassword;
		this.sDate = sDate;
		this.setActionRoles = new HashSet<String>();
		this.scParent = ss;
		
		if ( sSingleRoleAction!=null)
			this.setActionRoles.add(sSingleRoleAction);
		
		// Add user to the store
		if ( bAdd ) {
			scParent.getModel().add(this.getModel());
			scParent.flush();
		}
	}
	
	/** Creates a new user.
	 * 
	 * @param sNickName The nick name
	 * @param sName The user
	 * @param sPassword The password to set
	 * @param saSingleRole The default set of action roles
	 * @param ss The parent security store
	 * @param bAdd Should be added to the store
	 */
	public UserImpl(String sNickName, String sName, String sPassword, String sDate,
			String[] saSingleRoleAction, SecurityStoreImpl ss, boolean bAdd ) {
		
		this.sNickName = sNickName;
		this.sName = sName;
		this.sPassword = sPassword;
		this.sDate = sDate;
		this.setActionRoles = new HashSet<String>();
		this.scParent = ss;
		
		for ( String sURL:saSingleRoleAction )
			this.setActionRoles.add(sURL);
		
		// Add user to the store
		if ( bAdd ) {
			scParent.getModel().add(this.getModel());
			scParent.flush();
		}
	}
	
	/** Returns a string with all the information contained in the user.
	 * 
	 * @return The user information
	 */
	public String toString () {
		StringBuffer sb = new StringBuffer();
		
		sb.append("Nickname = "+sNickName+"\n");
		sb.append("Name     = "+sName+"\n");
		sb.append("Password = "+sPassword+"\n");
		sb.append("Actions  = "+setActionRoles.toString());
		
		return sb.toString();
	}

	/** Returns the nickname of the user.
	 * 
	 * @return The nick name
	 */
	public String getNickName () {
		return sNickName;
	}
	
	/** Returns the name of the user.
	 * 
	 * @return The name
	 */
	public String getName() {
		return sName;
	}
	
	/** Sets the name of the user.
	 * 
	 * @param sName The name
	 */
	public void setName( String sName ) {
		this.sName = sName;
	}
	
	/** Returns the encrypted password of a user.
	 * 
	 * @return The encrypted password
	 */
	public String getPassword() {
		return sPassword;
	}
	

	/** Sets the encrypted password of a user.
	 * 
	 * @param sPassword The encrypted password
	 */
	public void setPassword(String sPassword) {
		this.sPassword = PasswordEncryptor.encrypt(sPassword);
	}

	
	/** Return the set of action roles granted for the user.
	 * 
	 * @return The set of allowed roles
	 */
	public Set<String> getGrantedActionRoles () {
		return setActionRoles;
	}
	
	/** Grants a new action role to the user.
	 * 
	 * @param sActionRole The action role to grant
	 * @throws SecurityStoreException The action role is not a valid one
	 */
	public void grantActionRole ( String sActionRole ) 
	throws SecurityStoreException {
		Model model = scParent.getModel();
		if ( Action.setBasicActionURLs.contains(sActionRole) ) {
			setActionRoles.add(sActionRole);
			// Adding it to the store
			Resource resUser = model.createResource(UserImpl.BASE_USER_PROPERTY_URL+"/"+sNickName);	
			resUser.addProperty(ResourceFactory.createProperty(Role.ROLE_GRANT_PROPERTY_URL), ResourceFactory.createResource(sActionRole));
			
		}
		else { 
			throw new SecurityStoreException("Action role "+sActionRole+" is not a valid one");
		}
	}

	/** Grants a set of action roles to the user.
	 * 
	 * @param setActionRole The action role set to grant
	 * @throws SecurityStoreException The action role is not a valid one
	 */
	public void grantActionRoles ( Set<String> setActionRole ) 
	throws SecurityStoreException {
		Model model = scParent.getModel();
		if ( Action.setBasicActionURLs.containsAll(setActionRole) ) {
			setActionRoles.addAll(setActionRole);
			// Adding it to the store
			for ( String sActionRole:setActionRole ) {
				Resource resUser = model.createResource(UserImpl.BASE_USER_PROPERTY_URL+"/"+sNickName);	
				resUser.addProperty(ResourceFactory.createProperty(Role.ROLE_GRANT_PROPERTY_URL), ResourceFactory.createResource(sActionRole));
			}
			
		}
		else { 
			throw new SecurityStoreException("Action role "+setActionRole+" is not a valid one");
		}
	}

	/** Dennys the given action role for this user.
	 * 
	 * @param sActionRole The action role to denny
	 * @throws SecurityStoreException The action role is not a valid one
	 */
	public void dennyActionRole ( String sActionRole ) 
	throws SecurityStoreException {
		if ( Action.setBasicActionURLs.contains(sActionRole) ) {
			setActionRoles.remove(sActionRole);
			// Removing it to the store
			Model modTmp = ModelFactory.createDefaultModel();
			Resource resUser = modTmp.createResource(UserImpl.BASE_USER_PROPERTY_URL+"/"+sNickName);	
			resUser.addProperty(ResourceFactory.createProperty(Role.ROLE_GRANT_PROPERTY_URL), ResourceFactory.createResource(sActionRole));
			// Remove
			scParent.getModel().remove(modTmp);
				
		}
		else { 
			throw new SecurityStoreException("Action role "+sActionRole+" is not a valid one");
		}
	}
	
	/** Dennys all the action role for this user.
	 * 
	 * @throws SecurityStoreException The action role is not a valid one
	 */
	public void dennyAllActionRoles () 
	throws SecurityStoreException {
		setActionRoles.clear();
		// Removing it to the store
		Model modTmp = scParent.getUser(sNickName).getModel();
		// Remove
		scParent.getModel().remove(modTmp);
		scParent.getModel().add(getModel());
		// Flush the store
		scParent.flush();	
	}
	
	/** Dennys the given set of action role for this user.
	 * 
	 * @param setActionRole The action role set to deny
	 * @throws SecurityStoreException The action role is not a valid one
	 */
	public void dennyActionRole ( Set<String> setActionRole ) 
	throws SecurityStoreException {
		if ( Action.setBasicActionURLs.contains(setActionRole) ) {
			setActionRoles.removeAll(setActionRole);
			// Removing it to the store
			Model modTmp = ModelFactory.createDefaultModel();
			for ( String sActionRole:setActionRole ) {
				Resource resUser = modTmp.createResource(UserImpl.BASE_USER_PROPERTY_URL+"/"+sNickName);	
				resUser.addProperty(ResourceFactory.createProperty(Role.ROLE_GRANT_PROPERTY_URL), ResourceFactory.createResource(sActionRole));
			}
			// Remove
			scParent.getModel().remove(modTmp);
			
		}
		else { 
			throw new SecurityStoreException("Action role "+setActionRole+" is not a valid one");
		}
	}
	

	/** Checks if the given action role has been granted for this user.
	 * 
	 * @param sActionRole The action role to denny
	 * @throws SecurityStoreException The action role is not a valid one
	 */
	public boolean hasActionRoleGranted ( String sActionRole ) 
	throws SecurityStoreException {
		boolean bRes = false;
		
		if ( Action.setBasicActionURLs.contains(sActionRole) ) {
			bRes = setActionRoles.contains(sActionRole);
		}
		else { 
			throw new SecurityStoreException("Action role "+sActionRole+" is not a valid one");
		}
		return bRes;
	}

	/** Returns a model containing all the information about this user.
	 * 
	 * @return The model
	 */
	public Model getModel () {
		Model model = ModelFactory.createDefaultModel();
		model.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
        model.setNsPrefix("meandreSec",SecurityStoreImpl.BASE_SECURITY_URL);
        model.setNsPrefix("meandreSecProp",SecurityStoreImpl.BASE_SECURITY_URL_PROPERTY);
        model.setNsPrefix("meandreSecRole",Role.BASE_ROLE_URL);
        model.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        model.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        model.setNsPrefix("dc", "http://purl.org/dc/elements/1.1/");

        Resource resRoot = model.createResource(SecurityStoreImpl.BASE_SECURITY_URL);
		Resource resUser = model.createResource(UserImpl.BASE_USER_PROPERTY_URL+"/"+sNickName);
		
		resUser.addProperty(ResourceFactory.createProperty(SecurityStoreImpl.BASE_SECURITY_URL_PROPERTY+"name"), ResourceFactory.createTypedLiteral(sName))
			   .addProperty(ResourceFactory.createProperty(SecurityStoreImpl.BASE_SECURITY_URL_PROPERTY+"nickname"), ResourceFactory.createTypedLiteral(sNickName))
			   .addProperty(ResourceFactory.createProperty(SecurityStoreImpl.BASE_SECURITY_URL_PROPERTY+"password"), ResourceFactory.createTypedLiteral(sPassword))
			   .addProperty(ResourceFactory.createProperty("http://purl.org/dc/elements/1.1/date"), ResourceFactory.createTypedLiteral(sDate,XSDDatatype.XSDdateTime))
			   .addProperty(ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), model.createResource(UserImpl.BASE_USER_URL));
		
		for ( String sActionRole:setActionRoles ) {
			resUser.addProperty(ResourceFactory.createProperty(Role.ROLE_GRANT_PROPERTY_URL), ResourceFactory.createResource(sActionRole));
		}	
		
		resRoot.addProperty(ResourceFactory.createProperty(BASE_USER_PROPERTY_URL), resUser);
		
		return model;
	}
}
