package org.meandre.core.security;

import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.meandre.core.security.User;



/** A description of a Meandre system user. 
 * 
 * @author Xavier Llor&agrave;
 * @author Peter Groves
 *
 */
public class User {

	/** The base url for the users. */
	public static final String BASE_USER_URL = 
            SecurityManager.BASE_SECURITY_URL +  "user";
	
	/** The base url for the user properties and others. */
	public static final String BASE_USER_PROPERTY_URL = 
            SecurityManager.BASE_SECURITY_URL_PROPERTY + "user";
	
	/** constant for representing the nickName/userName field. The JSON
	 * methods use this, for example. */
	private static final String FIELD_USER_NAME = "user_name";
	
    /** constant for representing the fullName/name field. The JSON
     * methods use this, for example. */	
	private static final String FIELD_FULL_NAME = "full_name";
	
	/** field for representing a list of users. */
	private static final String FIELD_SET = "user_set";
	
	/** The user nickname */
	private String sNickName = null;
	
	/** The user name */
	private String sName = null;
	
	/** Creates a new user.
	 * 
	 * @param sNickName The nick name
	 * @param sName The user

	 */
	public User(String sNickName, String sName){
		
		this.sNickName = sNickName;
		this.sName = sName;
	}
	
	/** Returns a string with all the information contained in the user.
	 * 
	 * @return The user information
	 */
	public String toString () {
		StringBuffer sb = new StringBuffer();
		sb.append("Nickname = "+sNickName+"\n");
		sb.append("Name     = "+sName+"\n");
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
	
	public JSONObject toJSON(){
        JSONObject jo = new JSONObject();
        try{
            jo.put(FIELD_USER_NAME, this.getNickName());
            jo.put(FIELD_FULL_NAME, this.getName());
        }catch(JSONException je){
            //This supposedly won't ever happen
            je.printStackTrace();
        }
        return jo;
	}
	
	public static User fromJSON(JSONObject jo) throws JSONException{
	    User usr = null;
		try{
			String nickName = jo.getString(FIELD_USER_NAME);
			String fullName = jo.getString(FIELD_FULL_NAME);

			usr = new User(nickName, fullName);
		}catch(JSONException je){
			throw new JSONException("The input JSONObject does not" +
					" properly represent a User object.\n" + je.toString() +
					"\n in json object: \n" + jo.toString());
		}
		return usr;
	}
	
	public static JSONObject setToJSON(Set<User> users) {
	    JSONObject jo = null;
	    try{
	        JSONArray ja = new JSONArray();
	        for(User usr : users){
	            ja.put(usr.toJSON());
	        }
	        jo = new JSONObject();
	        jo.put(FIELD_SET, ja);
	    }catch(JSONException je){
	        //This supposedly won't ever happen
	        je.printStackTrace();
	    }
        return jo;
	    
	}
	
	public static Set<User> setFromJSON(JSONObject jo) throws JSONException{
		Set<User> users = new HashSet<User>(15);
		try{
			JSONArray ja = jo.getJSONArray(FIELD_SET);
			for(int i = 0; i < ja.length(); i++){
				JSONObject joUser = ja.getJSONObject(i);
				User usr = fromJSON(joUser);
				users.add(usr);
			}

		}catch(JSONException je){
			throw new JSONException("The input json object does not" +
					" properly represent a Set of Users. \n" + je.toString());
		}
		return users;
	}

    public int hashCode(){
        //TODO: should be hash of the url
        return sName.hashCode() + sNickName.hashCode();
    }
    
    /** equals function useable by java Collections. Two users are
     * equal if they have the same name and nickname.
     */
    public boolean equals(Object obj){
        User otherUser = null;
        try{
            otherUser = (User)obj;
        }catch(ClassCastException e){
            return false;
        }
        boolean isEqual = true;
        isEqual &= otherUser.getNickName().equals(this.getNickName());
        isEqual &= otherUser.getName().equals(this.getName());
        return isEqual;
    }

}
