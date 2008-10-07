package org.meandre.client;

import java.util.*;
import java.util.Map.Entry;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.*;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import org.meandre.core.repository.*;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.multipart.ByteArrayPartSource;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.PartSource;


/**
 * Programmatic interface to the Meandre server webservices API. Mimicks opening
 * a session with the server and allowing the client to interact with it,
 * although in reality the session has no state and WSCLient simply sends an
 * independent http request for every operation.
 *
 * @author pgroves
 *
 */
public class MeandreClient extends MeandreBaseClient{

    /**
     * initialize to talk to a particular server.
     *
     * @param serverHost just the hostname, e.g. "localhost", not "http://localhost"
     * @param serversPort the port on the serverhost that the server is listening
     */
    public MeandreClient(String serverHost, int serversPort){
        super(serverHost, serversPort);
    }



/////////
//About
/////////

    /**
     * requests all java properties of the server's Store.
     *
     *<p> calls:
     * http://<meandre_host>:<meandre_port>/services/about/installation.rdf
     *
     * TODO: need a java object instance to represent installation properties
     */
    public JSONObject retrieveInstallationProperties() throws TransmissionException{
        String sRestCommand = "services/about/installation.json";
        try {
        	JSONTokener jt = executeGetRequestJSON(sRestCommand, null);
            return (new JSONArray(jt)).getJSONObject(0);
		} catch (JSONException e) {
			throw new TransmissionException(e);
		}
    }

    /**
     * requests a list of assigned roles of the user (defined by the
     * credentials of this MeandreClient).
     *
     * @return a list of roles
     *
     *<p> calls:
     * http://<meandre_host>:<meandre_port>/services/about/user_roles.json
     * @throws TransmissionException
     *
     * TODO: Need java object to represent valid roles
     */

    public Set<String> retrieveUserRoles() throws TransmissionException {
    	try {
	        String sRestCommand = "services/about/user_roles.json";
	        JSONTokener jtRoles = executeGetRequestJSON(sRestCommand, null);
	//        String jArrayKey = "meandre_user_role";
	//        String jElementKey = "meandre_role";
	//        Set<String> ss = unpackJSONArray(jtRoles, jArrayKey, jElementKey);
	        JSONArray ja = new JSONArray(jtRoles);
	        Set<String> ss = new HashSet<String>();
	        for ( int i=0, iMax=ja.length() ; i<iMax ; i++ )
	        	ss.add(ja.getJSONObject(i).getString("meandre_role_uri"));
	        return ss;
    	}
    	catch ( Exception e ) {
    		throw new TransmissionException(e);
    	}
    }

    /**
     * requests the list of all valid roles the server supports. the roles
     * are returned in their url form.
     *
     * this is equivalent to getValidRoles() in MeandreAdminClient, but
     * this version requires only the 'about' role and not the 'admin'
     * role to access it. Also, this returns the url representation of
     * the roles, not Role objects.
     *
     * @return list of all valid roles
     */
    public Set<String> retrieveValidRoles() throws TransmissionException{
    	try {
	        String sRestCommand = "services/about/valid_roles.json";
	        JSONTokener jtRoles = executeGetRequestJSON(sRestCommand, null);
	//        String jArrayKey = "meandre_user_role";
	//        String jElementKey = "meandre_role";
	//        Set<String> ss = unpackJSONArray(jtRoles, jArrayKey, jElementKey);
	        JSONArray ja = new JSONArray(jtRoles);
	        Set<String> ss = new HashSet<String>();
	        for ( int i=0, iMax=ja.length() ; i<iMax ; i++ )
	        	ss.add(ja.getJSONObject(i).getString("meandre_role_uri"));
	        return ss;
    	}
    	catch ( Exception e ) {
    		throw new TransmissionException(e);
    	}

    }


/////////
//Locations (known peers of the server)
//////////

    /**
     * requests the locations (urls) of all meandre repositories the server
     * is aware of.
     *
     *<p> calls:
     * http://<meandre_host>:<meandre_port>/services/locations/list.json
     */
    public Set<LocationBean> retrieveLocations() throws TransmissionException{
    	try {
	        String sRestCommand = "services/locations/list.json";
	        JSONTokener jtLocs = executeGetRequestJSON(sRestCommand, null);
	//        String jArrayKey = "location_information";
	//        String jURLElementKey = "location";
	//        String jDescElementKey = "description";
	//        Map<String, String> smLocs = unpackJSONMap(jtLocs, jArrayKey, jURLElementKey,
	//                jDescElementKey);
	//        Iterator<String> locIter = smLocs.keySet().iterator();
	//        HashSet<LocationBean> beanSet = new HashSet<LocationBean>();
	//        while (locIter.hasNext()){
	//            String sLoc = locIter.next();
	//            String sDesc = smLocs.get(sLoc);
	//            beanSet.add(new LocationBean(sLoc, sDesc));
	//        }
	        JSONArray ja = new JSONArray(jtLocs);
	        HashSet<LocationBean> beanSet = new HashSet<LocationBean>();
	        for ( int i=0, iMax=ja.length() ; i<iMax ; i++ ) {
	        	JSONObject jo = ja.getJSONObject(i);
	        	beanSet.add(new LocationBean(jo.getString("location"), jo.getString("description")));
	        }
	        	
	        return beanSet;
    	}
    	catch (Exception e) {
			throw new TransmissionException(e);
		}
    }

    /**
     * Adds or updates the location of a meandre server peer. returns true
     * if the location is registered with the server after the call (whether
     * it was added or was already present).
     *
     *<p> calls:
     * http://<meandre_host>:<meandre_port>/services/locations/add.json
     *
     * TODO: Handle possible bad_request errors in http response
     */
    public boolean addLocation(String sLocationUrl, String description)
        throws TransmissionException{
        String sRestCommand = "services/locations/add.json";

        Set<NameValuePair> nvps = new HashSet<NameValuePair>();
        nvps.add(new NameValuePair("location", sLocationUrl));
        nvps.add(new NameValuePair("description", description));

        JSONTokener jtRetrieved = executeGetRequestJSON(sRestCommand, nvps);
        JSONArray ja;
        try {
			ja = new JSONArray(jtRetrieved);
		} catch (JSONException e) {
			ja = null;
		}
        if(ja == null){
            return false;
        }
        try{
            JSONObject joRetrieved = ja.getJSONObject(0);
            String loc = joRetrieved.getString("location");
            String descr = joRetrieved.getString("description");
            return ((loc.equals(sLocationUrl)) && (descr.equals(description)));
        }catch(JSONException je){
            return false;
        }
    }


    /**
     * removes the input location from the server's list of peers. returns
     * true if the location is not a peer after this method is called
     * (regardless of whether this removed it or if it wasn't there in
     * the first place).
     *
     *<p> calls:
     * http://<meandre_host>:<meandre_port>/services/locations/remove.json
     */
    public boolean removeLocation(String sUrl) throws TransmissionException{
        String sRestCommand = "services/locations/remove.json";

        Set<NameValuePair> nvps = new HashSet<NameValuePair>();
        nvps.add(new NameValuePair("location", sUrl));

        JSONTokener jtRetrieved = executeGetRequestJSON(sRestCommand, nvps);
        try{
            JSONObject joRetrieved = new JSONObject(jtRetrieved);
            if(jtRetrieved == null){
                return false;
            }
            String loc = joRetrieved.getString("location");
            return (loc.equals(sUrl));
        }catch(JSONException je){
            return false;
        }
    }


////////////
//Repository
/////////////

    /**
     * Locally recreates a Repository from the rdf model from the server.
     * The contents of the repository are dependent on the user requesting
     * it.
     *
     *<p> calls:
     * http://<meandre_host>:<meandre_port/services/repository/dump.nt
     * @throws TransmissionException
     */
    public QueryableRepository retrieveRepository() throws TransmissionException {
        String sRestCommand = "services/repository/dump.nt";
        Model model = executeGetRequestModel(sRestCommand, null);
        QueryableRepository qr = new RepositoryImpl(model);
        return qr;
    }


    /**
     * Tells the server to rebuild it's repository by (re-)querying all
     * of it's peers for information on available components and flows.
     *
     *<p> calls:
     * http://<meandre_host>:<meandre_port>/services/repository/regenerate.json
     */
    public boolean regenerate() throws TransmissionException{
		try {
    		String sRestCommand = "services/repository/regenerate.json";
		    JSONTokener jt = executeGetRequestJSON(sRestCommand, null);
		    JSONArray ja = new JSONArray(jt);
		    String sSuccess = "Repository successfully regenerated";
		    return (sSuccess.equals(ja.getJSONObject(0).getString("message")));
		}
		catch ( JSONException e ) {
			throw new TransmissionException(e);
		}
    }


    /**
     * requests the urls of all components in the server repository.
     *
     *<p> calls:
     * http://<meandre_host>:<meandre_port>/services/repository/list_components.json
     * @throws TransmissionException
     */
    public Set<URL> retrieveComponentUrls() throws TransmissionException {
        String sRestCommand = "services/repository/list_components.json";
        JSONTokener jtRetrieved = executeGetRequestJSON(sRestCommand, null);
        String jArrayKey = "meandre_executable_component";
        String jObjectKey = "meandre_uri";
        Set<String> sCompUrls = unpackJSONArray(jtRetrieved, jArrayKey, jObjectKey);
        Set<URL> componentUrls = convertStringSetToUrls(sCompUrls);
        return componentUrls;
    }

    /**
     * requests the urls of all flows in the server repository.
     *
     *<p> calls:
     * http://<meandre_host>:<meandre_port>/services/repository/list_flows.json
     * @throws TransmissionException
     */
    public Set<URL> retrieveFlowUrls() throws TransmissionException {
        String sRestCommand = "services/repository/list_flows.json";
        JSONTokener jtRetrieved = executeGetRequestJSON(sRestCommand, null);
        String jArrayKey = "meandre_flow_component";
        String jObjectKey = "meandre_uri";
        Set<String> sFlowUrls = unpackJSONArray(jtRetrieved, jArrayKey, jObjectKey);
        Set<URL> uFlows = convertStringSetToUrls(sFlowUrls);
        return uFlows;
    }


    /**
     * requests all tags for any and all components and flows.
     *
     *<p> calls:
     *http://<meandre_host>:<meandre_port>/services/repository/tags.json
     * @throws TransmissionException
     *
     * TODO:return tag objects instead of strings
     */
    public Set<String> retrieveAllTags() throws TransmissionException {
        String sRestCommand = "services/repository/tags.json";
        JSONTokener jtRetrieved = executeGetRequestJSON(sRestCommand, null);
        String jArrayKey = "meandre_tags";
        String jObjectKey = "tag";
        Set<String> sTags = unpackJSONArray(jtRetrieved, jArrayKey, jObjectKey);
        return sTags;

    }

    /**
     * requests all tags for all components.
     *
     *<p> calls:
     *http://<meandre_host>:<meandre_port>/services/repository/tags_components.json
     * @throws TransmissionException
     * TODO:return tag objects instead of strings
     */
    public Set<String> retrieveComponentTags() throws TransmissionException {
        String sRestCommand = "services/repository/tags_components.json";
        JSONTokener jtRetrieved = executeGetRequestJSON(sRestCommand, null);
        String jArrayKey = "meandre_tags";
        String jObjectKey = "tag";
        Set<String> sTags = unpackJSONArray(jtRetrieved, jArrayKey, jObjectKey);
        return sTags;
    }

    /**
     * requests all tags for all flows.
     *
     *<p> calls:
     *http://<meandre_host>:<meandre_port>/services/repository/tags_flows.json
     * @throws TransmissionException
     * TODO:return tag objects instead of strings
     */
    public Set<String> retrieveFlowTags() throws TransmissionException {
        String sRestCommand = "services/repository/tags_flows.json";
        JSONTokener jtRetrieved = executeGetRequestJSON(sRestCommand, null);
        String jArrayKey = "meandre_tags";
        String jObjectKey = "tag";
        Set<String> sTags = unpackJSONArray(jtRetrieved, jArrayKey, jObjectKey);
        return sTags;
    }

    /**
     * requests the urls of all components that have the input tag.
     *
     *<p> calls:
     *http://<meandre_host>:<meandre_port>/services/repository/components_by_tag.json
     * @throws TransmissionException
     * TODO:input a tag object instead of string
     */
    public Set<URL> retrieveComponentsByTag(String sTag) throws TransmissionException {
        String sRestCommand = "services/repository/components_by_tag.json";

        Set<NameValuePair> nvps = new HashSet<NameValuePair>();
        nvps.add(new NameValuePair("q", sTag));
        JSONTokener jtRetrieved = executeGetRequestJSON(sRestCommand, nvps);
        String jArrayKey = "meandre_executable_component";
        String jObjectKey = "meandre_uri";
        Set<String> sCompUrls = unpackJSONArray(jtRetrieved, jArrayKey, jObjectKey);
        Set<URL> componentUrls = convertStringSetToUrls(sCompUrls);
        return componentUrls;
    }

    /**
     * requests the urls of all flows that have the input tag.
     *
     *<p> calls:
     *http://<meandre_host>:<meandre_port>/services/repository/flows_by_tag.json
     * @throws TransmissionException
     * TODO:input a tag object instead of string
     */
    public Set<URL> retrieveFlowsByTag(String sTag) throws TransmissionException {
        String sRestCommand = "services/repository/flows_by_tag.json";
        Set<NameValuePair> nvps = new HashSet<NameValuePair>();
        nvps.add(new NameValuePair("q", sTag));
        JSONTokener jtRetrieved = executeGetRequestJSON(sRestCommand, nvps);
        String jArrayKey = "meandre_flow_component";
        String jObjectKey = "meandre_uri";
        Set<String> sFlowUrls = unpackJSONArray(jtRetrieved, jArrayKey, jObjectKey);
        Set<URL> uFlows = convertStringSetToUrls(sFlowUrls);
        return uFlows;
    }


    /**
     * requests the component description model from the server.
     *
     *<p> calls:
     *http://<meandre_host>:<meandre_port>/services/repository/describe_component.nt
     * @throws TransmissionException
     */
    public ExecutableComponentDescription retrieveComponentDescriptor(
            String sComponentUrl) throws TransmissionException {

        String sRestCommand = "services/repository/describe_component.nt";
        Set<NameValuePair> nvps = new HashSet<NameValuePair>();
        nvps.add(new NameValuePair("uri", sComponentUrl));
        Model compModel = executeGetRequestModel(sRestCommand, nvps);

        QueryableRepository repo = new RepositoryImpl(compModel);
        Set<ExecutableComponentDescription> repoComps =
                repo.getAvailableExecutableComponentDescriptions();
        Iterator<ExecutableComponentDescription> iter = repoComps.iterator();
        ExecutableComponentDescription comp = iter.next();

        if(iter.hasNext()){
            throw new TransmissionException("More than one Component " +
                    "Description was returned by the server.");
        }

        return comp;
    }

    /**
     * requests a flow description model from the server.
     *
     *<p> calls:
     *http://<meandre_host>:<meandre_port>/services/repository/describe_flow.nt
     * @throws TransmissionException
     */
    public FlowDescription retrieveFlowDescriptor(String sFlowUrl) throws TransmissionException {
        String sRestCommand = "services/repository/describe_flow.nt";
        Set<NameValuePair> nvps = new HashSet<NameValuePair>();
        nvps.add(new NameValuePair("uri", sFlowUrl));
        Model flowModel = executeGetRequestModel(sRestCommand, nvps);

        QueryableRepository repo = new RepositoryImpl(flowModel);
        Set<FlowDescription> repoFlows =
                repo.getAvailableFlowDescriptions();
        Iterator<FlowDescription> iter = repoFlows.iterator();
        FlowDescription flow = iter.next();

        if(iter.hasNext()){
            throw new TransmissionException("More than one Flow " +
                    "Description was returned by the server.");
        }
        return flow;
    }


    /**
     *TODO: need serious docs on this or a query object to input instead of
     * a string.
     *
     *<p> calls:
     *http://<meandre_host>:<meandre_port>/services/repository/search_components.json
     * @throws TransmissionException
     */
    public Set<URL> retrieveComponentUrlsByQuery(String sQuery)
            throws TransmissionException {
        String sRestCommand = "services/repository/search_components.json";
        Set<NameValuePair> nvps = new HashSet<NameValuePair>();
        nvps.add(new NameValuePair("q", sQuery));
        JSONTokener jtRetrieved = executeGetRequestJSON(sRestCommand, nvps);
        String jArrayKey = "meandre_executable_component";
        String jObjectKey = "meandre_uri";
        Set<String> sCompUrls = unpackJSONArray(jtRetrieved, jArrayKey, jObjectKey);
        Set<URL> componentUrls = convertStringSetToUrls(sCompUrls);
        return componentUrls;

    }

    /**
     * TODO: need serious docs on this or a query object to input instead of
     * a string.
     *
     *<p> calls:
     *http://<meandre_host>:<meandre_port>/services/repository/search_flows.json
     * @throws TransmissionException
     */
    public Set<URL> retrieveFlowUrlsByQuery(String sQuery)
            throws TransmissionException {

        String sRestCommand = "services/repository/search_flows.json";
        Set<NameValuePair> nvps = new HashSet<NameValuePair>();
        nvps.add(new NameValuePair("q", sQuery));
        JSONTokener jtRetrieved = executeGetRequestJSON(sRestCommand, nvps);

        String jArrayKey = "meandre_flow_component";
        String jObjectKey = "meandre_uri";
        Set<String> sFlowUrls = unpackJSONArray(jtRetrieved, jArrayKey, jObjectKey);
        Set<URL> uFlows = convertStringSetToUrls(sFlowUrls);
        return uFlows;
    }


    /**
     * uploads a single flow to the server.
     *
     * <p> calls:
     * http://<meandre_host>:<meandre_port>/services/repository/add.nt
     *
     */
    public boolean uploadFlow(FlowDescription flow, boolean overwrite)
            throws TransmissionException{

        Model mod = flow.getModel();
        return uploadModel(mod, null, overwrite);
    }

    /**
     * uploads a set of flows to the server.
     *
     * <p> calls:
     * http://<meandre_host>:<meandre_port>/services/repository/add.nt
     * TODO:Need test
     */
    public boolean uploadFlowBatch(Set<FlowDescription> flows, boolean overwrite)
            throws TransmissionException{

        HashSet<Model> hsFlowModels = new HashSet<Model>();
        Iterator<FlowDescription> flowIter = flows.iterator();
        while(flowIter.hasNext()){
            hsFlowModels.add(flowIter.next().getModel());
        }
        return uploadModelBatch(hsFlowModels, null, overwrite);
    }

    /**
     * uploads a single component to the server.
     *
     * <p> calls:
     * http://<meandre_host>:<meandre_port>/services/repository/add.nt
     *
     */
    public boolean uploadComponent(ExecutableComponentDescription component,
            Set<File> jarFileContexts, boolean overwrite)
            throws TransmissionException{

        Model mod = component.getModel();
        return uploadModel(mod, jarFileContexts, overwrite);
    }

    /**
     * uploads a set of flows to the server.
     *
     * <p> calls:
     * http://<meandre_host>:<meandre_port>/services/repository/add.nt
     * TODO:Need test
     */
    public boolean uploadComponentBatch(Set<ExecutableComponentDescription> comps,
            Set<File> jarFileContexts, boolean overwrite)
            throws TransmissionException {

        HashSet<Model> hsComponentModels = new HashSet<Model>();
        Iterator<ExecutableComponentDescription> compIter = comps.iterator();
        while(compIter.hasNext()){
            hsComponentModels.add(compIter.next().getModel());
        }
        return uploadModelBatch(hsComponentModels, jarFileContexts, overwrite);

    }


    /**
     * uploads all resources of a repository to a server, merging it with
     * the server's repository.
     *
     * the jar files set may be null.
     *
     * <p> calls:
     * http://<meandre_host>:<meandre_port>/services/repository/add.nt
     * TODO:Need test
     */
    public boolean uploadRepository(QueryableRepository repo,
            Set<File> jarFileContexts, boolean overwrite)
            throws TransmissionException {

        Model mod = repo.getModel();
        return uploadModel(mod, jarFileContexts, overwrite);
    }

    /**
     * uploads a single model containing flows and/or components and any
     * jar files.
     * the jarfiles set may be null.
     *
     * <p> calls:
     * http://<meandre_host>:<meandre_port>/services/repository/add.nt
     * TODO:Need test
     */
    private boolean uploadModel(Model mod, Set<File> jarFileContexts,
            boolean overwrite) throws TransmissionException {

        HashSet<Model> modSet = new HashSet<Model>(1);
        modSet.add(mod);
        return uploadModelBatch(modSet, jarFileContexts, overwrite);
    }

    /**
     * uploads a set of component or flow resources and any jar files.
     * the jarfiles set may be null.
     *
     * Note: this is the main upload function that actually does the
     * upload. all other upload* methods call this.
     *
     * <p> calls:
     * http://<meandre_host>:<meandre_port>/services/repository/add.nt
     * TODO:Need test
     */
    public boolean uploadModelBatch(Set<Model> msResourceModels,
            Set<File> jarFileContexts, boolean overwrite)
            throws TransmissionException {

        String sRestCommand = "services/repository/add.nt";

        Set<NameValuePair> nvps = new HashSet<NameValuePair>();
        nvps.add(new NameValuePair("overwrite", Boolean.toString(overwrite)));
        nvps.add(new NameValuePair("dump", "false"));

        Set<Part> postParts = new HashSet<Part>();

        for(Model modUpload : msResourceModels){
            ByteArrayOutputStream osModel = new ByteArrayOutputStream();
            modUpload.write(osModel, "N-TRIPLE");
            byte[] baModel = osModel.toByteArray();
            //NOTE: "InMemoryBytes" is given as the filename, and it is not
            //clear what it's used for by httpclient in this context
            PartSource source = new ByteArrayPartSource("InMemoryBytes",
            		baModel);
            postParts.add(new FilePart("repository", source));
        }

        if(jarFileContexts != null){
            for(File jarFile : jarFileContexts){
                //System.out.println("MeandreClient Upload, Jar file:" +
                //jarFile.getAbsolutePath());
                try{
                    if(!jarFile.exists()){
                        throw new FileNotFoundException(jarFile.toString());
                    }
                    postParts.add(new FilePart("context", jarFile));
                }catch(FileNotFoundException fne){
                   String msg = "Jar file to upload could not be found.";
                   throw new TransmissionException(msg, fne);
                }
            }
        }
        byte[] baRetrieved = executePostRequestBytes(sRestCommand, nvps, postParts);
        System.out.println("returned: " + (new String(baRetrieved)));
        return true;
    }

    /**
     * Uploads a set of jar files to the resources directory of the server.
     * For instance, jar files required by an applet that a component
     * uses in it's web UI, which are not uploaded with the component itself
     * because the component has no direct dependency on them, would be
     * uploaded via this method and then be available to the applet.
     *
     *
     * <p> calls:
     * http://<meandre_host>:<meandre_port>/services/repository/add.nt
     * TODO:Need test
     */
    public boolean uploadFiles(Set<File> files, boolean overwrite)
            throws TransmissionException {
    	//just use the regular uploader with no models
    	Set<Model> emptyModelSet = new HashSet<Model>(0);
    	boolean ret = uploadModelBatch(emptyModelSet, files, overwrite);
    	return ret;
    }

    /**
     *removes (deletes) either a component or flow from the server. returns
     *true if the resource was deleted.
     *
     *<p> calls:
     *http://<meandre_host>:<meandre_port>/services/repository/remove.json
     * TODO: need more specific error reporting when the server returns an empty
     * json string
     */
    public boolean removeResource(String sResourceUrl) throws TransmissionException{
        String sRestCommand = "services/repository/remove.json";
        Set<NameValuePair> nvps = new HashSet<NameValuePair>();
        nvps.add(new NameValuePair("uri", sResourceUrl));
        JSONTokener jtRetrieved = executeGetRequestJSON(sRestCommand, nvps);
        if(jtRetrieved == null){
            return false;
        }
        try{
            JSONObject joRetrieved = new JSONObject(jtRetrieved);
            if(joRetrieved.isNull("meandre_uri")){
                return false;
            }
            String sRetrieved = joRetrieved.getString("meandre_uri");
            //server returns the url if the resource was successfully removed
            return (sRetrieved.equals(sResourceUrl));
        }catch(JSONException e){
            throw new TransmissionException(e);
        }
    }



/////////
//Publish
/////////

    /**
     * commands the server to change a component or flow's status to "published."
     * returns true if the resource is in a state of "published" after this
     * method returns.
     *
     *<p> calls:
     * http://<meandre_host>:<meandre_port>/services/publish/publish.json
     */
    public boolean publish(String sResourceUrl) throws TransmissionException {
        String sRestCommand = "services/publish/publish.json";
        Set<NameValuePair> nvps = new HashSet<NameValuePair>();
        nvps.add(new NameValuePair("uri", sResourceUrl));
        JSONTokener jtRetrieved = executeGetRequestJSON(sRestCommand, nvps);
        if(jtRetrieved == null){
            return false;
        }
        String sRetrieved = unpackJSONObject(jtRetrieved, "meandre_uri");
        return (sResourceUrl.equals(sRetrieved));
    }


    /**
     * commands the server to change a component or flow's status to
     * "not published."
     *
     * returns true no matter what as long as the server received and understood
     * the request.
     *
     * TODO: modify so returns true if the resource is not in a state of
     * "published" after this method returns.
     *
     *<p> calls:
     *http://<meandre_host>:<meandre_port>/services/publish/unpublish.json
     */
    public boolean unpublish(String sResourceUrl) throws TransmissionException {
        String sRestCommand = "services/publish/unpublish.json";
        Set<NameValuePair> nvps = new HashSet<NameValuePair>();
        nvps.add(new NameValuePair("uri", sResourceUrl));
        JSONTokener jtRetrieved = executeGetRequestJSON(sRestCommand, nvps);
        if(jtRetrieved == null){
            return false;
        }
        String sRetrieved = unpackJSONObject(jtRetrieved, "meandre_uri");
        return (sResourceUrl.equals(sRetrieved));
    }


 /////////
//Execution
///////////

    /**
     * commands the server to run the flow with the given url-name. the returned
     * string is a human readable printout of stdout from the components in the
     * flow and (optionally, if verbose=true) statistics about the flow run.
     *
     * This method currently blocks waiting for flow to complete -> it does
     * not return the result string until the flow has completely finished.
     *
     *<p> calls:
     * http://<meandre_host>:<meandre_port>/services/execute/flow.txt
     */

    public String runFlow(String sFlowUrl, boolean verbose)
        throws TransmissionException{
        String sRestCommand = "services/execute/flow.txt";
        Set<NameValuePair> nvps = new HashSet<NameValuePair>();
        nvps.add(new NameValuePair("uri", sFlowUrl));
        nvps.add(new NameValuePair("statistic", Boolean.toString(verbose)));


        String sResults = executeGetRequestString(sRestCommand, nvps);
        return sResults;
    }

    /**
     * commands the server to run the flow with the given url-name. the returned
     * string is a human readable printout of stdout from the components in the
     * flow and (optionally, if verbose=true) statistics about the flow run.
     *
     * This method currently blocks waiting for flow to complete -> it does
     * not return the result string until the flow has completely finished.
     *
     *<p> calls:
     * http://<meandre_host>:<meandre_port>/services/execute/flow.txt
     */

    public InputStream runFlowStreamOutput(String sFlowUrl, boolean verbose)
        throws TransmissionException{
        String sRestCommand = "services/execute/flow.txt";
        Set<NameValuePair> nvps = new HashSet<NameValuePair>();
        nvps.add(new NameValuePair("uri", sFlowUrl));
        nvps.add(new NameValuePair("statistic", Boolean.toString(verbose)));


        InputStream insResult = executeGetRequestStream(sRestCommand, nvps);
        return insResult;
    }

    /**
     * Retrieves the WebUI information for the flow referenced by 'token'
     * Example of WebUI information returned:
     *          port=1716
     *          hostname=192.168.0.2
     *          token=1213938009687
     *          uri=http://test.org/flow/webmonkflow/1213938147793/1565344277
     *
     * Note: Not yet unit tested
     *
     * @param token The token of the flow to return the WebUI information for
     * @return  A JSONObject containing the WebUI information
     * @throws TransmissionException
     */
    public JSONObject retrieveWebUIInfo(String token) throws TransmissionException {
        String sRestCommand = "services/execute/uri_flow.txt";
        Set<NameValuePair> nvps = new HashSet<NameValuePair>();
        nvps.add(new NameValuePair("token", token));

        InputStream results = executeGetRequestStream(sRestCommand, nvps);

        Properties properties = new Properties();
        try {
            properties.load(results);
        }
        catch (IOException e) {
            throw new TransmissionException(e);
        }

        System.out.println(properties.toString());

        JSONObject joWebUIInfo = (properties.isEmpty()) ? null : new JSONObject();

        for (Entry<Object, Object> prop : properties.entrySet())
            try {
                joWebUIInfo.put(prop.getKey().toString(), prop.getValue());
            }
            catch (JSONException e) {
                throw new TransmissionException(e);
            }

        return joWebUIInfo;
    }

    /**
     * returns the url name of any running flows and the url assigned to
     * the webui component of the flow.
     *
     * @return a map where the keys are flow id urls, and the values are webui
     * urls
     *
     *<p> calls:
     *http://<meandre_host>:<meandre_port>/services/execute/list_running_flows.json
     *TODO: need to reverse the order in the map so that the always unique
     * webui_url is the key and the not-always-unique flow intance url is
     * the value. requires a server side change.
     * FIXME: This is totally untested.
     */
    public Map<URL,URL> retrieveRunningFlows() throws TransmissionException{
        String sRestCommand = "services/execute/list_running_flows.json";
        JSONTokener jtRetrieved = executeGetRequestJSON(sRestCommand, null);
        String jArrayKey = "meandre_running_flows";
        String jKeyElementKey = "flow_instance_uri";
        String jValueElementKey = "flow_instance_webui_uri";
        Map<String, String> msRetrievedPairs = unpackJSONMap(jtRetrieved, jArrayKey,
                jKeyElementKey, jValueElementKey);
        Map<URL, URL> muRetrievedPairs = convertStringMapToUrls(msRetrievedPairs);
        return muRetrievedPairs;
    }



////////////
//Public
//////////////

    /**
     * retrieves the public repository of published resources. does not
     * require authorization.
     *
     *<p> calls:
     * http://<meandre_host>:<meandre_port>/public/services/repository.nt
     * @throws TransmissionException
     */
    public QueryableRepository retrievePublicRepository()
            throws TransmissionException {
        String sRestCommand = "public/services/repository.nt";
        byte[] baResponse = executeGetRequestBytes(sRestCommand, null);
		Model model = ModelFactory.createDefaultModel();
		model.read(new ByteArrayInputStream(baResponse), null, "N-TRIPLE");
		return new RepositoryImpl(model);
    }

    /**
     * retrieves the demo repository of published resources. does not
     * require authorization
     *
     *<p> calls:
     *http://<meandre_host>:<meandre_port>/public/services/demo_repository.nt
     */
    public QueryableRepository retrieveDemoRepository()
            throws TransmissionException {
        String sRestCommand = "public/services/demo_repository.nt";
        byte[] baResponse = executeGetRequestBytes(sRestCommand, null);
		Model model = ModelFactory.createDefaultModel();
		model.read(new ByteArrayInputStream(baResponse), null, "N-TRIPLE");
		return new RepositoryImpl(model);
    }


////////////////////////
//Admin of Running Flows
////////////////////////


    /**
     * commands the WEBUI of a flow abort a running flow. the currently running
     * component will be allowed to complete but no other components in the
     * active flow will fire. the flow is specified by the port on the server that
     * it's webui is running on.
     *
     * if this method returns true, it simply means that the abort
     * request was received by the server, it does not necessarily mean
     * that the currently running component(s) are no longer running.
     *
     *
     *<p> calls:
     *http://<meandre_host>:<webui_port>/admin/abort.txt
     * FIXME: This is totally untested.
     */
    public boolean abortFlow(int iRunningFlowPort)
            throws TransmissionException {
        //we must modify the global port, so save the value to reset it at the
        //end
        int masterPort = this.getPort();
        this.setServerAddress(this.getServerUrl(), iRunningFlowPort);

        String sRestCommand = "admin/abort.txt";
        boolean success = false;
        String sExpected = "Abort request dispatched...";
        try{
            String sRetrieved = executeGetRequestString(sRestCommand, null);
            success = (sRetrieved == sExpected);
        }catch(TransmissionException e){
            throw e;
        }finally{
            //reset the port for this client instance
            this.setServerAddress(this.getServerUrl(), masterPort);
        }
        return success;
    }

    /**
     * requests the current statistics of the currently running flow from
     * the WEBUI. the flow is specified by the port on the server that
     * it's webui is running on.
     *
     * the returned json data is in the format produced by
     * StatisticsProbeImpl.getSerializedStatistics()
     *
     * *<p> calls:
     * http://<meandre_host>:<webui_port>/admin/statistics.json
     *
     * TODO: refactor StatisticsProbeImpl so that a RunningFlowStatistics
     * "bean" can be read and written to/from json, and StatisticsProbeImpl
     * simply constructs the bean.
     * FIXME: This is totally untested.
     */
    public JSONObject retrieveRunningFlowStatisitics(int iRunningFlowPort)
            throws TransmissionException{

        //we must modify the global port, so save the value to reset it at the
        //end
        int masterPort = this.getPort();
        this.setServerAddress(this.getServerUrl(), iRunningFlowPort);

        String sRestCommand = "admin/statistics.json";
        JSONObject joRetrieved = null;
        try{
            JSONTokener jtRetrieved = executeGetRequestJSON(sRestCommand, null);
            joRetrieved = new JSONObject(jtRetrieved);
        }catch(TransmissionException e){
            throw e;
        }catch(JSONException je){
            throw new TransmissionException(je);
        }finally{
            //reset the port for this client instance
            this.setServerAddress(this.getServerUrl(), masterPort);
        }

        return joRetrieved;
    }


    ///////////
    //private
    //////////





    /**
     * unpacks a list of strings that are packed in an JSONArray, packed in
     * a JSONObject. This is a common pattern for lists of elements returned by
     * the Meandre Web API.
     *
     * The input JSONTokener is converted to a JSONObject with a single element.
     * That element is a JSONArray with key jArrayKey. Each element in the
     * array is a JSONObject with key jElementKey, and the value of those elements
     * are extracted and returned as an array of strings.
     * @throws TransmissionException
     *
     */
    private Set<String> unpackJSONArray(JSONTokener jArrayWrapper,
            String jArrayKey, String jElementKey)
            throws TransmissionException{

        Set<String> scValues = new HashSet<String>();
        try{
            JSONObject joToplevel = new JSONObject(jArrayWrapper);
            JSONArray jArray = joToplevel.getJSONArray(jArrayKey);
            int numElems = jArray.length();

            for(int i = 0; i < numElems; i++){
                JSONObject jo = jArray.getJSONObject(i);
                String str = jo.getString(jElementKey);
                scValues.add(str);
            }

        }catch (JSONException exc){
            throw new TransmissionException(exc);
        }
        return scValues;
    }

    /**
     * unpacks a single string value from a tokener that contains a json object.
     * This is a common pattern for values returned by the Meandre Web API.
     *
     * The input tokener is converted into a JSONObject, and the string value
     * associated with the input jElementKey is retrieved and returned.
     *
     * If the json conversions fail, the JSONException is wrapped in a Transmission
     * Exception and thrown.
     */
    private String unpackJSONObject(JSONTokener jWrapper, String jElementKey)
            throws TransmissionException{
        String sValue = null;
        try{
            JSONObject jo = new JSONObject(jWrapper);
            sValue = jo.getString(jElementKey);
        }catch (JSONException exc){
            throw new TransmissionException(exc);
        }
        return sValue;
    }

    /**
     * unpacks a string to string Map encoded as a json array of key,value pairs.
     *
     * The input tokener is converted into a JSONObject. An array with jArrayKey
     * is extracted from the object. Each element of the array is cast to a
     * JSONObject and has a key string with jKeyElementKey and value string
     * with jValueElementKey. The returned Map is then a mapping from the
     * key strings to the value strings.
     */
    private Map<String, String> unpackJSONMap(JSONTokener jWrapper,
            String jArrayKey, String jKeyElementKey, String jValueElementKey)
            throws TransmissionException{

        Map<String, String> sMap = new HashMap<String, String>();
        try{
            JSONObject joToplevel = new JSONObject(jWrapper);
            JSONArray jArray = joToplevel.getJSONArray(jArrayKey);
            int numElems = jArray.length();
            for(int i = 0; i < numElems; i++){
                JSONObject jo = jArray.getJSONObject(i);
                String keyStr = jo.getString(jKeyElementKey);
                String valueStr = jo.getString(jValueElementKey);
                sMap.put(keyStr, valueStr);
            }
        }catch (JSONException exc){
            throw new TransmissionException(exc);
        }
        return sMap;
    }

    /** convert a Set of strings into a Set of URL's */
    private Set<URL> convertStringSetToUrls(Set<String> strs)
            throws TransmissionException{
        Set<URL> urls = new HashSet<URL>();
        Iterator<String> iter = strs.iterator();
        while(iter.hasNext()){
           try {
               urls.add(new URL(iter.next()));
           }catch (MalformedURLException e) {
               throw new TransmissionException(
                       "Problem converting downloaded data into URLs", e);
           }
        }
        return urls;
    }

    /**convert a String, String map to a URL, URL map */
    private Map<URL, URL> convertStringMapToUrls(Map<String, String> strMap)
            throws TransmissionException{
        Map<URL, URL> urlMap = new HashMap<URL, URL>();
        try{
            for(String strKey:strMap.keySet()){
                String strValue = strMap.get(strKey);
                URL urlKey = new URL(strKey);
                URL urlValue = new URL(strValue);
                urlMap.put(urlKey, urlValue);
            }
        }catch(MalformedURLException exc){
            throw new TransmissionException(
                    "Problem converting downloaded data into URLs", exc);
           }
        return urlMap;
    }

}
