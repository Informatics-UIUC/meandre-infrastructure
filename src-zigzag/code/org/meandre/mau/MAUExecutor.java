package org.meandre.mau;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Handler;
import java.util.logging.Level;

import org.json.JSONArray;
import org.json.JSONException;
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
import org.meandre.core.utils.ModelIO;
import org.meandre.core.utils.Version;
import org.meandre.webui.PortScroller;
import org.meandre.webui.WebUI;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;
import com.martiansoftware.jsap.SimpleJSAP;
import com.martiansoftware.jsap.UnflaggedOption;

/**
 * This class runs Meandre's MAU files.
 *
 * @author Xavier Llor&agrave;
 *
 */
public class MAUExecutor {

	/** The current version of the MAU executor */
	private static final String ZMAU_VERSION = "1.0.2vcli";

	/** The run temp dir */
	private static final String MAU_RUN_DIR = "run";

	/** Public resources */
	private static final String MAU_PUBLIC_RESOURCES_RUN_DIR = "public_resources";

	/** The output print stream to use */
	protected PrintStream ps;

	/** The executor object */
	protected Executor exec;

	/** The statistics probe object */
	protected StatisticsProbeImpl spi;

	/** The filename to execute */
	private final String sFileName;

	/** The parent class loader */
	private ClassLoader parentClassloader;

	/** The port number to use */
	private int iPort;

	private String config;

	/** The main method that runs the the MAU file.
	 *
	 * @param sArgs The command line arguments
	 * @throws FileNotFoundException The file could not be found
	 */
	public static void main(String sArgs[]) throws Exception {
		// Tone down the logger
		KernelLoggerFactory.getCoreLogger().setLevel(Level.WARNING);
		for ( Handler h:KernelLoggerFactory.getCoreLogger().getHandlers() )
			h.setLevel(Level.WARNING);

        // Parse command line arguments
        JSAPResult jsapResult = parseArguments(sArgs);

        // Extract the argument values
        String mauFile = jsapResult.getString("mau");
        int port = jsapResult.getInt("port");
        String config = jsapResult.contains("config") ? jsapResult.getString("config") : null;

        String[] params = jsapResult.getStringArray("param");

        MAUExecutor mau = new MAUExecutor(mauFile);
        mau.setParentClassloader(MAUExecutor.class.getClassLoader());
        mau.setWebUIPortNumber(port);
        if (config != null)
            mau.setConfigFile(config);

        Properties flowParams = new Properties();
        for (String param : params) {
        	String key = param.substring(0, param.indexOf('='));
        	String value = param.substring(key.length() + 1);
        	flowParams.put(key, value);
        }

        mau.run(flowParams);
	}

	private void setConfigFile(String config) {
        this.config = config;
    }

    /** Creates a new MAU execution object for the given filename.
	 *
	 * @param sFileName The file name
	 */
	public MAUExecutor ( String sFileName ) {
		ps = System.out;
		this.sFileName = sFileName;
		this.iPort = 1715;
	}

	/** Set the WebUI port number to use.
	 *
	 * @param iPort The port number
	 */
	public void setWebUIPortNumber(int iPort) {
		this.iPort = iPort;
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
		try {
			return new JSONObject(spi.serializeProbeInformation());
		} catch (JSONException e) {
			throw new ProbeException(e);
		}
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
	public void run (Properties flowParams) throws Exception {
		ps.println("Meandre MAU Executor [" + MAUExecutor.ZMAU_VERSION + "/" + Version.getFullVersion() + "]");
		ps.println("All rights reserved by DITA, NCSA, UofI (2007-2011)");
		ps.println("THIS SOFTWARE IS PROVIDED UNDER University of Illinois/NCSA OPEN SOURCE LICENSE.");
		ps.println();
		ps.flush();

		ps.println("Executing MAU file " + sFileName);
		File fTmp = new File(sFileName+"."+MAU_RUN_DIR);
		File fTmpPR = new File(sFileName+"."+MAU_PUBLIC_RESOURCES_RUN_DIR);
		ps.println("Creating temp dir " + fTmp.toString());
		ps.println("Creating temp dir " + fTmpPR.toString());
		fTmp.mkdir();
		ps.println();

		QueryableRepository qr = processModelFromMAU();

		Resource resURI = qr.getAvailableFlows().iterator().next();
		ps.println("Preparing flow: "+resURI);

	    Properties props = new Properties();
		if (config != null) {
		    props.loadFromXML(new FileInputStream(config));
	        props.setProperty(CoreConfiguration.MEANDRE_CORE_CONFIG_FILE, config);
		} else {
    		props.setProperty(CoreConfiguration.MEANDRE_CORE_CONFIG_FILE, "meandre-config-core.xml");
		}
        props.setProperty(CoreConfiguration.MEANDRE_BASE_PORT, ""+(iPort-1));
        props.setProperty(CoreConfiguration.MEANDRE_HOME_DIRECTORY, ".");
        props.setProperty(CoreConfiguration.MEANDRE_PRIVATE_RUN_DIRECTORY, fTmp.toString());
        props.setProperty(CoreConfiguration.MEANDRE_PUBLIC_RESOURCE_DIRECTORY, fTmpPR.toString());

		CoreConfiguration cnf = new CoreConfiguration(props);
		cnf.initializeLogging();

		Conductor conductor = new Conductor(cnf.getConductorDefaultQueueSize(),cnf);

		exec =null;
		spi = null;


		try {
			spi = new StatisticsProbeImpl();
			spi.initialize();
			MrProbe mrProbe = new MrProbe(KernelLoggerFactory.getCoreLogger(),spi,false,false);
			conductor.setParentClassloader(this.getParentClassloader());
			exec = conductor.buildExecutor(qr, resURI, mrProbe, System.out, flowParams);
			mrProbe.setName(exec.getThreadGroupName()+"mr-probe");

			ps.flush();

			// Redirecting the streamers
			System.setOut(ps);
			System.setErr(ps);

			ps.println("Preparation completed correctly\n");

			ps.print("Execution started at: ");
			ps.println(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date()));
			ps.println("----------------------------------------------------------------------------");
			ps.flush();
			int nextPort = PortScroller.getInstance(cnf).nextAvailablePort(exec.getFlowUniqueExecutionID());
			String token=System.currentTimeMillis()+"";
			WebUI webui=exec.initWebUI(nextPort,token);
			exec.execute(webui);
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
		catch (CorruptedDescriptionException cde) {
            ps.println("Preparation could not be completed correctly!\n");
            ps.println("----------------------------------------------------------------------------");
            ps.println();
            ps.println("Reason for aborting the preparation:");
            ps.println();
            ps.println(cde);
            ps.flush();
            return;
        }
        catch (ConductorException ce) {
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
        catch (NoClassDefFoundError te) {
            ps.println("----------------------------------------------------------------------------");
            ps.print("Missing class definition: ");
            ps.println(te.getMessage());
            ps.println("----------------------------------------------------------------------------");
            ps.println();
            ps.println("Exception details:");
            ps.println();
            ps.println(te);
            ps.flush();
            return;
        }
        catch (Throwable t) {
            if (exec != null) exec.requestAbort();
            ps.println("----------------------------------------------------------------------------");
            ps.print("Unknown exception at: ");
            ps.println(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date()));
            ps.println("----------------------------------------------------------------------------");
            ps.println();
            t.printStackTrace(ps);
            ps.println();
            ps.flush();
        }

		printStatistics();

		// Cleaning the tmp run dir
		ps.println("Cleaning temp dir " + fTmp.toString());
		deleteDir(fTmp);
		ps.println("Cleaning temp dir " + fTmpPR.toString());
		deleteDir(fTmpPR);
		ps.println();

	}

	/**  Deletes all files and subdirectories under dir.
     *   Returns true if all deletions were successful.
     *   If a deletion fails, the method stops attempting to delete and returns false.
     *
     * @param dir The directory to delete
     * @return True if it was properly cleaned, false otherwise
     */
	 private boolean deleteDir(File dir) {
	    if (dir.isDirectory()) {
	         String[] children = dir.list();
	         for (int i=0; i<children.length; i++) {
	             boolean success = deleteDir(new File(dir, children[i]));
	             if (!success)
	                 return false;
	         }
	     }
	    // The directory is now empty so delete it
        if (dir.exists())
	        return dir.delete();
        else
        	return true;

	 }

	private final HashSet<String> setProcessedJars = new HashSet<String>();

	/** Process the model contained on the MAU file and rearrenge the contexts URIs.
	 *
	 * @return The edited model
	 * @throws FileNotFoundException The file could not be retrieved
	 */
	protected QueryableRepository processModelFromMAU() throws FileNotFoundException {
		try {
			// Extract the repository description
			Model mod = ModelFactory.createDefaultModel();
			File file = new File(sFileName);
			URL url = new URL("jar:file:"+file.getAbsolutePath()+"!/repository/repository.ttl");
			ModelIO.readModelInDialect(mod, url);
			QueryableRepository qr = new RepositoryImpl(mod);

			// Edit the contexts URI
			JarFile jar = new JarFile(sFileName);
			Enumeration<JarEntry> iterJE = jar.entries();
			setProcessedJars.clear();
			while (iterJE.hasMoreElements()) {
				JarEntry je = iterJE.nextElement();
				String [] sa = je.getName().split("/");
				editContextJarURI(qr,sa[sa.length-1],"jar:file:"+file.getAbsolutePath()+"!/contexts/"+sa[sa.length-1].trim());
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
		String sPrefix = sFileName+"."+MAU_PUBLIC_RESOURCES_RUN_DIR+File.separator+"contexts"+File.separator+"java";
		new File(sPrefix).mkdirs();

		for ( ExecutableComponentDescription ecd:qr.getAvailableExecutableComponentDescriptions() ) {
			Set<RDFNode> setNew = new HashSet<RDFNode>();
			for ( RDFNode rdfNode:ecd.getContext() ) {
				if ( rdfNode.isResource() &&
					 rdfNode.toString().endsWith(sJarName) ) {
					try {
						InputStream is = new URL(sNewURI).openStream();
						File fo = new File(sPrefix+File.separator+sJarName);
						if ( !setProcessedJars.contains(fo.toString()) ) {
							FileOutputStream fos = new FileOutputStream(fo);
							byte [] baTmp = new byte[1048576];
							int iNumBytes = 0;
							while ( (iNumBytes=is.read(baTmp))>-1 )
								fos.write(baTmp, 0, iNumBytes);
							fos.close();
							is.close();
							setProcessedJars.add(fo.toString());
						}
						setNew.add(qr.getModel().createResource("file:"+fo.getAbsolutePath()));
					} catch (MalformedURLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
				else
					setNew.add(rdfNode);
			}
			Set<RDFNode> set = ecd.getContext();
			set.clear();
			set.addAll(setNew);
		}


	}

	/** Print the output statistics to the output stream.
	 *
	 */
	protected void printStatistics() {
		try {
			JSONObject jsonStats = new JSONObject(spi.serializeProbeInformation());
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
		spi.dispose();
	}



	/**
	 * @return the parentClassloader
	 */
	public ClassLoader getParentClassloader() {
		return parentClassloader;
	}

	/**
	 * @param parentClassloader the parentClassloader to set
	 */
	public void setParentClassloader(ClassLoader parentClassloader) {
		this.parentClassloader = parentClassloader;
	}

    /**
     * Parses the command line arguments
     *
     * @param args The command line arguments
     * @return The JSAPResult object containing the parsed arguments
     */
    private static JSAPResult parseArguments(String[] args) throws JSAPException {
        JSAPResult result = null;

        String generalHelp = "Runs a MAU file";

        SimpleJSAP jsap =
            new SimpleJSAP(MAUExecutor.class.getSimpleName(),
            		generalHelp,
                    new Parameter[] {
	                    new UnflaggedOption("mau", JSAP.STRING_PARSER, true, "The MAU file to run"),
	                    new FlaggedOption("port", JSAP.INTEGER_PARSER,
	                            "1715", JSAP.NOT_REQUIRED, JSAP.NO_SHORTFLAG,
	                            "port", "The port number to bind to"),
	                    new FlaggedOption("config", JSAP.STRING_PARSER,
	                            JSAP.NO_DEFAULT, JSAP.NOT_REQUIRED, JSAP.NO_SHORTFLAG,
	                            "config", "The configuration file to use"),
	                    new FlaggedOption("param", JSAP.STRING_PARSER,
	                    		"", JSAP.NOT_REQUIRED, JSAP.NO_SHORTFLAG,
	                    		"param", "The key=value parameter to be passed to the flow")
	                    		.setAllowMultipleDeclarations(true)
                    });

        result = jsap.parse(args);
        if (jsap.messagePrinted())
            System.exit(-1);

        return result;
    }
}
