package org.meandre.webservices.controllers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.meandre.configuration.CoreConfiguration;
import org.meandre.core.engine.Conductor;
import org.meandre.core.engine.ConductorException;
import org.meandre.core.engine.Executor;
import org.meandre.core.engine.MrProbe;
import org.meandre.core.engine.probes.StatisticsProbeImpl;
import org.meandre.core.repository.CorruptedDescriptionException;
import org.meandre.core.repository.FlowDescription;
import org.meandre.core.repository.QueryableRepository;
import org.meandre.core.store.Store;
import org.meandre.core.utils.Constants;
import org.meandre.core.utils.NetworkTools;
import org.meandre.webservices.beans.JobDetail;
import org.meandre.webservices.logger.WSLoggerFactory;
import org.meandre.webui.PortScroller;
import org.meandre.webui.WebUI;
import org.meandre.webui.WebUIException;
import org.meandre.webui.WebUIFactory;
import org.meandre.webui.WebUIFragment;

import com.hp.hpl.jena.rdf.model.Resource;

/** This class wraps the basic logic to execute flows.
 *
 * @author Xavier Llor&agrave;
 * -modified by Amit Kumar June 14th 2008 to return a 404 status if a flow does not exist
 */
public class WSExecuteLogic {

	/** The store to be used */
	private Store store;
	/** The core configuraion object ot use */
	private CoreConfiguration cnf;
	/**Stores process execute*/
	private Hashtable<String,JobDetail> executionTokenList;

	/** Creates the execute logic for the given store.
	 *
	 * @param store The store to use
	 * @param cnf The core configuration object
	 */
	public WSExecuteLogic ( Store store, CoreConfiguration cnf ) {
		this.store = store;
		this.cnf = cnf;
		this.executionTokenList = new Hashtable<String,JobDetail>();
	}

	/** Runs a flow given a URI against the user repository verbosely
	 *
	 * @param request The request object
	 * @param response The response object
	 * @throws IOException An exception arised while printing
	 * @throws CorruptedDescriptionException
	 * @throws ConductorException
	 */
	public void executeVerboseFlowURI(HttpServletRequest request,
			HttpServletResponse response) throws IOException,
			CorruptedDescriptionException, ConductorException {

		String sURI = request.getParameter("uri");
		boolean bStats = false;
		
		String sStats = request.getParameter("statistics");
		String token = request.getParameter("token");

		if ( sStats!=null ) {
			bStats = sStats.equals("true");
		}

		if ( sURI==null ) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		}
		else {
			//
			// Executing the flow
			//
			QueryableRepository qr = store.getRepositoryStore(request.getRemoteUser());
			Resource resURI = qr.getModel().createResource(sURI);
			FlowDescription fd = qr.getFlowDescription(resURI);
			if ( fd==null ) {
				//
				// flow not found
				//
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
			}
			else {
				//
				// Executing the flow
				//

				response.setStatus(HttpServletResponse.SC_OK);
				OutputStream outStream = response.getOutputStream();
				PrintStream pw = new PrintStream(outStream);

				pw.println("Meandre Execution Engine version "+Constants.MEANDRE_VERSION);
				pw.println("All rights reserved by DITA, NCSA, UofI (2007-2008)");
				pw.println("THIS SOFTWARE IS PROVIDED UNDER University of Illinois/NCSA OPEN SOURCE LICENSE.");
				pw.println();

				pw.flush();

				// Create the execution
				pw.println("Preparing flow: "+sURI);
				Conductor conductor = new Conductor(Conductor.DEFAULT_QUEUE_SIZE,cnf);
				Executor exec =null;

				// Redirecting the output
				PrintStream psOUT = System.out;
				PrintStream psERR = System.err;
				StatisticsProbeImpl spi = null;
				try {
					if ( !bStats ){
						exec = conductor.buildExecutor(qr, resURI,pw);
					}
					else {
						spi = new StatisticsProbeImpl();
						MrProbe mrProbe = new MrProbe(WSLoggerFactory.getWSLogger(),spi,false,false);
						exec = conductor.buildExecutor(qr, resURI, mrProbe,pw);
						mrProbe.setName(exec.getThreadGroupName()+"mr-probe");
					}
					pw.flush();
					int nextPort = PortScroller.getInstance(cnf).nextAvailablePort(exec.getFlowUniqueExecutionID());
					/*This needs to be synchronized*/
					boolean hasToken = Boolean.FALSE;
					if(token!=null){
						hasToken = Boolean.TRUE;
						if(this.executionTokenList.containsKey(token)){
							System.out.println("Error: "+ token +"  already used. The token should be unique." );
							response.sendError(HttpServletResponse.SC_BAD_REQUEST);
						}
					}
					if(hasToken){
						String executionId=	exec.getFlowUniqueExecutionID();
						if(executionId!=null){
						JobDetail jobDetail = new JobDetail();
						jobDetail.setToken(token);
						jobDetail.setFlowInstanceId(executionId);
						jobDetail.setHostname(getHostName());
						jobDetail.setPort(nextPort);
						this.executionTokenList.put(token, jobDetail);
						}
					}
					/**/

					// Redirecting the streamers
					System.setOut(pw);
					System.setErr(pw);

					pw.println("Preparation completed correctly\n");

					pw.print("Execution started at: " + nextPort +" on ");
					pw.println(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date()));
					pw.println("----------------------------------------------------------------------------");
					pw.flush();
					WebUI webui = exec.initWebUI(nextPort,token);
					exec.execute(webui);
					pw.flush();
					pw.println("----------------------------------------------------------------------------");
					pw.print("Execution finished at: ");
					pw.println(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date()));
					if ( exec.hadGracefullTermination() ) {
						//
						// Graceful termination
						//
						pw.println("Execution finished gracefully.");
					}
					else {
						//
						// Aborted execution.
						//
						pw.println("Execution aborted!!!\nReason:\n");
						for ( String sMsg:exec.getAbortMessage() )
							pw.println("\t"+sMsg);
					}
					pw.flush();

				}
				catch ( CorruptedDescriptionException cde ) {
					pw.println("Preparation could not be completed correctly!\n");
					pw.println("----------------------------------------------------------------------------");
					pw.println();
					pw.println("Reason for aborting the preparation:");
					pw.println();
					pw.println(cde);
					pw.flush();
				}
				catch ( ConductorException ce ) {
					pw.println("----------------------------------------------------------------------------");
					pw.print("Execution aborted at: ");
					pw.println(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date()));
					pw.println("----------------------------------------------------------------------------");
					pw.println();
					pw.println("Reason for aborting the execution:");
					pw.println();
					pw.println(ce);
					pw.flush();
				}
				catch ( NoClassDefFoundError te ) {
					pw.println("----------------------------------------------------------------------------");
					pw.print("Missing class definition: ");
					pw.println(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date()));
					pw.println("----------------------------------------------------------------------------");
					pw.println();
					pw.println("Reason for aborting the execution:");
					pw.println();
					pw.println(te);
					pw.flush();
				}finally{

				}

				if ( bStats ) {
					try {
						JSONObject jsonStats = spi.getSerializedStatistics();
						pw.println("----------------------------------------------------------------------------");
						pw.println();
						pw.println("Flow execution statistics");
						pw.println();
						pw.println("Flow unique execution ID : "+jsonStats.get("flow_unique_id"));
						pw.println("Flow state               : "+jsonStats.get("flow_state"));
						pw.println("Started at               : "+jsonStats.get("started_at"));
						pw.println("Last update              : "+jsonStats.get("latest_probe_at"));
						pw.println("Total run time (ms)      : "+jsonStats.get("runtime"));
						pw.println();
						pw.flush();

						JSONArray jaEXIS = (JSONArray) jsonStats.get("executable_components_statistics");
						for ( int i=0,iMax=jaEXIS.length() ; i<iMax ; i++ ) {
							JSONObject joEXIS = (JSONObject) jaEXIS.get(i);
							pw.println("\tExecutable components instance ID          : "+joEXIS.get("executable_component_instance_id"));
							pw.println("\tExecutable components state                : "+joEXIS.get("executable_component_state"));
							pw.println("\tTimes the executable components fired      : "+joEXIS.get("times_fired"));
							pw.println("\tAccumulated executable components run time : "+joEXIS.get("accumulated_runtime"));
							pw.println("\tPieces of data pulled                      : "+joEXIS.get("pieces_of_data_in"));
							pw.println("\tPieces of data pushed                      : "+joEXIS.get("pieces_of_data_out"));
							pw.println("\tNumber of properties read                  : "+joEXIS.get("number_of_read_properties"));
							pw.println();
						}
						pw.flush();
					}
					catch ( Exception e ) {
						WSLoggerFactory.getWSLogger().warning("This exception should have never been thrown\n"+e);
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						e.printStackTrace(new PrintStream(baos));
						WSLoggerFactory.getWSLogger().warning(baos.toString());
					}
				}

				// Reset the output redirection
				System.setOut(psOUT);
				System.setErr(psERR);
			}
		}
	}

	/** Runs a flow given a URI against the user repository silently only outputting
	 * the flow printed elements.
	 *
	 * @param request The request object
	 * @param response The response object
	 * @throws IOException An exception arised while printing
	 * @throws CorruptedDescriptionException
	 * @throws ConductorException
	 */
	public void executeSilentFlowURI(HttpServletRequest request,
			HttpServletResponse response) throws IOException,
			CorruptedDescriptionException, ConductorException {
		String sURI = request.getParameter("uri");

		if ( sURI==null ) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		}
		else {
			//
			// Executing the flow
			//
			QueryableRepository qr = store.getRepositoryStore(request.getRemoteUser());
			Resource resURI = qr.getModel().createResource(sURI);
			FlowDescription fd = qr.getFlowDescription(resURI);
			if ( fd==null ) {
				//
				// flow not found
				//
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
			}
			else {
				//
				// Executing the flow
				//

				response.setStatus(HttpServletResponse.SC_OK);
				OutputStream outStream = response.getOutputStream();
				PrintStream pw = new PrintStream(outStream);

				// Create the execution
				Conductor conductor = new Conductor(Conductor.DEFAULT_QUEUE_SIZE,cnf);
				Executor exec = null;

				// Redirecting the output
				//PrintStream psOUT = System.out;
				//PrintStream psERR = System.err;

				try {
					exec = conductor.buildExecutor(qr, resURI,pw);

					// Redirecting the streamers
					System.setOut(pw);
					System.setErr(pw);

					pw.flush();
					int nextPort = PortScroller.getInstance(cnf).nextAvailablePort(exec.getFlowUniqueExecutionID());
					/*This needs to be synchronized*/
					boolean hasToken = Boolean.FALSE;
					String token = null;
					token = request.getParameter("token");
					if(token!=null){
						hasToken = Boolean.TRUE;
						if(this.executionTokenList.containsKey(token)){
							System.out.println("Error: "+ token +"  already used. The token should be unique." );
							response.sendError(HttpServletResponse.SC_BAD_REQUEST);
						}
					}
					if(hasToken){
						String executionId=	exec.getFlowUniqueExecutionID();
						if(executionId!=null){
						JobDetail jobDetail = new JobDetail();
						jobDetail.setToken(token);
						jobDetail.setFlowInstanceId(executionId);
						jobDetail.setHostname(getHostName());
						jobDetail.setPort(nextPort);
						this.executionTokenList.put(token, jobDetail);
						}
					}
					/**/
					// this is where the server actually starts -so populate the hashtable before
					// starting the server as that takes a bit of time
					WebUI webui = exec.initWebUI(nextPort,token);
					exec.execute(webui);
					pw.flush();

					if ( !exec.hadGracefullTermination() ) {
						//
						// Aborted execution.
						//
						pw.println("Execution aborted!!!\nReason:\n");
						for ( String sMsg:exec.getAbortMessage() )
							pw.println("\t"+sMsg);
					}
					pw.flush();
				}
				catch ( CorruptedDescriptionException cde ) {
					pw.println("Preparation could not be completed correctly!\n");
					pw.println("----------------------------------------------------------------------------");
					pw.println();
					pw.println("Reason for aborting the preparation:");
					pw.println();
					pw.println(cde);
					pw.flush();
				}
				catch ( ConductorException ce ) {
					pw.println("----------------------------------------------------------------------------");
					pw.print("Execution aborted at: ");
					pw.println(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date()));
					pw.println("----------------------------------------------------------------------------");
					pw.println();
					pw.println("Reason for aborting the execution:");
					pw.println();
					pw.println(ce);
					pw.flush();
				}

				// Reset the output redirection
				//System.setOut(psOUT);
				//System.setErr(psERR);
			}

		}
	}

	/** Returns the list of running flows.
	 *
	 * @param request The request object
	 * @param response The response object
	 * @return The JSON object containing the results
	 * @throws IOException Something went wrong
	 */
	public JSONObject listRunningFlowsAsJSON ( HttpServletRequest request, HttpServletResponse response ) throws IOException {
		JSONObject joRes = new JSONObject();
		JSONArray ja = new JSONArray();
		String sHostName = "http://"+request.getLocalName()+":";

		try {
			for ( String sID:WebUIFactory.getFlows() )  {
				WebUI webuiTmp = WebUIFactory.getExistingWebUI(sID);
				if ( webuiTmp==null )
					continue;
				JSONObject jo = new JSONObject();
				jo.put("flow_instance_uri", sID);
				jo.put("flow_instance_webui_uri", sHostName+webuiTmp.getPort()+"/");
				ja.put(jo);
			}
			joRes.put("meandre_running_flows",ja);

		}
		catch ( Exception e ) {
			throw new IOException(e.toString());
		}

		return joRes;
	}


	/** Returns the list of running flows.
	 *
	 * @param request The request object
	 * @param response The response object
	 * @return The JSON object containing the results
	 * @throws IOException Something went wrong
	 */
	public JSONObject listRunningFlowsAsTxt ( HttpServletRequest request, HttpServletResponse response ) throws IOException {
		JSONObject joRes = new JSONObject();
		JSONArray ja = new JSONArray();
		String sHostName = "http://"+request.getLocalName()+":";
		PrintWriter pw = response.getWriter();

		try {
			for ( String sID:WebUIFactory.getFlows() )  {
				WebUI webuiTmp = WebUIFactory.getExistingWebUI(sID);
				if ( webuiTmp==null )
					continue;
				JSONObject jo = new JSONObject();
				pw.println(sID);
				pw.println(sHostName+webuiTmp.getPort()+"/");
				ja.put(jo);
			}
			joRes.put("meandre_running_flows",ja);

		}
		catch ( Exception e ) {
			throw new IOException(e.toString());
		}

		return joRes;
	}

	/**Returns the flow url
	 *
	 * @param request
	 * @param response
	 * @throws IOException
	 * @throws WebUIException
	 */
	public void getFlowURL(HttpServletRequest request, HttpServletResponse response) throws IOException, WebUIException {
		String sURI = request.getParameter("uri");
		String sHostName = "http://"+request.getLocalName()+":";
		if ( sURI==null ) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		}else {
			WebUI webuiTmp = WebUIFactory.getExistingWebUI(sURI);
			PrintWriter pw = response.getWriter();
			if(webuiTmp!=null){
				String port=webuiTmp.getPort()+"";
				pw.println( sHostName+ port+"/" );
				pw.flush();
			}else{
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
			}

		}

	}

	/**Returns the list of component web urls.
	 *
	 * @param request
	 * @param response
	 * @throws IOException
	 * @throws WebUIException
	 */
	public void getWebComponentURLForFlow(HttpServletRequest request, HttpServletResponse response) throws IOException, WebUIException {
		String sURI = request.getParameter("uri");
		String sHostName = "http://"+request.getLocalName()+":";
		if ( sURI==null ) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		}else {
			WebUI webuiTmp = WebUIFactory.getExistingWebUI(sURI);

			PrintWriter pw = response.getWriter();
			if(webuiTmp!=null){
				String port = webuiTmp.getPort()+"";
				List<WebUIFragment> fragmentList=webuiTmp.getWebUIDispatcher().getLstHandlers();
				if(fragmentList!=null){
					Iterator<WebUIFragment> itw=fragmentList.iterator();
					while(itw.hasNext()){
					WebUIFragment wfrag = itw.next();
					pw.println(sHostName+port+"/"+wfrag.getFragmentID());
					}
				}
				pw.flush();
			}else{
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
			}

		}


	}

	/**For a given token submitted by the client initially upon the start of the flow
	 * return the flowInstanceID -this function is useful when the flow is web based
	 * flow.
	 *
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	public void getFlowURIFromToken(HttpServletRequest request, HttpServletResponse response)
	throws IOException{
		String token = request.getParameter("token");
		if(token==null){
			response.sendError(HttpServletResponse.SC_EXPECTATION_FAILED);
			return;
		}
		JobDetail jobdetail = this.executionTokenList.get(token);
		if(jobdetail==null){
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}else{
			PrintWriter pw = response.getWriter();
			pw.println("port="+ jobdetail.getPort());
			pw.println("hostname="+ jobdetail.getHostname());
			pw.println("token="+ jobdetail.getToken());
			pw.println("uri="+ jobdetail.getFlowInstanceId());
			pw.flush();
		}
	}

	/**return the host ip address
	 *
	 * @return The host name
	 */
	public String getHostName() {
		return NetworkTools.getLocalHostName();
	}

}
