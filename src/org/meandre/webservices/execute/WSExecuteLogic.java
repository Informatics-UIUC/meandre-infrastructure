package org.meandre.webservices.execute;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.meandre.WSCoreBootstrapper;
import org.meandre.core.engine.Conductor;
import org.meandre.core.engine.ConductorException;
import org.meandre.core.engine.Executor;
import org.meandre.core.store.Store;
import org.meandre.core.store.repository.CorruptedDescriptionException;
import org.meandre.core.store.repository.FlowDescription;
import org.meandre.core.store.repository.QueryableRepository;

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
				
				pw.println("Meandre Execution Engine version "+WSCoreBootstrapper.VERSION);
				pw.println("All rigths reserved by DITA, NCSA, UofI (2007).");
				pw.println("2007. All rigths reserved by DITA, NCSA, UofI.");
				pw.println("THIS SOFTWARE IS PROVIDED UNDER University of Illinois/NCSA OPEN SOURCE LICENSE.");
				pw.println();

				
				// Create the execution
				pw.println("Preparing flow: "+sURI);
				Conductor conductor = new Conductor(Conductor.DEFAULT_QUEUE_SIZE);
				Executor exec = conductor.buildExecutor(qr, resURI);
				
				// Redirecting the output
				PrintStream psOUT = System.out;
				PrintStream psERR = System.err;

				// Redirecting the streamers
				System.setOut(pw);
				System.setErr(pw);

				pw.println("Preparation completed correctly\n");
				
				pw.print("Execution started at: ");
				pw.println(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date()));
				pw.println("----------------------------------------------------------------------------");
				exec.execute();
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
				Executor exec = conductor.buildExecutor(qr, resURI);
				
				// Redirecting the output
				PrintStream psOUT = System.out;
				PrintStream psERR = System.err;
	
				// Redirecting the streamers
				System.setOut(pw);
				System.setErr(pw);
	
				exec.execute();
				
				if ( !exec.hadGracefullTermination() ) {
					//
					// Aborted execution.
					//
					pw.println("Execution aborted!!!\nReason:\n");
					for ( String sMsg:exec.getAbortMessage() )
						pw.println("\t"+sMsg);
				}
	
				// Reset the output redirection
				System.setOut(psOUT);
				System.setErr(psERR);
			}
			
		}
	}

}
