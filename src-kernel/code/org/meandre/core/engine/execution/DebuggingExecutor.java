package org.meandre.core.engine.execution;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import org.meandre.configuration.CoreConfiguration;
import org.meandre.core.repository.QueryableRepository;
import org.meandre.core.repository.RepositoryImpl;
import org.meandre.support.io.FileUtils;
import org.meandre.support.rdf.ModelUtils;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;
import com.martiansoftware.jsap.SimpleJSAP;
import com.martiansoftware.jsap.Switch;
import com.martiansoftware.jsap.UnflaggedOption;

import de.schlichtherle.io.FileInputStream;

public class DebuggingExecutor extends SaraExecutor {

	public DebuggingExecutor(QueryableRepository qr, CoreConfiguration cnf) {
		super(qr, cnf);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// Parse command line arguments
        JSAPResult jsapResult = parseArguments(args);

        URL[] rdfUrls = jsapResult.getURLArray("rdf");
        int port = jsapResult.getInt("port");
        String[] params = jsapResult.getStringArray("param");
        String paramFile = jsapResult.getString("paramFile", null);
        boolean quiet = jsapResult.getBoolean("quiet");

        // Extract the flow parameters
        Properties flowParams = new Properties();
        if (paramFile != null) {
        	try {
        		flowParams.load(new FileReader(paramFile));
        	}
        	catch (FileNotFoundException e) {
        		System.err.println("Error: File not found " + paramFile);
        		System.exit(-1);
        	}
        }

        for (String param : params) {
        	String key = param.substring(0, param.indexOf('='));
        	String value = param.substring(key.length() + 1);
        	flowParams.put(key, value);
        }

        Model master = ModelFactory.createDefaultModel();
        for (URL rdfUrl : rdfUrls)
        	master.add(ModelUtils.getModel(rdfUrl.toURI(), null));

        File baseDir = File.createTempFile("meandre_exec_", null);
        baseDir.delete();
        File runDir = new File(baseDir, "run");
        if (!runDir.mkdirs())
            throw new IOException("Cannot create necessary temporary folder(s) for flow execution: " + runDir);

        File pubResDir = new File(baseDir, "public_resources");

        File confFile = new File(baseDir, "meandre-config-core.xml");
        if (jsapResult.contains("conf"))
            confFile = new File(jsapResult.getString("conf"));

        Properties confProps = new Properties();
        if (confFile.exists())
            confProps.loadFromXML(new FileInputStream(confFile));

        Properties props = new Properties(confProps);
        props.setProperty(CoreConfiguration.MEANDRE_BASE_PORT, "" + (port - 1));
        props.setProperty(CoreConfiguration.MEANDRE_HOME_DIRECTORY, baseDir.toString());
        props.setProperty(CoreConfiguration.MEANDRE_PRIVATE_RUN_DIRECTORY, runDir.toString());
        props.setProperty(CoreConfiguration.MEANDRE_PUBLIC_RESOURCE_DIRECTORY, pubResDir.toString());
        props.setProperty(CoreConfiguration.MEANDRE_CORE_CONFIG_FILE, confFile.toString());

        try {
            QueryableRepository qr = new RepositoryImpl(master);
            new DebuggingExecutor(qr, new CoreConfiguration(props)).run(port, flowParams, System.out, System.err, quiet);
        }
        finally {
            FileUtils.deleteFileOrDirectory(baseDir);
        }
	}

    /**
     * Parses the command line arguments
     *
     * @param args The command line arguments
     * @return The JSAPResult object containing the parsed arguments
     */
    private static JSAPResult parseArguments(String[] args) throws JSAPException {
        JSAPResult result = null;

        String generalHelp = "Provides the ability to run a flow in debug mode";

        SimpleJSAP jsap =
            new SimpleJSAP(DebuggingExecutor.class.getSimpleName(),
                    generalHelp,
                    new Parameter[] {
                    new UnflaggedOption("rdf", JSAP.URL_PARSER, true, "The RDF for the flow and components").setGreedy(true),
                    new FlaggedOption("port", JSAP.INTEGER_PARSER,
                            "1760", JSAP.NOT_REQUIRED, 'p',
                            JSAP.NO_LONGFLAG, "The port number to run the flow on"),
                    new FlaggedOption("conf", JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.NOT_REQUIRED, JSAP.NO_SHORTFLAG, "conf",
                            "The configuration file to use as basis - port, home directory, run directory, and published resources " +
                            "directory settings in this config file will NOT be honored."),
                    new FlaggedOption("param", JSAP.STRING_PARSER,
                    		JSAP.NO_DEFAULT, JSAP.NOT_REQUIRED, JSAP.NO_SHORTFLAG,
                    		"param", "The key=value parameter to be passed to the flow")
                    		.setAllowMultipleDeclarations(true),
            		new FlaggedOption("paramFile", JSAP.STRING_PARSER,
                    		JSAP.NO_DEFAULT, JSAP.NOT_REQUIRED, JSAP.NO_SHORTFLAG,
                    		"paramFile", "A Java properties file containing the key=value flow parameters to be set"),
                    new Switch("quiet", JSAP.NO_SHORTFLAG, "quiet")
                    		.setHelp("Do not output flow statistics at end of flow execution")
                    });

        result = jsap.parse(args);
        if (jsap.messagePrinted())
            System.exit(-1);

        return result;
    }

}
