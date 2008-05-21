/**
 * MeandreProxy creates and maintains a local cache of a remote 
 * Meandre Repository.
 */

package org.meandre.client;

import java.util.Set;
import java.util.logging.Logger;

import org.meandre.core.repository.LocationBean;
import org.meandre.core.repository.QueryableRepository;



public class MeandreProxy{

	/** The logger we'll write to */
	protected Logger log = null;

    /** the meandre client to handle the calls to the server */
    private MeandreClient client;

	/** The user name */
	private String sUserName;
	
	/** The password */
	private String sPassword;

	/** The base url of the remote server */
	private String sBaseURL;

	/** The credentials */
	private String sUPEncoding;
	
	/** Cached roles */
	private Set<String> mapRoles;

	/** Cached repository */
	private QueryableRepository qrCached;
	
	/** Is the proxy ready? */
	private boolean bIsReady;

	/** Did the last call succeed */
	private boolean bWasCallOK;


	/** Creates a Meandre Proxy and contacts the server to initialize
     * the cache.
	 * 
	 * @param sUser The user of the proxy
	 * @param sPasswd The password of the proxy
	 * @param sURL The Meandre server URL
	 */
	public MeandreProxy ( String sUser, String sPasswd, String sServerHost,
            int iServerPort ) {
		this.sUserName = sUser;
		this.sPassword = sPasswd;
		this.sBaseURL  = "http://" + sServerHost + ":" + 
                Integer.toString(iServerPort) + "/";

        this.client = new MeandreClient(sServerHost, iServerPort);
        client.setCredentials(sUser, sPasswd);
		
		String sUserPassword = sUserName + ":" + sPassword;
		this.sUPEncoding = new sun.misc.BASE64Encoder().encode (sUserPassword.getBytes());
		
		// Force a first authetication for role caching
		this.bIsReady = null!=getRoles();
		// Force the repository caching
		this.qrCached = getRepository();
	}

	/** Returns true if the proxy was successfully initialized; false otherwise.
	 *  
	 * @return True is successfully initialized
	 */
	public boolean isReady() {
		return bIsReady;
	}
	
	/** Returns true if the last call was completed successfully.
	 * 
	 * @return True if everything when well. False otherwise
	 */
	public boolean getCallOk () {
		return bWasCallOK;
	}
	
	/** Gets the user name.
	 * 
	 * @return The user name
	 */
	public String getName () {
		return sUserName;
	}    

	/** Flushes the cached roles.
	 * 
	 */
	public void flushRoles () {
		mapRoles = null;
	}

	
	
	/** Flushes the cached repository.
	 * 
	 */
	public void flushRepository () {
		qrCached = null;
	}
	
	/** Return the roles for the user of this proxy.
	 * 
	 * @return The set of granted role for the proxy user
	 */
	public Set<String> getRoles() {
		if ( mapRoles==null ) {
            try{
                Set<String> roles = this.client.retrieveUserRoles();
            }catch(TransmissionException e){
                bWasCallOK = false;
                log("Couldn't retrieve roles: " + e.toString());
            }           
        }
		return mapRoles;
	}
	
	/** Gets the current cached repository.
	 * 
	 * @return The cached queryable repository 
	 */
	public QueryableRepository getRepository () {
		if ( this.qrCached==null ) {
            try{
                this.qrCached = this.client.retrieveRepository();
            }catch(TransmissionException e){
                bWasCallOK = false;
                log("Couldn't retrieve Repository: " +e.toString());
            }

        }
		return this.qrCached;
	}



	/** Retrieves the public repository from the server (no cacheing).
	 * 
	 * @return The public queryable repository 
	 */
	public QueryableRepository getPublicRepository () {
		// The public repository
        try{
            return this.client.retrieveRepository();
        }catch(TransmissionException e){
            bWasCallOK = false;
            log("Couldn't retrieve Public Repository: " + e.toString());
        }
        return null;
	}

	
	/** Forces the repository to be recached before returning it.
	 * 
	 * @return The recached repository
	 */
	public QueryableRepository getRepositoryFlush () {
		this.qrCached = null;
		return getRepository();
	}

	/** Return the list of locations for the user of this proxy.
	 * 
	 * @return The array of location for this user
	 */

	public LocationBean[] getLocations() {
		bWasCallOK = true;
		Set<LocationBean> loca = null;
        try{
            loca = this.client.retrieveLocations();
        }catch(TransmissionException e){
            bWasCallOK = false;
            log("Couldn't retrieve locations: " + e.toString());
        }
        LocationBean[] locArray = new LocationBean[loca.size()];
        return loca.toArray(locArray);
	}

	/** regenerates the  remote user repository and updates the local cache.
	 * 
	 * @return The result of the process. true if succesfull
	 */
	public boolean getRegenerate () {
		boolean localWasCallOK = true;
		
        try{
            localWasCallOK = this.client.regenerate();
        }catch(TransmissionException e){
    		localWasCallOK = false;		
            log("Proxy couldn't regenerate repository:") ;
        }
		getRepositoryFlush();

        //set was call ok to true only if the local regenerate and the
        //repository flush both succeeded.
		bWasCallOK = localWasCallOK && bWasCallOK;
		return bWasCallOK;
	}

	/** Gets the result of attempting to add a new location to the user repository.
	 * 
	 * @param sLocation The URL location
	 * @param sDescription The location description
	 * @return The result of the process. True if it was succesful
	 */
	public boolean getAddLocation (String sLocation, String sDescription ) {
        bWasCallOK = true;
        try{
            if ( mapRoles!=null ) {
                bWasCallOK = this.client.addLocation(sLocation, sDescription);
            }
        }catch(Exception e){
            bWasCallOK = false;
            log("Proxy couldn't add location:" + e.toString());
        }
        return bWasCallOK;
    }

	
	/** Gets the result of attempting to remove a location from the user repository.
	 * 
	 * @param sLocation The URL location
	 * @return true if the removal was successful
	 */
	public boolean getRemoveLocation (String sLocation ) {
        bWasCallOK = true;
        try{
            if ( mapRoles!=null ) {
                bWasCallOK = this.client.removeLocation(sLocation);
            }
        }catch(Exception e){
            bWasCallOK = false;
            log("Proxy couldn't remove location:" + e.toString());
        }
        return bWasCallOK;
	}

	/** publishes a component or flow (identified by it's uri) at the remote
     * server.
	 * 
	 * @param sURI The resource URI to publish
	 * @return The result of the process. Returns true if successful
     **/
	public boolean getPublish (String sURI ) {
        if ( mapRoles!=null ) {
		    try {
			    bWasCallOK = this.client.publish(sURI);
			} catch (TransmissionException e) {
                bWasCallOK = false;
			    log.warning("Proxy couldn't perform publish: " + e);
			}
		}
		return bWasCallOK;
	}

	/** unpublishes a component or flow (identified by it's uri) at the remote
     * server.
     *
     * returns true no matter what as long as the server received and understood
     * the request.
	 * 
	 * @param sURI The resource URI to publish
	 * @return The result of the process. Returns true if successful
     **/
	public boolean getUnpublish (String sURI ) {
        if (mapRoles!=null) {
		    try {
			    bWasCallOK = this.client.unpublish(sURI);
			} catch (TransmissionException e) {
                bWasCallOK = false;
			    log.warning("Proxy couldn't perform unpublish: " + e);
			}
		}
		return bWasCallOK;
	}



	/** Gets the result of attempting to remove a component or flow, identified
     * by it's, URI from the user repository.
	 * 
	 * @param sURI The resource URI to remove
	 * @return  true if successful
	 */
	public boolean getRemove (String sURI ) {
		if ( mapRoles!=null ) {
			try {
				bWasCallOK = this.client.removeResource(sURI);
			} catch (TransmissionException e) {
                bWasCallOK = false;
			    log.warning("Proxy couldn't perform remove: " + e);
			}
            if(bWasCallOK){
				flushRepository();
            }
		}
		return bWasCallOK;
	}
	
	/** Return the list of running flows of this proxy.
	 * 
     *
	 * @return The set of running flows. will return an empty set if
     * the transmission failed, so check wasCallOK()
	 */
	/*public Set<RunningFlow> getRunningFlows() {
        Set<RunningFlow> flowBeans = new HashSet<RunningFlow>();

		if ( mapRoles!=null ) {
            try{
                Map<URL, URL> flowMap= this.client.retrieveRunningFlows();

                Iterator<URL> keyIter = flowMap.keySet().iterator();
                while(keyIter.hasNext()){
                    URL key = keyIter.next();
                    URL val = flowMap.get(key);
                    RunningFlow flow = new RunningFlow(
                            val.toString(), key.toString());
                    flowBeans.put(flow);
                }
            }catch(TransmissionException e){
                bWasCallOK = false;
                log("Proxy couldn't retrieve running flows: " + e.toString());
            }
		}
		return flowBeans;
	}*/





	
	/** Runs a flow and streams the output.
	 * 
	 * @param sURI The flow to execute
	 * @param sFormat The format of the output
	 * @param jw The writer to use
	 */
	/*public void runFlowInteractively ( String sURI, String sFormat, JspWriter jw ) {
		String sRequest = sBaseURL+"services/execute/flow."+sFormat+"?statistics=true&uri="+sURI;
		executeStreamableGetRequest(sRequest,jw);
	}*/

	/**
	 * handle generic logging messages for this proxy's default logging level
	 * @param msg
	 */
	private void log(String msg){
	    System.out.println(msg);
	}

	/** Does an authenticated get request against the provided URL and stream back
	 * the contents
	 * 
	 * @param sURL The URL to request
	 * @param jw The outpt writter
	 */
	/*private void executeStreamableGetRequest(String sURL, JspWriter jw) {
		try {
			// Create the URL
			URL url = new URL(sURL);
			
			// Create and authenticated connection
			URLConnection uc = url.openConnection();
			uc.setRequestProperty ("Authorization", "Basic " + sUPEncoding);
			
			// Pull the stuff out of the Meandre server
			InputStream is = (InputStream)uc.getInputStream();
			int iTmp;
			while ( (iTmp=is.read())!=-1 )
				jw.write(iTmp);
			
			is.close();
		}
		catch ( IOException e ) {
			log.warning(e.toString());
		}
	}*/
}
