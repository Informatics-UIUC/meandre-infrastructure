package org.meandre.executables;

import java.io.File;

import org.meandre.executables.shutdown.ShutdownClient;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.stringparsers.FileStringParser;

/**
 * commandline interface to start up a MeandreServer (Infrastructure-Server). 
 */

public class ShutdownInfrastructureCMD{



    /** port specified on the commandline to listen to local shutdown commands.*/
    private static int _shutdownPort;

    /** directory for installation files (including config files) specified
     * on commandline.
     */
    private static File _installDir;

    public static void main(String[] args) throws Exception{
        parseArgs(args);

        ShutdownClient sc = new ShutdownClient(RunInfrastructureCMD.APP_NAME,
            _installDir, _shutdownPort);

        sc.shutdown();
    }

    /**
     * populate the static variables of this class.
     * @throws JSAPException 
     * @throws UnknownHostException 
     */
    private static void parseArgs(String[] args) throws JSAPException{

        JSAP jsap = makeCommandLineParser();
        
        JSAPResult config = jsap.parse(args);

        if (!config.success()) {
            exitWithError(config, jsap);
        }

        _installDir = config.getFile("installationDir");
        _shutdownPort = config.getInt("shutdownPort");
        log("Installation Dir = " + _installDir);
        log("Shutdown Port = " + _shutdownPort);
        
    }

    private static JSAP makeCommandLineParser() throws JSAPException{
        JSAP jsap = new JSAP();
        
        //dir for installation files
        FlaggedOption installDirOpt = new FlaggedOption("installationDir");
        installDirOpt.setShortFlag('d');
        installDirOpt.setLongFlag("install-dir");
        installDirOpt.setStringParser(FileStringParser.getParser());
        installDirOpt.setRequired(true);
        installDirOpt.setHelp("The directory the MeandreInfrastructureCMD was" +
            " told to install too.");
        jsap.registerParameter(installDirOpt);



        //port number for the shutdown controller
        FlaggedOption shutdownPortOpt = new FlaggedOption("shutdownPort");
        shutdownPortOpt.setShortFlag('s');
        shutdownPortOpt.setLongFlag("shutdown-port");
        shutdownPortOpt.setStringParser(JSAP.INTEGER_PARSER);
        shutdownPortOpt.setRequired(false);
        shutdownPortOpt.setDefault("1711");
        shutdownPortOpt.setHelp("The port number of the " +
            " Shutdown listener service. See MeandreInfrastructureCMD. " +
            " Defaults to '1711' if unspecified." );
        jsap.registerParameter(shutdownPortOpt);

        return jsap;
    }

    /**
     * error message when the result of the parse fails. The contents of
     * this method are copied from the JASP tutorial at:
     * http://www.martiansoftware.com/jsap/doc/
     * 
     * @param parseResult the jsapResult returned when the commandline args
     * where parsed. assumes this 'has errors'
     * @param jsap the jsap used to parse the commandline args that created
     * the error result parseResult
     */
    private static void exitWithError(JSAPResult parseResult, JSAP jsap){
        System.err.println();

        for (java.util.Iterator errs = parseResult.getErrorMessageIterator();
            errs.hasNext();) {
            System.err.println("Error: " + errs.next());
        }

        System.err.println();
        System.err.println("Usage: java " +
                ShutdownInfrastructureCMD.class.getName());
        System.err.println("                "
                + jsap.getUsage());
        System.err.println();
        System.err.println(jsap.getHelp());
        System.exit(1);
    }
    
    private static void log(String msg){
        System.out.println(msg);
    }

}
