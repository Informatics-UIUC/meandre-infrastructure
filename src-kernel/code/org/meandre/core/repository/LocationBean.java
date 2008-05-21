package org.meandre.core.repository;

/** This bean wraps the url and description of a peer location.
 * 
 * @author Xavier Llor&agrave;
 * updates: Peter Groves
 *
 *
 * TODO: This isn't a good package for this class to live in.
 *
 */
public class LocationBean {
	
	/** The location URL */
	private String sLocation;
	
	/** The location description */
	private String sDescription;

	/** Creates a bean and sets the location and description.
	 * 
	 * @param sLocation The location URL
	 * @param sDescription The description
	 */
	public LocationBean ( String sLocation, String sDescription ) {
		setLocation(sLocation);
		setDescription(sDescription);
	}
	
	/** Sets the URL location
	 * 
	 * @param sLocation the sLocation to set
	 */
	public void setLocation(String sLocation) {
		this.sLocation = sLocation;
	}

	/** Gets the URL location
	 * 
	 * @return the sLocation
	 */
	public String getLocation() {
		return sLocation;
	}

	/** Sets the location description
	 * 
	 * @param description the sDescription to set
	 */
	public void setDescription(String sDescription) {
		this.sDescription = sDescription;
	}

	/** Gets the location description
	 * 
	 * @return the description
	 */
	public String getDescription() {
		return sDescription;
	}
	
	/**
	 * tests if this bean has equivalent values to another one.
	 *
	 * @param other
	 * @return
	 */

    public boolean equals(Object otherLocation){
        boolean isEqual = true;
        LocationBean otherBean = null;
        try{
            otherBean = (LocationBean)otherLocation;
        }catch(ClassCastException e){
            return false;
        }
        
        isEqual = isEqual && (this.getLocation().equals(otherBean.getLocation()));
        isEqual = isEqual && (this.getDescription().equals(otherBean.getDescription()));
        return isEqual;
    }
    	
	/**
	 * returns a pretty print version of this location's url and description
	 */
	public String toString(){
	    StringBuffer sb = new StringBuffer();
	    sb.append("LocationURL=\'");
	    sb.append(getLocation());
	    sb.append("\' Description=\'");
	    sb.append(getDescription());
	    sb.append("\'");
	    return sb.toString();
	    		
	}
	
	/** a hashcode derived from the internal url and description. used by hashmaps. */
    public int hashCode(){
        int hc = this.getLocation().hashCode();
        hc += this.getDescription().hashCode();
        return hc;
    }

}
