package org.meandre.core.engine.execution;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONObject;
import org.meandre.configuration.CoreConfiguration;
import org.meandre.core.engine.Conductor;
import org.meandre.core.engine.ConductorException;
import org.meandre.core.engine.Executor;
import org.meandre.core.engine.MrProbe;
import org.meandre.core.engine.probes.StatisticsProbeImpl;
import org.meandre.core.logger.KernelLoggerFactory;
import org.meandre.core.repository.CorruptedDescriptionException;
import org.meandre.core.repository.QueryableRepository;
import org.meandre.core.repository.RepositoryImpl;
import org.meandre.core.utils.Constants;
import org.meandre.support.io.FileUtils;
import org.meandre.support.rdf.ModelUtils;
import org.meandre.webui.WebUI;

import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Executor that can be used with Meandre 2.0.x (based on MAUExecutor)
 * 
 * @author Boris Capitanu
 */

public class SaraExecutor {
    
    private final QueryableRepository qr;
    private final CoreConfiguration cnf;
    
    private Executor executor;
    private StatisticsProbeImpl spi;
    
    
    public SaraExecutor(QueryableRepository qr, CoreConfiguration cnf) {
        this.qr = qr;
        this.cnf = cnf;
    }

    private void run(int port, PrintStream console, PrintStream log) throws Exception {
        if (console != System.out) System.setOut(console);
        if (log != System.err)     System.setErr(log);

        log.println("Meandre Executor " + Constants.MEANDRE_VERSION);
        log.println("All rights reserved by DITA, NCSA, UofI (2007-2009)");
        log.println("THIS SOFTWARE IS PROVIDED UNDER University of Illinois/NCSA OPEN SOURCE LICENSE.");
        log.println();
        log.flush();
        
        Resource resURI = qr.getAvailableFlows().iterator().next();
        log.println("Preparing flow: "+resURI);
        
        Conductor conductor = new Conductor(cnf.getConductorDefaultQueueSize(), cnf);
        conductor.setParentClassloader(getClass().getClassLoader());

        try {
            spi = new StatisticsProbeImpl();
            spi.initialize();
            
            MrProbe mrProbe = new MrProbe(KernelLoggerFactory.getCoreLogger(), spi, false, false);
            executor = conductor.buildExecutor(qr, resURI, mrProbe, console);
            mrProbe.setName(executor.getThreadGroupName() + "mr-probe");

            log.flush();
            log.println("Preparation completed correctly\n");

            log.print("Execution started at: ");
            log.println(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date()));
            log.println("----------------------------------------------------------------------------");
            log.flush();
            
            String token = System.currentTimeMillis() + "";
            WebUI webui = executor.initWebUI(port, token);
            executor.execute(webui);
            console.flush();
            
            log.println("----------------------------------------------------------------------------");
            log.print("Execution finished at: ");
            log.println(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date()));
            if (executor.hadGracefullTermination())
                //
                // Graceful termination
                //
                log.println("Execution finished gracefully.");
            else {
                //
                // Aborted execution.
                //
                log.println("Execution aborted!!!\nReason:\n");
                for (String sMsg : executor.getAbortMessage())
                    log.println("\t" + sMsg);
            }
            log.flush();
        }
        catch (CorruptedDescriptionException cde) {
            log.println("Preparation could not be completed correctly!\n");
            log.println("----------------------------------------------------------------------------");
            log.println();
            log.println("Reason for aborting the preparation:");
            log.println();
            log.println(cde);
            log.flush();
            return;
        }
        catch (ConductorException ce) {
            log.println("----------------------------------------------------------------------------");
            log.print("Execution aborted at: ");
            log.println(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date()));
            log.println("----------------------------------------------------------------------------");
            log.println();
            log.println("Reason for aborting the execution:");
            log.println();
            log.println(ce);
            log.flush();
        }
        catch (NoClassDefFoundError te) {
            log.println("----------------------------------------------------------------------------");
            log.print("Missing class definition: ");
            log.println(te.getMessage());
            log.println("----------------------------------------------------------------------------");
            log.println();
            log.println("Exception details:");
            log.println();
            log.println(te);
            log.flush();
            return;
        }
        catch (Throwable t) {
            if (executor != null) executor.requestAbort();
            log.println("----------------------------------------------------------------------------");
            log.print("Unknown exception at: ");
            log.println(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date()));
            log.println("----------------------------------------------------------------------------");
            log.println();
            t.printStackTrace(log);
            log.println();
            log.flush();
        }

        printStatistics(log);
    }
    
    private void printStatistics(PrintStream ps) {
        try {
            JSONObject jsonStats = new JSONObject(spi.serializeProbeInformation());
            ps.println("----------------------------------------------------------------------------");
            ps.println();
            ps.println("Flow execution statistics");
            ps.println();
            if (jsonStats.has("flow_unique_id"))  ps.println("Flow unique execution ID : " + jsonStats.get("flow_unique_id"));
            if (jsonStats.has("flow_state"))      ps.println("Flow state               : " + jsonStats.get("flow_state"));
            if (jsonStats.has("started_at"))      ps.println("Started at               : " + jsonStats.get("started_at"));
            if (jsonStats.has("latest_probe_at")) ps.println("Last update              : " + jsonStats.get("latest_probe_at"));
            if (jsonStats.has("runtime"))         ps.println("Total run time (ms)      : " + jsonStats.get("runtime"));
            ps.println();
            ps.flush();

            if (jsonStats.has("executable_components_statistics")) {
                JSONArray jaEXIS = (JSONArray) jsonStats.get("executable_components_statistics");
                for (int i = 0, iMax = jaEXIS.length(); i < iMax; i++) {
                    JSONObject joEXIS = (JSONObject) jaEXIS.get(i);
                    if (joEXIS.has("executable_component_instance_id"))
                        ps.println("\tExecutable components instance ID          : " + joEXIS.get("executable_component_instance_id"));
                    if (joEXIS.has("executable_component_state"))
                        ps.println("\tExecutable components state                : " + joEXIS.get("executable_component_state"));
                    if (joEXIS.has("times_fired"))
                        ps.println("\tTimes the executable components fired      : " + joEXIS.get("times_fired"));
                    if (joEXIS.has("accumulated_runtime"))
                        ps.println("\tAccumulated executable components run time : " + joEXIS.get("accumulated_runtime"));
                    if (joEXIS.has("pieces_of_data_in"))
                        ps.println("\tPieces of data pulled                      : " + joEXIS.get("pieces_of_data_in"));
                    if (joEXIS.has("pieces_of_data_out"))
                        ps.println("\tPieces of data pushed                      : " + joEXIS.get("pieces_of_data_out"));
                    if (joEXIS.has("number_of_read_properties"))
                        ps.println("\tNumber of properties read                  : " + joEXIS.get("number_of_read_properties"));
                    ps.println();
                }
                ps.flush();
            }
        }
        catch ( Exception e ) {
            KernelLoggerFactory.getCoreLogger().warning("This exception should have never been thrown\n"+e);
        }
        spi.dispose();
    }
    
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Usage: SaraExecutor <port>");
            System.exit(-1);
        }
        
        int port = Integer.parseInt(args[0]);
        
        File baseDir = File.createTempFile("meandre_exec_", null);
        baseDir.delete();
        File runDir = new File(baseDir, "run");
        if (!runDir.mkdirs())
            throw new IOException("Cannot create necessary temporary folder(s) for flow execution: " + runDir);
        
        File pubResDir = new File(baseDir, "public_resources");
        
        Properties props = new Properties();
        props.setProperty(CoreConfiguration.MEANDRE_BASE_PORT, "" + (port - 1));
        props.setProperty(CoreConfiguration.MEANDRE_HOME_DIRECTORY, baseDir.toString());
        props.setProperty(CoreConfiguration.MEANDRE_PRIVATE_RUN_DIRECTORY, runDir.toString());
        props.setProperty(CoreConfiguration.MEANDRE_PUBLIC_RESOURCE_DIRECTORY, pubResDir.toString());
        props.setProperty(CoreConfiguration.MEANDRE_CORE_CONFIG_FILE, new File(baseDir, "meandre-config-core.xml").toString());
        
        try {
            QueryableRepository qr = new RepositoryImpl(ModelUtils.getModel(System.in, null));
            new SaraExecutor(qr, new CoreConfiguration(props)).run(port, System.out, System.err);
        }
        finally {
            FileUtils.deleteFileOrDirectory(baseDir);
        }
    }
}
