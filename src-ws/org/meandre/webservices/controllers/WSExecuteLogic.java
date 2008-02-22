package org.meandre.webservices.controllers;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.meandre.core.engine.Conductor;
import org.meandre.core.engine.ConductorException;
import org.meandre.core.engine.Executor;
import org.meandre.core.engine.MrProbe;
import org.meandre.core.engine.probes.StatisticsProbeImpl;
import org.meandre.core.store.Store;
import org.meandre.core.store.repository.CorruptedDescriptionException;
import org.meandre.core.store.repository.FlowDescription;
import org.meandre.core.store.repository.QueryableRepository;
import org.meandre.core.utils.Constants;
import org.meandre.webservices.utils.WSLoggerFactory;
import org.meandre.webui.WebUI;
import org.meandre.webui.WebUIFactory;

import com.hp.hpl.jena.rdf.model.Resource;

/** This class wraps the basic logic to execute flows.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class WSExecuteLogic {


	/** Runs a flow given a URI against the user repository verbosely
	 * 
	 * @param request The request object
	 * @param response The response object
	 * @throws IOException An exception arised while printing 
	 * @throws CorruptedDescriptionException
	 * @throws ConductorException
	 */
	public static void executeVerboseFlowURI(HttpServletRequest request,
			HttpServletResponse response) throws IOException,
			CorruptedDescriptionException, ConductorException {
		String sURI = request.getParameter("uri");
		boolean bStats = false;
		String sStats = request.getParameter("statistics");
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
			QueryableRepository qr = Store.getRepositoryStore(request.getRemoteUser());
			Resource resURI = qr.getModel().createResource(sURI);
			FlowDescription fd = qr.getFlowDescription(resURI);
			if ( fd==null ) {
				//
				// Unknow flow
				//
				response.sendError(HttpServletResponse.SC_EXPECTATION_FAILED);
			}
			else {
				//
				// Executing the flow
				//
				
				response.setStatus(HttpServletResponse.SC_OK);
				OutputStream outStream = response.getOutputStream();
				PrintStream pw = new PrintStream(outStream);
				
				pw.println("Meandre Execution Engine version "+Constants.MEANDRE_VERSION);
				pw.println("All rigths reserved by DITA, NCSA, UofI (2007).");
				pw.println("2007. All rigths reserved by DITA, NCSA, UofI.");
				pw.println("THIS SOFTWARE IS PROVIDED UNDER University of Illinois/NCSA OPEN SOURCE LICENSE.");
				pw.println();

				pw.flush();
				
				// Create the execution
				pw.println("Preparing flow: "+sURI);
				Conductor conductor = new Conductor(Conductor.DEFAULT_QUEUE_SIZE);
				Executor exec =null;
				
				// Redirecting the output
				PrintStream psOUT = System.out;
				PrintStream psERR = System.err;
				StatisticsProbeImpl spi = null;
				
				try {
					if ( !bStats )
						exec = conductor.buildExecutor(qr, resURI);
					else {
						spi = new StatisticsProbeImpl();
						MrProbe mrProbe = new MrProbe(WSLoggerFactory.getWSLogger(),spi,false,false);
						exec = conductor.buildExecutor(qr, resURI, mrProbe);
					}
					
					pw.flush();
					
					
					// Redirecting the streamers
					System.setOut(pw);
					System.setErr(pw);
	
					pw.println("Preparation completed correctly\n");
					
					pw.print("Execution started at: ");
					pw.println(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date()));
					pw.println("----------------------------------------------------------------------------");
					pw.flush();
					exec.execute();
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
				}
				
				if ( bStats ) {
					try {
						JSONObject jsonStats = spi.getSerializedStatistics();
						pw.println("----------------------------------------------------------------------------");
						pw.println();
						pw.println("Flow execution statistics");
						pw.println();
						pw.println("Flow unique execution ID :"+jsonStats.get("flow_unique_id"));
						pw.println("Flow state               :"+jsonStats.get("flow_state"));
						pw.println("Started at               :"+jsonStats.get("started_at"));
						pw.println("Last update              :"+jsonStats.get("latest_probe_at"));
						pw.println("Total run time (ms)      :"+jsonStats.get("runtime"));
						pw.println();
						pw.flush();
						
						JSONArray jaEXIS = (JSONArray) jsonStats.get("executable_components_statistics");
						for ( int i=0,iMax=jaEXIS.length() ; i<iMax ; i++ ) {
							JSONObject joEXIS = (JSONObject) jaEXIS.get(i);
							pw.println("\tExecutable components instance ID          :"+joEXIS.get("executable_component_instance_id"));
							pw.println("\tExecutable components state                :"+joEXIS.get("executable_component_state"));
							pw.println("\tTimes the executable components fired      :"+joEXIS.get("times_fired"));
							pw.println("\tAccumulated executable components run time :"+joEXIS.get("accumulated_runtime"));
							pw.println("\tPieces of data pulled                      :"+joEXIS.get("pieces_of_data_in"));
							pw.println("\tPieces of data pushed                      :"+joEXIS.get("pieces_of_data_out"));
							pw.println("\tNumber of properties read                  :"+joEXIS.get("number_of_read_properties"));
							pw.println();
						}
						pw.flush();
					}
					catch ( Exception e ) {
						WSLoggerFactory.getWSLogger().warning("This exception should have never been thrown\n"+e);
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
	public static void executeSilentFlowURI(HttpServletRequest request,
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
			QueryableRepository qr = Store.getRepositoryStore(request.getRemoteUser());
			Resource resURI = qr.getModel().createResource(sURI);
			FlowDescription fd = qr.getFlowDescription(resURI);
			if ( fd==null ) {
				//
				// Unknow flow
				//
				response.sendError(HttpServletResponse.SC_EXPECTATION_FAILED);
			}
			else {
				//
				// Executing the flow
				//
				
				response.setStatus(HttpServletResponse.SC_OK);
				OutputStream outStream = response.getOutputStream();
				PrintStream pw = new PrintStream(outStream);
				
				// Create the execution
				Conductor conductor = new Conductor(Conductor.DEFAULT_QUEUE_SIZE);
				Executor exec = null;
				
				// Redirecting the output
				PrintStream psOUT = System.out;
				PrintStream psERR = System.err;
				
				try {
					exec = conductor.buildExecutor(qr, resURI);
					
					// Redirecting the streamers
					System.setOut(pw);
					System.setErr(pw);
		
					pw.flush();
					exec.execute();
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
				System.setOut(psOUT);
				System.setErr(psERR);
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
	public static JSONObject listRunningFlowsAsJSON ( HttpServletRequest request, HttpServletResponse response ) throws IOException {
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
	public static JSONObject listRunningFlowsAsTxt ( HttpServletRequest request, HttpServletResponse response ) throws IOException {
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

}
