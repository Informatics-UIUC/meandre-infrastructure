package org.meandre.core.engine.execution;

import java.io.File;
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
import com.martiansoftware.jsap.UnflaggedOption;

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

        Model master = ModelFactory.createDefaultModel();
        for (URL rdfUrl : rdfUrls)
        	master.add(ModelUtils.getModel(rdfUrl.toURI(), null));

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
            QueryableRepository qr = new RepositoryImpl(master);
            new DebuggingExecutor(qr, new CoreConfiguration(props)).run(port, System.out, System.err);
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
                            JSAP.NO_LONGFLAG, "The port number to run the flow on")
                    });

        result = jsap.parse(args);
        if (jsap.messagePrinted())
            System.exit(-1);

        return result;
    }

}
