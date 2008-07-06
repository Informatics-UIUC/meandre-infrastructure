package org.meandre.core.security;

import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;


/** A Meandre role is used to associate a User with some permission
 * or privilege.
 * 
 * @author Peter Groves
 *
 */
public class Role {


	/** This naming convention is deprecated and not particularly useful. */
	public static final String BASE_ROLE_URL = 
        SecurityManager.BASE_SECURITY_URL_PROPERTY + "role/";

	/** This naming convention is deprecated and not particularly useful. */
   /** public static final String BASE_WEB_ROLE_URL = 
        SecurityManager.BASE_SECURITY_URL_PROPERTY + "role/web";*/	
	
    /** The base URL for granting roles. This is used to indicate a role
     * is granted in an rdf model.
     **/
    public static final String ROLE_GRANT_PROPERTY_URL = 
            BASE_ROLE_URL + "grant";
    
    /** used for json keys, etc., when a role is an attribute in a data set */
	private static final String FIELD_ROLE = "role";
    
	/** used for json keys, etc, when a set of roles is an attribute in
	 * a data set*/
	private static final String FIELD_ROLE_SET = "role_set";
	
    /** used for xml keys, etc, when a url identifier is a separate 
     * attribute in a data set*/
    private static final String FIELD_MEANDRE_URL = "meandre_url";
    
    /** the short name of a Role is the unique part of it's identifying URL.
     */
    String _shortName;

    /** the complete identifier for this role.  */
    String _longNameUrl;
    

    //////////////////////////
    //Standard Supported Roles
    //////////////////////////

    public static Role HOME = new Role("Home");
    public static Role REPOSITORY = new Role("Repository");
    public static Role COMPONENTS = new Role("Components");
    public static Role FLOWS = new Role("Flows");
    public static Role WORKBENCH = new Role("Workbench");
    public static Role PUBLISH = new Role("Publish");
    public static Role EXECUTION = new Role("Execution");
    public static Role CREDITS = new Role("Credits");
    public static Role PROFILE = new Role("Profile");
    public static Role ADMIN = new Role("Admin");

    /**
     * construct a role in the default domain (default url prefix) 
     * with the given name. 
     */
    public Role(String roleName){
        _shortName = roleName;
        _longNameUrl = BASE_ROLE_URL + roleName;
    }
    
    public Role(Resource res){
        _longNameUrl = res.getURI();
        
        //finding the short name
        //split up the url
        String[] urlChunks = _longNameUrl.split("/");
        _shortName = null;
        int lastChunkIndex = urlChunks.length - 1;
        if(urlChunks[lastChunkIndex].equals("")){
            //we would get here if the url ended in "/", so we want the second
            //to last chunk
            _shortName = urlChunks[lastChunkIndex - 1];
        }else{
            _shortName = urlChunks[lastChunkIndex];
        }
       
    }

    /** get the set of available standard roles for this installation.
     */
    public static Set<Role> getStandardRoles(){
        Set<Role> allRoles = new HashSet<Role>(15);
        allRoles.add(HOME);
        allRoles.add(REPOSITORY);
        allRoles.add(COMPONENTS);
        allRoles.add(FLOWS);
        allRoles.add(WORKBENCH);
        allRoles.add(PUBLISH);
        allRoles.add(EXECUTION);
        allRoles.add(CREDITS);
        allRoles.add(PROFILE);
        allRoles.add(ADMIN);

        return allRoles;
    }

    /** a hash code for use by HashMaps and other Collections. Based
     * on the long url name.
     */
    public int hashCode(){
        return _longNameUrl.hashCode();
    }

    /** equals function useable by java Collections. Two roles are
     * equal if they have the same long url name.
     */
	public boolean equals(Object obj){
        Role otherRole = null;
        try{
            otherRole = (Role)obj;
        }catch(ClassCastException e){
            return false;
        }
        boolean isEqual = true;
        isEqual &= otherRole.getUrl().equals(this.getUrl());
        return isEqual;
    }

    /** the long name url of a role that should distinguish it universally. */
    public String getUrl(){
        return _longNameUrl;
    }


    /** the short base name of a role suitable for display. short names are
     * unique for any meandre installation instance. (which also means
     * two roles from the same security store with the same short name 
     * are considered to be the same role.*/
    public String getShortName(){
        return _shortName;
    }

    /**
     * returns true if this role is one of the standard static roles.
     * same as Role.getStandardRoles().contains(thisRole)
     */
    public boolean isStandard(){
        return (getStandardRoles().contains(this));
    }

    /** 
     * human readable string representation of the role name.
     */
    public String toString(){
        return getShortName();
    }
    
    public static Role fromResource(Resource res){
        return new Role(res);
    }
	public static Role fromUrl(String sUrl){
		return new Role(ResourceFactory.createResource(sUrl));
	}

    public JSONObject toJSON(){
        JSONObject jo = new JSONObject();
        try{
            jo.put(FIELD_ROLE, this.getUrl());
        }catch(JSONException je){
            //This supposedly won't ever happen
            je.printStackTrace();
        }
        return jo;        
    }
    
    public static Role fromJSON(JSONObject jo) throws JSONException{
		Role role = null;
		try{
			String roleUrl = jo.getString(FIELD_ROLE);
			role = fromUrl(roleUrl);
		}catch(JSONException je){
			throw new JSONException("The input JSONObject does not" +
					" properly represent a User object.\n" + je.toString() +
					"\n in json object: \n" + jo.toString());
		}
		return role;
    }
    
    public static JSONObject setToJSON(Set<Role> roles){
		JSONObject jo = null;
	    try{
	        JSONArray ja = new JSONArray();
	        for(Role rl : roles){
	            ja.put(rl.toJSON());
	        }
	        jo = new JSONObject();
	        jo.put(FIELD_ROLE_SET, ja);
	    }catch(JSONException je){
	        //This supposedly won't ever happen
	        je.printStackTrace();
	    }
        return jo;
    }
    
    public static Set<Role> setFromJSON(JSONObject jo) throws JSONException{
		Set<Role> roles = new HashSet<Role>(15);
		try{
			JSONArray ja = jo.getJSONArray(FIELD_ROLE_SET);
			for(int i = 0; i < ja.length(); i++){
				JSONObject joRole = ja.getJSONObject(i);
				Role rl = fromJSON(joRole);
				roles.add(rl);
			}

		}catch(JSONException je){
			throw new JSONException("The input json object does not" +
					" properly represent a Set of Roles. \n" + je.toString());
		}
		return roles;
    }
    
    /**
     * converts a set of roles into an xml form. this does not do a 
     * one to one mapping from the result of setToJSON, but does construct
     * a new json representation that can be transformed into valid xml.
     *  
     * @param roles
     * @return a string representation of 'roles' as xml
     */
    public static String setToXML(Set<Role> roles){
        String sXml = null;
        try{
            JSONArray ja = new JSONArray();
            for(Role rl : roles){
                JSONObject jRole = new JSONObject();
                jRole.put(FIELD_MEANDRE_URL, rl.getUrl());
                ja.put(jRole);
            }
            JSONObject jo = new JSONObject();
            jo.put(FIELD_ROLE, ja);
            sXml = XML.toString(jo, FIELD_ROLE_SET);
            
        }catch(JSONException je){
            //This supposedly won't ever happen
            je.printStackTrace();
        }
        return sXml;     
    }
    
    public static String setToText(Set<Role> roles){
        StringBuffer sb = new StringBuffer();
        for(Role rl : roles){
            sb.append(rl.getUrl());
            sb.append("\n");
        }
        return sb.toString();
    }
    
    
}
