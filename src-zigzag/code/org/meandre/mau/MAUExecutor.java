package org.meandre.mau;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Handler;
import java.util.logging.Level;

import org.json.JSONArray;
import org.json.JSONObject;
import org.meandre.configuration.CoreConfiguration;
import org.meandre.core.engine.Conductor;
import org.meandre.core.engine.ConductorException;
import org.meandre.core.engine.Executor;
import org.meandre.core.engine.MrProbe;
import org.meandre.core.engine.ProbeException;
import org.meandre.core.engine.probes.StatisticsProbeImpl;
import org.meandre.core.logger.KernelLoggerFactory;
import org.meandre.core.repository.CorruptedDescriptionException;
import org.meandre.core.repository.ExecutableComponentDescription;
import org.meandre.core.repository.QueryableRepository;
import org.meandre.core.repository.RepositoryImpl;
import org.meandre.core.utils.Constants;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * This class runs Meandre's MAU files.
 * 
 * @author Xavier Llor&agrave;
 * 
 */
public class MAUExecutor {

	/** The current version of the MAU executor */
	private static final String ZMAU_VERSION = "1.0.1vcli";

	/** The output print stream to use */
	protected PrintStream ps;
	
	/** The executor object */
	protected Executor exec;
	
	/** The statistics probe object */
	protected StatisticsProbeImpl spi;

	/** The filename to execute */
	private String sFileName;
	
	/** The main method that runs the the MAU file.
	 * 
	 * @param sArgs The command line arguments
	 * @throws FileNotFoundException The file could not be found
	 */
	public static void main(String sArgs[]) throws FileNotFoundException {
		// Tone down the logger
		KernelLoggerFactory.getCoreLogger().setLevel(Level.WARNING);
		for ( Handler h:KernelLoggerFactory.getCoreLogger().getHandlers() )
			h.setLevel(Level.WARNING);
		
		if ( sArgs.length!=1 ) {
			System.err.println("Wrong syntax!!!\nThe MAU executor requires one .mau file");
		}
		else  {
			MAUExecutor mau = new MAUExecutor(sArgs[0]);
			mau.run();
		}
	}
	
	/** Creates a new MAU execution object for the given filename.
	 * 
	 * @param sFileName The file name
	 */
	public MAUExecutor ( String sFileName ) {
		ps = System.out;
		this.sFileName = sFileName;
	}
	
	/** Set the output stream to use.
	 * 
	 * @param os The output stream
	 */
	public void setOutpuStream ( OutputStream os ) {
		ps  = new PrintStream(os);
	}
	
	/** Get the abort messages if any
	 * 
	 * @return The abort messages
	 */
	public Set<String> getAbortMessages () {
		if ( exec!=null )
			return exec.getAbortMessage();
		else 
			return new HashSet<String>();
	}
	
	/** Returns the statistics for the MAU run using a JSONObject.
	 * 
	 * @return The JSONObject containing the statistics
	 * @throws ProbeException The statistics could not be retrieved
	 */
	public JSONObject getStatistics () throws ProbeException {
		return spi.getSerializedStatistics();
	}
	
	/** Returns the termination status for the execution
	 * 
	 * @return True if the flow finished without errors, false otherwise
	 */
	public boolean hadGracefullTermination() {
		if ( exec!=null )
			return exec.hadGracefullTermination();
		else
			return false;
	}
		
	/** Runs the MAU file.
	 * 
	 * @throws FileNotFoundException The file could not be found
	 */ 
	public void run () throws FileNotFoundException {
		
		ps.println("Meandre MAU Executor [" + MAUExecutor.ZMAU_VERSION + "/" + Constants.MEANDRE_VERSION + "]");
		ps.println("All rigths reserved by DITA, NCSA, UofI (2007-2008).");
		ps.println("THIS SOFTWARE IS PROVIDED UNDER University of Illinois/NCSA OPEN SOURCE LICENSE.");
		ps.println();
		ps.flush();

		ps.println("Executing MAU file " + sFileName);
		ps.println();
		
		QueryableRepository qr = processModelFromMAU();
		
		Resource resURI = qr.getAvailableFlows().iterator().next();
		ps.println("Preparing flow: "+resURI);
		CoreConfiguration cnf = new CoreConfiguration();
		Conductor conductor = new Conductor(Conductor.DEFAULT_QUEUE_SIZE,cnf);
		
		exec =null;
		spi = null;
		
		
		try {
			spi = new StatisticsProbeImpl();
			MrProbe mrProbe = new MrProbe(KernelLoggerFactory.getCoreLogger(),spi,false,false);
			exec = conductor.buildExecutor(qr, resURI, mrProbe);
			
			ps.flush();
			
			// Redirecting the streamers
			System.setOut(ps);
			System.setErr(ps);

			ps.println("Preparation completed correctly\n");
			
			ps.print("Execution started at: ");
			ps.println(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date()));
			ps.println("----------------------------------------------------------------------------");
			ps.flush();
			exec.execute();
			ps.flush();
			ps.println("----------------------------------------------------------------------------");
			ps.print("Execution finished at: ");
			ps.println(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date()));
			if ( exec.hadGracefullTermination() ) {
				//
				// Graceful termination
				//
				ps.println("Execution finished gracefully.");
			}
			else {
				//
				// Aborted execution.
				//
				ps.println("Execution aborted!!!\nReason:\n");
				for ( String sMsg:exec.getAbortMessage() )
					ps.println("\t"+sMsg);
			}
			ps.flush();
		
		}
		catch ( CorruptedDescriptionException cde ) {
			ps.println("Preparation could not be completed correctly!\n");
			ps.println("----------------------------------------------------------------------------");
			ps.println();
			ps.println("Reason for aborting the preparation:");
			ps.println();
			ps.println(cde);
			ps.flush();
		}
		catch ( ConductorException ce ) {
			ps.println("----------------------------------------------------------------------------");
			ps.print("Execution aborted at: ");
			ps.println(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date()));
			ps.println("----------------------------------------------------------------------------");
			ps.println();
			ps.println("Reason for aborting the execution:");
			ps.println();
			ps.println(ce);
			ps.flush();
		}
		catch ( NoClassDefFoundError te ) {
			ps.println("----------------------------------------------------------------------------");
			ps.print("Missing class definition: ");
			ps.println(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date()));
			ps.println("----------------------------------------------------------------------------");
			ps.println();
			ps.println("Reason for aborting the execution:");
			ps.println();
			ps.println(te);
			ps.flush();
		}
		
		printStatistics();
		
	}

	/** Process the model contained on the MAU file and rearrenge the contexts URIs.
	 * 
	 * @return The edited model
	 * @throws FileNotFoundException The file could not be retrieved
	 */
	protected QueryableRepository processModelFromMAU() throws FileNotFoundException {
		try {
			// Extract the repository description
			Model mod = ModelFactory.createDefaultModel();
//			File file = new File(".");
//			URL url = new URL("jar:file://"+file.getAbsolutePath()+"/"+sFileName+"!/repository/repository.ttl");
			File file = new File(sFileName);
			URL url = new URL("jar:file://"+sFileName+"!/repository/repository.ttl");
			mod.read(url.openStream(), null,"TTL");
			QueryableRepository qr = new RepositoryImpl(mod);
			
			// Edit the contexts URI
			JarFile jar = new JarFile(sFileName);
			Enumeration<JarEntry> iterJE = jar.entries();
			while (iterJE.hasMoreElements()) {
				JarEntry je = iterJE.nextElement();
				//System.out.println(je.getName());
				String [] sa = je.getName().split("/");
//				editContextJarURI(qr,sa[sa.length-1],"jar:file://"+file.getAbsolutePath()+"/"+sFileName+"!"+je.getName());
				editContextJarURI(qr,sa[sa.length-1],"jar:file://"+sFileName+"!"+je.getName());
			}
			return qr;
		} catch (MalformedURLException e) {
			throw new FileNotFoundException(e.toString());
		} catch (IOException e) {
			throw new FileNotFoundException(e.toString());
		}
		
	}

	/** Edit the context URI to point to the ones contained in the jar.
	 * 
	 * @param qr The queryable repository to edit
	 * @param sJarName The name of the jar
	 * @param sNewURI The new URI to set
	 */
	private void editContextJarURI(QueryableRepository qr, String sJarName, String sNewURI) {
		for ( ExecutableComponentDescription ecd:qr.getAvailableExecutableComponentDescriptions() ) {
			Set<RDFNode> setNew = new HashSet<RDFNode>();
			for ( RDFNode rdfNode:ecd.getContext() ) 
				if ( rdfNode.isResource() &&
					 rdfNode.toString().endsWith(sJarName) ) {
					setNew.add(qr.getModel().createResource(sNewURI));
				}
				else 
					setNew.add(rdfNode);
			}
		
		
	}

	/** Print the output statistics to the output stream.
	 * 
	 */
	protected void printStatistics() {
		try {
			JSONObject jsonStats = spi.getSerializedStatistics();
			ps.println("----------------------------------------------------------------------------");
			ps.println();
			ps.println("Flow execution statistics");
			ps.println();
			ps.println("Flow unique execution ID : "+jsonStats.get("flow_unique_id"));
			ps.println("Flow state               : "+jsonStats.get("flow_state"));
			ps.println("Started at               : "+jsonStats.get("started_at"));
			ps.println("Last update              : "+jsonStats.get("latest_probe_at"));
			ps.println("Total run time (ms)      : "+jsonStats.get("runtime"));
			ps.println();
			ps.flush();
			
			JSONArray jaEXIS = (JSONArray) jsonStats.get("executable_components_statistics");
			for ( int i=0,iMax=jaEXIS.length() ; i<iMax ; i++ ) {
				JSONObject joEXIS = (JSONObject) jaEXIS.get(i);
				ps.println("\tExecutable components instance ID          : "+joEXIS.get("executable_component_instance_id"));
				ps.println("\tExecutable components state                : "+joEXIS.get("executable_component_state"));
				ps.println("\tTimes the executable components fired      : "+joEXIS.get("times_fired"));
				ps.println("\tAccumulated executable components run time : "+joEXIS.get("accumulated_runtime"));
				ps.println("\tPieces of data pulled                      : "+joEXIS.get("pieces_of_data_in"));
				ps.println("\tPieces of data pushed                      : "+joEXIS.get("pieces_of_data_out"));
				ps.println("\tNumber of properties read                  : "+joEXIS.get("number_of_read_properties"));
				ps.println();
			}
			ps.flush();
		}
		catch ( Exception e ) {
			KernelLoggerFactory.getCoreLogger().warning("This exception should have never been thrown\n"+e);
		}
	}
}
