package org.meandre.executables;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.logging.Level;

import org.meandre.executables.shutdown.ShutdownControlThread;
import org.meandre.executables.shutdown.Shutdownable;
import org.meandre.webservices.MeandreServer;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.stringparsers.FileStringParser;

/**
 * commandline interface to start up a MeandreServer (Infrastructure-Server).
 */

public class RunInfrastructureCMD{

    public static final String APP_NAME = "MeandreInfrastructure";

    /**the name of the file with default locations that will be written
     * in the installation dir.
     */
    private static String LOC_FILENAME = "meandre-default-locations.txt";


    /** port specified on the commandline to run the server on*/
    private static int _serverPort;

    /** port specified on the commandline to listen to local shutdown commands.*/
    private static int _shutdownPort;

    /** directory for installation files (including config files) specified
     * on commandline.
     */
    private static File _installDir;

    /** urls of ttl files to load automatically when the infrastructure
     * server starts up the first time.  */
    private static String[] _defaultLocations;

    private static Level _kernelLogLevel = null;
    private static Level _wsLogLevel = null;


    public static void main(String[] args) throws Exception{
        parseArgs(args);

        prepareDefaultLocations();

        ShutdownableServer server = new ShutdownableServer(_serverPort, _installDir.toString());
        server.setGlobalLoggingLevel(_kernelLogLevel, _wsLogLevel);

        ShutdownControlThread sct = new ShutdownControlThread(server, _shutdownPort, _installDir, APP_NAME);

        log("RunInfrastructureCMD: Starting ShutdownControlThread");
        sct.start();
        log("RunInfrastructureCMD: Starting MeandreServer");
        server.start();
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
            RunInfrastructureCMD.exitWithError(config, jsap);
        }

        _installDir = config.getFile("installationDir");
        _serverPort = config.getInt("meandrePort");
        _shutdownPort = config.getInt("shutdownPort");
        _defaultLocations = config.getStringArray("defaultLocations");
        _kernelLogLevel = Level.parse(config.getString("kernelLogLevel", "INFO"));
        _wsLogLevel = Level.parse(config.getString("wsLogLevel", "INFO"));

        if(!_installDir.exists()){
            _installDir.mkdirs();
        }
        log("Installation Dir = " + _installDir);
        log("Shutdown Port = " + _shutdownPort);
        log("Meandre Port = " + _serverPort);
    }

    private static JSAP makeCommandLineParser() throws JSAPException{
        JSAP jsap = new JSAP();

        //dir for installation files
        FlaggedOption installDirOpt = new FlaggedOption("installationDir");
        installDirOpt.setShortFlag('d');
        installDirOpt.setLongFlag("install-dir");
        installDirOpt.setStringParser(FileStringParser.getParser());
        installDirOpt.setRequired(true);
        installDirOpt.setHelp("The directory that contains meandre config" +
            " files and repository contents. If this directory does not " +
            "exist it will be created and populated with a default installation");
        jsap.registerParameter(installDirOpt);

        //option to specify the kernel log level
        FlaggedOption kernelLogLevelOpt = new FlaggedOption("kernelLogLevel");
        kernelLogLevelOpt.setShortFlag(JSAP.NO_SHORTFLAG);
        kernelLogLevelOpt.setLongFlag("kernel-log-level");
        kernelLogLevelOpt.setRequired(false);
        kernelLogLevelOpt.setHelp("The kernel logging level, one of: " +
        		"ALL, FINEST, FINER, FINE, INFO, WARNING, SEVERE");
        jsap.registerParameter(kernelLogLevelOpt);

        //option to specify the webservices log level
        FlaggedOption wsLogLevelOpt = new FlaggedOption("wsLogLevel");
        wsLogLevelOpt.setShortFlag(JSAP.NO_SHORTFLAG);
        wsLogLevelOpt.setLongFlag("ws-log-level");
        wsLogLevelOpt.setRequired(false);
        wsLogLevelOpt.setHelp("The webservices logging level, one of: " +
                "ALL, FINEST, FINER, FINE, INFO, WARNING, SEVERE");
        jsap.registerParameter(wsLogLevelOpt);

        //port number for the webservices
        FlaggedOption meandrePortOpt = new FlaggedOption("meandrePort");
        meandrePortOpt.setShortFlag('p');
        meandrePortOpt.setLongFlag("meandre-port");
        meandrePortOpt.setStringParser(JSAP.INTEGER_PARSER);
        meandrePortOpt.setRequired(false);
        meandrePortOpt.setDefault("1714");
        meandrePortOpt.setHelp("The port number of the " +
            " Meandre Infrastructure instance's webservices." +
            " Defaults to '1714' if unspecified." );
        jsap.registerParameter(meandrePortOpt);

        //port number for the shutdown controller
        FlaggedOption shutdownPortOpt = new FlaggedOption("shutdownPort");
        shutdownPortOpt.setShortFlag('s');
        shutdownPortOpt.setLongFlag("shutdown-port");
        shutdownPortOpt.setStringParser(JSAP.INTEGER_PARSER);
        shutdownPortOpt.setRequired(false);
        shutdownPortOpt.setDefault("1711");
        shutdownPortOpt.setHelp("The port number of the " +
            " Shutdown listener service. Run MeandreShutdownCMD with the" +
            " same port number (and securityTokenDirectory equal to this" +
            " server's install-directory) to shutdown this MeandreServer." +
            " Defaults to '1711' if unspecified." );
        jsap.registerParameter(shutdownPortOpt);

       //meandre location to be auto-loaded on startup
        FlaggedOption defaultLocationsOpt = new FlaggedOption("defaultLocations");
        defaultLocationsOpt.setShortFlag('l');
        defaultLocationsOpt.setLongFlag("default-location");
        defaultLocationsOpt.setStringParser(JSAP.STRING_PARSER);
        defaultLocationsOpt.setRequired(false);
        defaultLocationsOpt.setAllowMultipleDeclarations(true);
        defaultLocationsOpt.setHelp("Default repository location(s) to be" +
            " loaded on startup. This should be a full url to a .ttl, .rdf, or" +
            " .nt file. Multiple declarations are allowed." );
        jsap.registerParameter(defaultLocationsOpt);
        return jsap;
    }

    /**
     * Writes a file with the default locations into the installation directory
     * so that MeandreServer will load them when it's Store starts up. If
     * the file is already there, it will assume the existing file contains
     * what the user really wants and NOT overwrite the file.
     * @throws FileNotFoundException
     */
    private static void prepareDefaultLocations() throws FileNotFoundException{
        //don't bother if no locations were specified on the commandline
        if(_defaultLocations.length == 0){
            return;
        }


        //the file must live in the install directory, make sure it's there
        if(!_installDir.exists()){
            _installDir.mkdir();
        }
        File locFile = new File(_installDir, LOC_FILENAME);

        //write the file, unless the file is already there
        if(!locFile.exists()){
            PrintWriter printer = new PrintWriter(locFile);
            for(String loc: _defaultLocations){
                printer.println(loc);
            }
            printer.flush();
            printer.close();
        }

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
                RunInfrastructureCMD.class.getName());
        System.err.println("                "
                + jsap.getUsage());
        System.err.println();
        System.err.println(jsap.getHelp());
        System.exit(1);
    }

    private static void log(String msg){
        System.out.println(msg);
    }
    /** a version of MeandreServer that conforms to the Shutdownable interface.
     * This is so a ShutdownControlThread can shut down our server.
     */
    private static class ShutdownableServer extends MeandreServer
            implements Shutdownable{

        public ShutdownableServer(int port, String sInstallDir){
            super(port, sInstallDir);

        }

        @Override
        public void stop(){
            try{
                super.stop();
            }catch(Exception e){
                e.printStackTrace();
            }
        }

    }

}
