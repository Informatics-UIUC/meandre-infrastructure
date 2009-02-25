package org.meandre.client;


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;

import java.util.logging.Logger;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.json.JSONTokener;
import org.meandre.core.logger.MeandreFormatter;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * The basic framework for making calls to the webservices of a running
 * Meandre Infrastructure instance. MeandreBaseClient takes care of 
 * authorization and basic http communication. Also provides convenience
 * methods for common types of meandre requests, such as GET request that 
 * returns data in JSON format.
 *
 * @author Peter Groves
 */
public class MeandreBaseClient{

	/** authorization for accessing a meandre server */
	private  UsernamePasswordCredentials _credentials = null;

	/**base url of the server to interact with. */
    private String _serverHost = null;
	
    /**port number the server is listening on.*/
    private int _port;

	/** logger for request/response contents*/
	private Logger _log;
	
	/**
	 * initialize a client that talks to a Meandre server located at the input
	 * url and port. The default global logger will be used until setLogger()
	 * is called.
     *
     * @param serverHost just the hostname, e.g. "localhost", 
     *      NOT "http://localhost"
     * @param serversPort the port on the serverhost that the server is listening
	 */
	public MeandreBaseClient(String serverHost, int serversPort){
		setServerAddress(serverHost, serversPort);
		setLogger(ClientLoggerFactory.getClientLogger());
	}

	/**
	 * initialize a client that talks to a Meandre server located at the input
	 * url and port.
     *
     * @param serverHost just the hostname, e.g. "localhost", 
     *      NOT "http://localhost"
     * @param serversPort the port on the serverhost that the server is listening
	 */
	public MeandreBaseClient(String serverHost, int serversPort, Logger log){
		setServerAddress(serverHost, serversPort);
		setLogger(log);
	}


    /**
     * set the username and password that this client will use when sending
     * requests to the server. must be set before any server calls requiring
     * authentication are made.
     */
    public void setCredentials(String userName, String password){
		_credentials = new UsernamePasswordCredentials(userName, password);
    }

    /**
     * Change the server and port that this client will communicate with.
     * Note that this does not reset the credentials - the same credentials
     * used on the old serverHost will be used on the new server until 
     * you call setCredentials.
     */
    public void setServerAddress(String serverHost, int serversPort){
        _serverHost = serverHost;
        _port = serversPort;
    }

    public int getPort(){
        return _port;
    }

    public String getServerHost(){
        return _serverHost;
    }

	public Logger getLogger(){
		return _log;
	}

	public void setLogger(Logger log){
		_log = log;
	}

	/**
	 * synchronizes the open http connection with this MeandreClient's
	 * internal variables (server, port, credentials).
	 */
	/*private void updateConnection(){
	    HostConfiguration config = new HostConfiguration();
        config.setHost(_serverHost, _port);
        _httpClient.setHostConfiguration(config);
		if(_credentials != null){
            AuthScope scope = new AuthScope(null, _port, null);
			_httpClient.getState().setCredentials(scope, _credentials);
		}
	}*/


    /**
     * returns a new http client with this meandre client's credentials.
     * a new one is returned every time b/c executing http methods on
     * an httpclient blocks, while MeandreBaseClient is expected to 
     * allow multiple requests to be dispatched at asynchronously.
     */
    private HttpClient getHttpClient(){
        HttpClient httpClient = new HttpClient();
	    HostConfiguration config = new HostConfiguration();
        config.setHost(_serverHost, _port);
        httpClient.setHostConfiguration(config);

        //if credentials are not set, we will assume we don't need authorization
		if(_credentials != null){
            AuthScope scope = new AuthScope(null, _port, null);
			httpClient.getState().setCredentials(scope, _credentials);
        }
		
        return httpClient;
    }


    /** Does an authenticated GET request against the server using the
	 * input url suffix and params. 
     * 
     * <p>url visited will be:
     * <p>http://<meandre-host>:<port>/<sRestCommand><queryParams(?n1=v1?n2=v2...)>
	 * 
	 * @param sRestCommand The url suffix
	 * @param queryParams the http query params to append to the url. Null is
	 * an acceptable value for this set if no params are needed.
	 * 
	 * @return The raw content bytes of the server's response
	 */
	public byte[] executeGetRequestBytes(String sRestCommand, 
			Set<NameValuePair> queryParams)
	        throws TransmissionException{

		GetMethod get = new GetMethod();
		get.setPath("/" + sRestCommand);
		get.setDoAuthentication(true);
		if(queryParams != null){
		    NameValuePair[] nvp = new NameValuePair[queryParams.size()]; 
		    nvp = queryParams.toArray(nvp);
		    get.setQueryString(nvp);
		}
		byte[] baResponse = null;
		_log.fine("executing GET:  " + extractMethodsURIString(get));
		try{
			getHttpClient().executeMethod(get);
			verifyResponseOK(get);
			baResponse = get.getResponseBody();
		}catch (TransmissionException te){
			throw te;
		}catch(Exception e){
		    //e.printStackTrace();
			_log.severe("unanticipated exception performing http GET: " +
			        extractMethodsURIString(get));
			throw new TransmissionException(e);
		}
		return baResponse;
	}

    /** Does an authenticated GET request against the server using the
     * input url suffix and params. returns an input stream that needs
     * to be consumed, rather than the complete response content all
     * at once. The input stream will remain open for as long as more
     * data is being downloaded, which is potentially a long time. 
     * 
     * <p>url visited will be:
     * <p>http://<meandre-host>:<port>/<sRestCommand><queryParams(?n1=v1?n2=v2...)>
     * 
     * @param sRestCommand The url suffix
     * @param queryParams the http query params to append to the url. Null is
     * an acceptable value for this set if no params are needed.
     * 
     * @return The raw content bytes of the server's response as a stream
     */
    public InputStream executeGetRequestStream(String sRestCommand, 
            Set<NameValuePair> queryParams)
            throws TransmissionException{

        GetMethod get = new GetMethod();
        get.setPath("/" + sRestCommand);
        get.setDoAuthentication(true);
        if(queryParams != null){
            NameValuePair[] nvp = new NameValuePair[queryParams.size()]; 
            nvp = queryParams.toArray(nvp);
            get.setQueryString(nvp);
        }
        InputStream insResponse = null;
        _log.fine("executing GET:" + extractMethodsURIString(get));
        try{
            getHttpClient().executeMethod(get);
			verifyResponseOK(get);
            insResponse = get.getResponseBodyAsStream();
		}catch (TransmissionException te){
			throw te;
        }catch(Exception e){
            _log.severe("unanticipated exception - GET: " + 
                    extractMethodsURIString(get));
            throw new TransmissionException(e);
        }
        return insResponse;
    }	
	
	

    /** Does an authenticated POST request against the server with the input
	 * url suffix, params, and file/data parts. 
     * 
	 * 
	 * @param sRestCommand The url suffix
	 * @param queryParams the http query param name-value pairs to append
	 * @param dataParts the post content parts for a multipart request, usually 
	 * 					Files to upload.
	 *
	 * @return The raw content bytes of the server's response
	 */

	public byte[] executePostRequestBytes(String sRestCommand, 
			Set<NameValuePair> queryParams, Set<Part> dataParts) 
			throws TransmissionException{
	    
	    PostMethod post = new PostMethod();
        post.setPath("/" + sRestCommand);
        post.setDoAuthentication(true);
        
        Set<Part> parts = null;
        if(dataParts == null){
            parts = new HashSet<Part>();
        }else{
            parts = dataParts;
        }
        if(queryParams != null){
            for(NameValuePair param: queryParams){
                parts.add(new StringPart(param.getName(), param.getValue()));
            }
        }
        Part[] aParts = new Part[parts.size()];
        parts.toArray(aParts);
        post.setRequestEntity(
                new MultipartRequestEntity(aParts, post.getParams()));
        post.getParams().setBooleanParameter(
				HttpMethodParams.USE_EXPECT_CONTINUE, true);
        byte[] baResponse = null;
        _log.fine("executing POST: " + extractMethodsURIString(post));
        _log.finer("POST contents:\n" + post.toString());
        try{
            getHttpClient().executeMethod(post);
			verifyResponseOK(post);
            baResponse = post.getResponseBody();
		}catch (TransmissionException te){
			throw te;
        }catch(Exception e){
			_log.severe("unanticipated exception - POST: " + 
			        extractMethodsURIString(post));
            throw new TransmissionException(e);
        }
        return baResponse;
	}


	/**
	 * performs a GET request and returns the response data as a string.
     * see <code>executePostRequestBytes</code> for info on params
	 *
	 */
	public String executeGetRequestString(String sRestCommand,
			Set<NameValuePair> queryParams) throws TransmissionException {
	   
        byte[] baRetrieved = executeGetRequestBytes(sRestCommand, queryParams);
        String sRetrieved;
	    try {
	        sRetrieved = new String(baRetrieved, "UTF-8");
	    } catch (UnsupportedEncodingException e) {
	        throw new TransmissionException(
	                "Server response couldn't be converted to UTF-8 text", e);
	    }
        _log.finer("Response from GET:\n" + sRetrieved);
        return sRetrieved;
	}

	/***
	 * performs a GET request and returns the response in json format.
	 * returns null if the response was an empty string.
     * see <code>executePostRequestBytes</code> for info on params
	 *  
	 */
	public JSONTokener executeGetRequestJSON(String sRestCommand, 
			Set<NameValuePair> queryParams) throws TransmissionException {

	    String sRaw = executeGetRequestString(sRestCommand, queryParams);
	    if(sRaw.equals("")){
        //TODO: there should be a better response than returning null
	        return null;
	    }
	    JSONTokener jtRaw = new JSONTokener(sRaw);
	    return jtRaw;
	}
	/**
	 * performs a GET request and returns the response data as an RDF
	 * Jena Model. The sRestCommand must request the model data in 
	 * the N-Triple format (*.nt file)
     * see <code>executePos1tRequestBytes</code> for info on params
	 *
	 */
	public Model executeGetRequestModel(String sRestCommand,
			Set<NameValuePair> queryParams) throws TransmissionException {
	    
	    byte[] baRetrieved = executeGetRequestBytes(sRestCommand, queryParams);
	    Model mRetrieved = ModelFactory.createDefaultModel();
	    try{
	        mRetrieved.read(new ByteArrayInputStream(baRetrieved),null,"N-TRIPLE");
	    //}catch(NullPointerException e){
        //    throw new TransmissionException("Problem constructing Jena Model: " +
        //           "Usually this means the server returned an empty string," +
        //            "meaning the requested rdf resource was not found.", e);
	    }catch(Exception e){
	        throw new TransmissionException(
				"Problem constructing Jena Model from downloaded bytes", e);
	    }
        return mRetrieved;

	}

	/**
	 * checks that the method has executed properly and it's response 
	 * code indicates a successful operation. throws an exception
	 * with a description of the failure.
     *
     * currently, any 2XX code is considered OK, and all others are
     * considered failures and result in an Exception.
	 */
	protected void verifyResponseOK(HttpMethod method) 
			throws TransmissionException{
        int iStat = method.getStatusCode();
        if( (iStat < 200) || (iStat > 299) ) {
            String reason = HttpStatus.getStatusText(iStat);
            String sFailedUrl = extractMethodsURIString(method);
            StringBuffer errBuf = new StringBuffer();
            errBuf.append("Problem contacting the Meandre Server at \'");
            errBuf.append(_serverHost);
            errBuf.append("\', port:");
            errBuf.append(_port);
            errBuf.append(", as user:\'");
            errBuf.append(_credentials.getUserName());
            errBuf.append("\' \n ");
            errBuf.append(method.getName());
            errBuf.append(" ");
            errBuf.append(sFailedUrl);
            errBuf.append("\' \n HTTP Response Code ");
            errBuf.append(iStat);
            errBuf.append(" -> ");
            errBuf.append(reason);

            _log.severe(errBuf.toString());

		    throw new TransmissionException(iStat + " " + reason + " " +
                sFailedUrl);
        }
		return;
	}

	/**
	 * A convience method for getting the URI that a GET or POST method
	 * is using. Intended for logging: if extracting the URI fails then
	 * "UNKNOWN_URL" is returned but no indication of failure will be
	 * given.
	 * 
	 * @param method an HttpMethod, usually a POST or GET with all it's
	 * parameters set.
	 * @return the url the http method visited as a String
	 */
    private String extractMethodsURIString(HttpMethod method) {
        String sFailedUrl = "";
        try{
            sFailedUrl = method.getURI().toString();
            //sFailedUrl = method.toString();
        }catch(URIException e){
            sFailedUrl = "UNKNOWN_URL";
        }
        return sFailedUrl;
    }


}
