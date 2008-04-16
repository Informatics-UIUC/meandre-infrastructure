package org.meandre.mau;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.Handler;

import org.json.JSONArray;
import org.json.JSONObject;
import org.meandre.core.engine.Conductor;
import org.meandre.core.engine.ConductorException;
import org.meandre.core.engine.Executor;
import org.meandre.core.engine.MrProbe;
import org.meandre.core.engine.probes.StatisticsProbeImpl;
import org.meandre.core.logger.KernelLoggerFactory;
import org.meandre.core.store.repository.CorruptedDescriptionException;
import org.meandre.core.store.repository.QueryableRepository;
import org.meandre.core.store.repository.RepositoryImpl;
import org.meandre.core.utils.Constants;
import org.meandre.webservices.utils.WSLoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * This class runs Meandre's MAU files.
 * 
 * @author Xavier Llor&agrave;
 * 
 */
public class MAUExecutor {

	private static final String ZMAU_VERSION = "1.0.0vcli";

	public static void main(String args[]) throws FileNotFoundException {
		
		// Tone down the logger
		KernelLoggerFactory.getCoreLogger().setLevel(Level.WARNING);
		for ( Handler h:KernelLoggerFactory.getCoreLogger().getHandlers() )
			h.setLevel(Level.WARNING);
		
		if ( args.length!=1 ) {
			System.err.println("Wrong syntax!!!\nThe MAU executor requires one .mau file");
		}
		else {
			String sFileName = args[0];
	
			System.out.println("Meandre MAU Executor [" + MAUExecutor.ZMAU_VERSION + "/" + Constants.MEANDRE_VERSION + "]");
			System.out.println("All rigths reserved by DITA, NCSA, UofI (2007-2008).");
			System.out.println("THIS SOFTWARE IS PROVIDED UNDER University of Illinois/NCSA OPEN SOURCE LICENSE.");
			System.out.println();
			System.out.flush();
	
			System.out.println("Executin MAU file " + sFileName);
			System.out.println();
	
			Model mod = ModelFactory.createDefaultModel();
			mod.read(new FileInputStream(sFileName), null,"TTL");
			QueryableRepository qr = new RepositoryImpl(mod);
			
			Resource resURI = qr.getAvailableFlows().iterator().next();
			System.out.println("Preparing flow: "+resURI);
			Conductor conductor = new Conductor(Conductor.DEFAULT_QUEUE_SIZE);
			Executor exec =null;
			
			StatisticsProbeImpl spi = null;
			
			try {
				spi = new StatisticsProbeImpl();
				MrProbe mrProbe = new MrProbe(WSLoggerFactory.getWSLogger(),spi,false,false);
				exec = conductor.buildExecutor(qr, resURI, mrProbe);
				
				System.out.flush();
				
				// Redirecting the streamers
				System.setOut(System.out);
				System.setErr(System.out);
	
				System.out.println("Preparation completed correctly\n");
				
				System.out.print("Execution started at: ");
				System.out.println(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date()));
				System.out.println("----------------------------------------------------------------------------");
				System.out.flush();
				exec.execute();
				System.out.flush();
				System.out.println("----------------------------------------------------------------------------");
				System.out.print("Execution finished at: ");
				System.out.println(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date()));
				if ( exec.hadGracefullTermination() ) {
					//
					// Graceful termination
					//
					System.out.println("Execution finished gracefully.");
				}
				else {
					//
					// Aborted execution.
					//
					System.out.println("Execution aborted!!!\nReason:\n");
					for ( String sMsg:exec.getAbortMessage() )
						System.out.println("\t"+sMsg);
				}
				System.out.flush();
			
			}
			catch ( CorruptedDescriptionException cde ) {
				System.out.println("Preparation could not be completed correctly!\n");
				System.out.println("----------------------------------------------------------------------------");
				System.out.println();
				System.out.println("Reason for aborting the preparation:");
				System.out.println();
				System.out.println(cde);
				System.out.flush();
			}
			catch ( ConductorException ce ) {
				System.out.println("----------------------------------------------------------------------------");
				System.out.print("Execution aborted at: ");
				System.out.println(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date()));
				System.out.println("----------------------------------------------------------------------------");
				System.out.println();
				System.out.println("Reason for aborting the execution:");
				System.out.println();
				System.out.println(ce);
				System.out.flush();
			}
			catch ( NoClassDefFoundError te ) {
				System.out.println("----------------------------------------------------------------------------");
				System.out.print("Missing class definition: ");
				System.out.println(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date()));
				System.out.println("----------------------------------------------------------------------------");
				System.out.println();
				System.out.println("Reason for aborting the execution:");
				System.out.println();
				System.out.println(te);
				System.out.flush();
			}
			
			try {
				JSONObject jsonStats = spi.getSerializedStatistics();
				System.out.println("----------------------------------------------------------------------------");
				System.out.println();
				System.out.println("Flow execution statistics");
				System.out.println();
				System.out.println("Flow unique execution ID : "+jsonStats.get("flow_unique_id"));
				System.out.println("Flow state               : "+jsonStats.get("flow_state"));
				System.out.println("Started at               : "+jsonStats.get("started_at"));
				System.out.println("Last update              : "+jsonStats.get("latest_probe_at"));
				System.out.println("Total run time (ms)      : "+jsonStats.get("runtime"));
				System.out.println();
				System.out.flush();
				
				JSONArray jaEXIS = (JSONArray) jsonStats.get("executable_components_statistics");
				for ( int i=0,iMax=jaEXIS.length() ; i<iMax ; i++ ) {
					JSONObject joEXIS = (JSONObject) jaEXIS.get(i);
					System.out.println("\tExecutable components instance ID          : "+joEXIS.get("executable_component_instance_id"));
					System.out.println("\tExecutable components state                : "+joEXIS.get("executable_component_state"));
					System.out.println("\tTimes the executable components fired      : "+joEXIS.get("times_fired"));
					System.out.println("\tAccumulated executable components run time : "+joEXIS.get("accumulated_runtime"));
					System.out.println("\tPieces of data pulled                      : "+joEXIS.get("pieces_of_data_in"));
					System.out.println("\tPieces of data pushed                      : "+joEXIS.get("pieces_of_data_out"));
					System.out.println("\tNumber of properties read                  : "+joEXIS.get("number_of_read_properties"));
					System.out.println();
				}
				System.out.flush();
			}
			catch ( Exception e ) {
				WSLoggerFactory.getWSLogger().warning("This exception should have never been thrown\n"+e);
			}
		}
		
	}
}
