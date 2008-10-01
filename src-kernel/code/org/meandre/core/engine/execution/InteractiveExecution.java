package org.meandre.core.engine.execution;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

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
import org.meandre.core.utils.Constants;
import org.meandre.core.utils.NetworkTools;
import org.meandre.webservices.beans.JobDetail;
import org.meandre.webservices.logger.WSLoggerFactory;
import org.meandre.webui.PortScroller;
import org.meandre.webui.WebUI;

import com.hp.hpl.jena.rdf.model.Resource;

/** This class provide simple execution of a flow on demand.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class InteractiveExecution {

	/** Executes the requested flow in verbose mode.
	 * 
	 * @param qr The query repository to use
	 * @param sURI The URI of the flow to execute
	 * @param outStream The output stream to use to output messages
	 * @param cnf The core configuration object
	 * @param bStats Should statistics be collected
	 * @param sToken The token to assign to the execution of the flow
	 * @param job The job detail information bean
	 * @throws IOException A problem was encountered when printing content to the output
	 * @throws CorruptedDescriptionException The flow could not be properly recovered
	 * @throws ConductorException An execution was thrown during the execution process
	 */
	public static void executeVerboseFlowURI( QueryableRepository qr, String sURI, OutputStream outStream, CoreConfiguration cnf, boolean bStats, String sToken, JobDetail job ) throws IOException,
			CorruptedDescriptionException, ConductorException {

		PrintStream pw = new PrintStream(outStream);
		
		pw.println("Meandre Execution Engine version "+Constants.MEANDRE_VERSION);
		pw.println("All rights reserved by DITA, NCSA, UofI (2007-2008)");
		pw.println("THIS SOFTWARE IS PROVIDED UNDER University of Illinois/NCSA OPEN SOURCE LICENSE.");
		pw.println();

		pw.flush();

		// Create the execution
		FlowDescription fd = qr.getFlowDescription(qr.getModel().createResource(sURI));
		Resource resURI = fd.getFlowComponent();
		pw.println("Preparing flow: "+sURI);
		Conductor conductor = new Conductor(Conductor.DEFAULT_QUEUE_SIZE,cnf);
		Executor exec =null;

		// Redirecting the output
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
			
			pw.println("Preparation completed correctly\n");

			pw.print("Execution started at: " + nextPort +" on ");
			pw.println(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date()));
			pw.println("----------------------------------------------------------------------------");
			pw.flush();
			WebUI webui = exec.initWebUI(nextPort,sToken);
			job.setToken(sToken);
			job.setFlowInstanceId(sURI);
			job.setHostname(NetworkTools.getLocalHostName());
			job.setPort(nextPort);
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
	
	}
	
	/** Executes the requested flow in silent mode.
	 * 
	 * @param qr The query repository to use
	 * @param sURI The URI of the flow to execute
	 * @param outStream The output stream to use to output messages
	 * @param cnf The core configuration object
	 * @param sToken The token to assign to the execution of the flow
	 * @param job The job detail information bean
	 * @throws IOException A problem was encountered when printing content to the output
	 * @throws CorruptedDescriptionException The flow could not be properly recovered
	 * @throws ConductorException An execution was thrown during the execution process
	 */
	public static void executeSilentFlowURI( QueryableRepository qr, String sURI, OutputStream outStream, CoreConfiguration cnf, String sToken, JobDetail job  ) throws IOException,
			CorruptedDescriptionException, ConductorException {

		PrintStream pw = new PrintStream(outStream);
		
		// Create the execution
		FlowDescription fd = qr.getFlowDescription(qr.getModel().createResource(sURI));
		Resource resURI = fd.getFlowComponent();
		Conductor conductor = new Conductor(Conductor.DEFAULT_QUEUE_SIZE,cnf);
		Executor exec =null;

		// Redirecting the output
		StatisticsProbeImpl spi = null;
		try {
			spi = new StatisticsProbeImpl();
			MrProbe mrProbe = new MrProbe(WSLoggerFactory.getWSLogger(),spi,false,false);
			exec = conductor.buildExecutor(qr, resURI, mrProbe,pw);
			mrProbe.setName(exec.getThreadGroupName()+"mr-probe");
		
			int nextPort = PortScroller.getInstance(cnf).nextAvailablePort(exec.getFlowUniqueExecutionID());
			
			WebUI webui = exec.initWebUI(nextPort,sToken);
			job.setToken(sToken);
			job.setFlowInstanceId(sURI);
			job.setHostname(NetworkTools.getLocalHostName());
			job.setPort(nextPort);
			exec.execute(webui);
			if ( !exec.hadGracefullTermination() ) {
				//
				// Aborted execution.
				//
				pw.println("----------------------------------------------------------------------------");
				pw.println("Execution aborted!!!\nReason:\n");
				for ( String sMsg:exec.getAbortMessage() )
					pw.println("\t"+sMsg);
			}
			pw.flush();

		}
		catch ( CorruptedDescriptionException cde ) {
			pw.println("----------------------------------------------------------------------------");
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
	}
	
}
