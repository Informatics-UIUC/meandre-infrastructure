/**
 * 
 */
package org.meandre.zigzag.console;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.Date;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;

import jline.ConsoleReader;

import org.meandre.core.logger.KernelLoggerFactory;
import org.meandre.core.repository.DataPortDescription;
import org.meandre.core.repository.ExecutableComponentDescription;
import org.meandre.core.repository.FlowDescription;
import org.meandre.core.repository.PropertiesDescriptionDefinition;
import org.meandre.core.repository.QueryableRepository;
import org.meandre.core.utils.Constants;
import org.meandre.zigzag.parser.ParseException;
import org.meandre.zigzag.parser.ZigZag;
import org.meandre.zigzag.semantic.FlowGenerator;

import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

/** The ZigZag interpreter console
 * 
 * @author Xavier Llor&agrave
 *
 */
public class ZigZagConsole {

	/** The new line caracter */
	private final static String NEW_LINE = System.getProperty("line.separator");
	
	/** The ZigZag parser */
	private ZigZag parser;
	
	/** The flow generator object */
	private FlowGenerator fg;

	/** Should the console be disposed */
	private boolean bNotDone;
	
	/** The current ZigZag file so far */
	private StringBuffer sbZigZag;

	/** Creates a ZigZag console object.
	 * 
	 */
	public ZigZagConsole () {
		bNotDone = true;
		parser = null;    
		sbZigZag =  new StringBuffer();
	    resetFlowDescriptor();
	}
	
	/** Resets the current flow descriptor.
	 * 
	 */
	public void resetFlowDescriptor() {
		fg = new FlowGenerator();
	    fg.setPrintStream(new PrintStream(new NullOuputStream()));
	    fg.init(null);
	}
	
	/** Fires the console interpreter.
	 * 
	 * @param sArgs The commandline arguaments
	 * @throws IOException Problem arised on the console reader
	 */
	public void start ( String [] sArgs ) throws IOException {
		
		printVersion();
		System.out.println("Session starterd at: "+new Date());
		System.out.println();
		
		ConsoleReader cr = new ConsoleReader();
		cr.setDefaultPrompt(">>> ");
		cr.setBellEnabled(true);
		
		bNotDone = true;
		String sLine;
		while ( bNotDone ) {
			//System.out.print(">>> "); 
			try {
				sLine = cr.readLine().trim();
				if (sLine.length()>0 )
					parseCommandLine(sLine);
			}
			catch ( NullPointerException e ) {
				System.out.println();
				bNotDone = false;
			} catch (ParseException e) {
				System.err.println(e.getMessage());
				//e.printStackTrace();
			}
			
		}
		
		
	}

	/** Print the current version of the ZigZga interpreter console.
	 * 
	 */
	protected void printVersion() {
		System.out.println();
		System.out.println("Meandre ZigZag scripting language interpreter console ["+ZigZag.ZIGZAG_VERSION+"/"+Constants.MEANDRE_VERSION+"]");
		System.out.println("All rigths reserved by DITA, NCSA, UofI (2007-2008).");
		System.out.println("THIS SOFTWARE IS PROVIDED UNDER University of Illinois/NCSA OPEN SOURCE LICENSE.");
		System.out.println();
	}

	/** Parses the interpreter command line.
	 * 
	 * @param sLine The line to parser
	 * @throws ParseException There was a parser problem
	 */
	private void parseCommandLine(String sLine) throws ParseException {
		boolean bParsed = parseSystemCommand(sLine);
		if ( !bParsed )
			parseZigZagEntry(sLine);		
	}

	/** Tries to parser a system command.
	 * 
	 * @param sLine The line to parse
	 * @return True if that was a system command
	 */
	private boolean parseSystemCommand(String sLine) {
		boolean bProcessed = false;
		String[] saLine = sLine.split("[ \t]");
		String sCmd = saLine[0];
	
		if ( sCmd.equals("help") ) {
			// Print the help
			printHelp(saLine);
			bProcessed = true;
		}
		else if ( sCmd.equals("ls") ) {
			// List whatever needs to be listed
			listComponentsFlows(saLine);
			bProcessed = true;
		}
		else if ( sCmd.equals("search") ) {
			// List whatever needs to be listed
			searchComponentsFlows(saLine);
			bProcessed = true;
		}
		else if ( sCmd.equals("desc") ) {
			// List whatever needs to be listed
			describeComponent(saLine);
			bProcessed = true;
		}
		else if ( sCmd.equals("quit") ) {
			// Quit the console
			bNotDone = false;
			bProcessed = true;
		}
		else if ( sCmd.equals("reset") ) {
			// Resets the flow description
			resetFlowDescriptor();
			sbZigZag = new StringBuffer();
			System.out.println();
			System.out.println("\t Flow information reseted.");
			System.out.println();
			bProcessed = true;
		}
		else if ( sCmd.equals("save") ) {
			saveFile(saLine);
			bProcessed = true;
		}
		else if ( sCmd.equals("load") ) {
			loadZigZag(saLine);
			bProcessed = true;
		}
		else if ( sCmd.equals("version") ) {
			printVersion();
			System.out.println("\t ---Sempre sense perdre el món de vista,");
			System.out.println("\t       guaitant més enllà del Meandre---");
			System.out.println("\t                                 Xavier.");
			System.out.println();
			bProcessed = true;
		}
			
		return bProcessed;
	}
	
	/** Load a ZigZag file into the interpreter.
	 * 
	 * @param saLine The command line being processes
	 */
	private void loadZigZag(String[] saLine) {
		if ( saLine.length<3 ) {
			System.out.println();
			System.out.println("\t Wrong syntax. Please see help load for more information.");
			System.out.println();
		}
		else {
			String sFormat = saLine[1];
			String sURI = "";
			for ( int i=2, iMax=saLine.length ; i<iMax ; i++ )
				sURI += saLine[i]+" ";
			sURI = sURI.trim();
		
			if ( sFormat.equals("zigzag") ) {
				
				try {
					FileReader fr = new FileReader(sURI);
					LineNumberReader lnr = new LineNumberReader(fr);
					StringBuffer sb = new StringBuffer();
					String sLine;
					while ( (sLine=lnr.readLine())!=null )
						sb.append(sLine+NEW_LINE);
					
					parseZigZagEntry(sb.toString());
					
				} catch (FileNotFoundException e) {
					System.out.println();
					System.out.println("\t File "+sURI+" could not be loaded.");
					System.out.println();
				} catch (IOException e) {
					System.out.println();
					System.out.println("\t Problem while reading from file "+sURI+".");
					System.out.println();
				} catch (ParseException e) {
					System.out.println();
					System.out.println(e.getMessage());
					System.out.println();
				}
			}
			else {
				System.out.println();
				System.out.println("\t File "+sURI+" could not be loaded.");
				System.out.println();
			}
		}
	}

	/** Save the current flow to the disk.
	 * 
	 * @param saLine The command line being processed.
	 */
	private void saveFile(String[] saLine) {
		if ( saLine.length<3 ) {
			System.out.println();
			System.out.println("\t Wrong syntax. Please see help save for more information.");
			System.out.println();
		}
		else {
			String sFormat = saLine[1];
			
			String sURI = "";
			for ( int i=2, iMax=saLine.length ; i<iMax ; i++ )
				sURI += saLine[i]+" ";
			sURI = sURI.trim();
			
			if ( sFormat.equals("zigzag") ) {
				// Save the ZigZag file
				try {
					File file = new File(sURI);
					FileOutputStream fos = new FileOutputStream(file);
					PrintStream ps = new PrintStream(fos);
					ps.println(sbZigZag.toString());
					ps.flush();
					ps.close();
					fos.close();
				} catch (FileNotFoundException e) {
					System.out.println();
					System.out.println("\t File "+sURI+" could not be saved.");
					System.out.println();
				} catch (IOException e) {
					System.out.println();
					System.out.println("\t Problem while closing file "+sURI+".");
					System.out.println();
				}
			}
			else if ( sFormat.equals("mau") ) {
				// Save the MAU file
				try {
					fg.generateMAU(sURI);
				} catch (ParseException e) {
					System.out.println();
					System.out.println("\t File "+sURI+" could not be saved.");
					System.out.println();
				}
			}
			else {
				System.out.println();
				System.out.println("\t Wrong file format. Please see help save for more information.");
				System.out.println();
			}
		}
		
	}

	/** Describes the requested component
	 * 
	 * @param saLine The processed command line
	 */
	private void describeComponent(String[] saLine) {
		if ( saLine.length==1 ) {
			System.out.println();
			System.out.println("\t Wrong syntax. Please see help desc for more information.");
			System.out.println();
		}
		else {
			String sURI = "";
			for ( int i=1, iMax=saLine.length ; i<iMax ; i++ )
				sURI += saLine[i]+" ";
			sURI = sURI.trim();
			
			ExecutableComponentDescription ecd = 
				fg.getRepository()
				  .getExecutableComponentDescription(
						  ModelFactory.createDefaultModel().createResource(sURI)
						);
			
			if ( ecd!=null ) {
				System.out.println();
				
				System.out.println("\t "+ecd.getName()+" ("+ecd.getExecutableComponent()+")");
				System.out.println("\t by "+ecd.getCreator()+" on "+ecd.getCreationDate());
				// Tags
				System.out.print("\t [ ");
				Object [] oa = ecd.getTags().getTags().toArray();
				for ( int i=0, iMax=oa.length-1 ; i<iMax ; i++ )
					System.out.print(oa[i]+", ");
				if ( oa.length>0 )
					System.out.print(oa[oa.length-1]);
				System.out.println(" ]");
				// Properties
				PropertiesDescriptionDefinition pdd = ecd.getProperties();
				System.out.println("\t Properties:");
				for ( String sKey:pdd.getKeys() )
					System.out.println("\t\t "+sKey+" = "+pdd.getValue(sKey)+" ("+pdd.getDescription(sKey)+")");
				// Inputs
				System.out.println("\t Inputs:");
				for ( DataPortDescription dpd:ecd.getInputs()) 
					System.out.println("\t\t "+dpd.getName()+" ("+dpd.getDescription()+")");
				// Outputs
				System.out.println("\t Outputs:");
				for ( DataPortDescription dpd:ecd.getOutputs()) 
					System.out.println("\t\t "+dpd.getName()+" ("+dpd.getDescription()+")");
				
				System.out.println();
			}
			else {
				System.out.println("\t Component "+sURI+" does not exist in the currently assembled repository");
			}
		}
	}

	/** Search the components or flows in the current repository.
	 * 
	 * @param saLine The processed command line
	 */
	private void searchComponentsFlows(String[] saLine) {
		if ( saLine.length==1 )
			listComponentsFlows(saLine);
		else {
			String sQuery = "";
			for ( int i=1, iMax=saLine.length ; i<iMax ; i++ )
				sQuery += saLine[i]+" ";
			sQuery = sQuery.trim();
			
			QueryableRepository qr = fg.getRepository();
			
			// List Components
			System.out.println("Components:");
			for ( Resource res:qr.getAvailableExecutableComponents(sQuery) ) {
				ExecutableComponentDescription ecd = qr.getExecutableComponentDescription(res);
				System.out.println("\t "+ecd.getName()+" ("+ecd.getExecutableComponent()+")");
			}
			System.out.println();
		
			// List flows
			System.out.println("Flows:");
			for ( Resource res:qr.getAvailableFlows(sQuery) ) {
				FlowDescription fd = qr.getFlowDescription(res);
				System.out.println("\t "+fd.getName()+" ("+fd.getFlowComponent()+")");
			}
			System.out.println();
			
		}
	}

	/** List the components or flows in the current repository.
	 * 
	 * @param saLine The processed command line
	 */
	private void listComponentsFlows(String[] saLine) {
		String sCmd = (saLine.length>1)?saLine[1]:"";
		
		QueryableRepository qr = fg.getRepository();
		System.out.println();
		boolean bError = true;
		if ( saLine.length==1 || sCmd.equals("components") ) {
			// List Components
			System.out.println("Components:");
			for ( ExecutableComponentDescription ecd:qr.getAvailableExecutableComponentDescriptions() ) {
				System.out.println("\t "+ecd.getName()+" ("+ecd.getExecutableComponent()+")");
			}
			System.out.println();
			bError = false;
		}
		if ( saLine.length==1 || sCmd.equals("flows") ) {
			// List flows
			System.out.println("Flows:");
			for ( FlowDescription fd:qr.getAvailableFlowDecriptions() ) {
				System.out.println("\t "+fd.getName()+" ("+fd.getFlowComponent()+")");
			}
			System.out.println();
			bError = false;
		}
		if ( sCmd.equals("zigzag") ) {
			// List the current ZigZag script
			System.out.println(sbZigZag.toString());
			System.out.println();
			bError = false;
		}
		if ( sCmd.equals("aliases") ) {
			// List the current ZigZag script
			Map<String, ExecutableComponentDescription> mapAlias = fg.getComponentAliases();
			for ( String sKey:mapAlias.keySet() )
				System.out.println(sKey+" --> "+mapAlias.get(sKey).getExecutableComponent());
			System.out.println();
			bError = false;
		}
		if ( sCmd.equals("instances") ) {
			// List the current ZigZag script
			Map<String, String> mapAlias = fg.getInstances();
			for ( String sKey:mapAlias.keySet() )
				System.out.println(sKey+" instance-of "+mapAlias.get(sKey));
			System.out.println();
			bError = false;
		}
		if ( bError ) {
			System.out.println("\t Unknown command ls "+sCmd+". See help list for more information.");
		}
	}

	/** Print the help for the system console commands.
	 * 
	 * @param saLine The help request
	 */
	private void printHelp(String[] saLine) {
		String sCmd = (saLine.length>1)?saLine[1]:"";
		
		if ( saLine.length==1 ) {
			System.out.println("ZigZag interpreter console help");
			System.out.println();
			System.out.println("\t desc   : Describe the requested component.");
			System.out.println("\t help   : Prints the help. Type help [command] for deatiled help.");
			System.out.println("\t load   : Load a ZigZag script into the interpreter.");
			System.out.println("\t ls     : List information related to the ZigZag script built.");
			System.out.println("\t reset  : Resets the current constructed flow.");
			System.out.println("\t search : Searches for any component and flow that match the query.");
			System.out.println("\t save   : Save the flow built so far.");
			System.out.println("\t quit   : Quits the console (same as ctr+D).");
			System.out.println("\t version: Prints the version information.");
		}
		else if ( sCmd.equals("help") ) {
			System.out.println("\t help [command]:");
			System.out.println("\t\t Prints the help for the provided command.");
		}
		else if ( sCmd.equals("quit") ) {
			System.out.println("\t quit: Quits the console (same as ctr+D).");
		}
		else if ( sCmd.equals("search") ) {
			System.out.println("\t search [query]: Searches for components and flows that match the");
			System.out.println("\n                 provided query against the available metadata.");
		}
		else if ( sCmd.equals("ls") ) {
			System.out.println("\t ls [components|flows|zigzag|aliases|instances]: ");
			System.out.println("\t                  List the current imported components or flows.");
			System.out.println("\t                  It can also list the current ZigZag script, the alias");
			System.out.println("\t                  assigned so far, and the current componentent instances.");
			System.out.println("\t                  If no argument is provided available components and flows are listed.");
		}
		else if ( sCmd.equals("desc") ) {
			System.out.println("\t desc <component_URI>: ");
			System.out.println("\t                  Provides a complete description of the requested component.");
		}
		else if ( sCmd.equals("reset") ) {
			System.out.println("\t reset: Resets the current constructed flow build so far.");
		}
		else if ( sCmd.equals("version") ) {
			System.out.println("\t version: Print the version information for this ZigZag interpreter console.");
		}
		else if ( sCmd.equals("save") ) {
			System.out.println("\t save [zigzag|mau] <file_name>: ");
			System.out.println("\t                  Save the current flow to the provided file name. ");
			System.out.println("\t                  If the ZigZag format is selected save the flow as a ZigZag file. ");
			System.out.println("\t                  Otherwise, the flow is saved as a MAU file. ");
		}
		else if ( sCmd.equals("load") ) {
			System.out.println("\t load zigzag <file_name>: ");
			System.out.println("\t                  Loads a ZigZag file into the interpreter. ");
		}
		else {
			System.out.println("\t Unknow command "+sCmd+".");
		}
		
		System.out.println();
	}

	/** Parses the command line as part of ZigZag.
	 * 
	 * @param sLine The command line
	 * @throws ParseException There was a parser problem
	 */
	private void parseZigZagEntry(String sLine) throws ParseException {
		parser = new ZigZag(new StringReader(sLine));
		parser.setFlowGenerator(fg);
		parser.start();
		sbZigZag.append(sLine+NEW_LINE);
	}

	/** The entry point to the main.
	 * 
	 * @param sArgs The command line arguments
	 * @throws IOException Something when wrong :D
	 */
	public static void main ( String [] sArgs ) throws IOException {
		// Tone down the logger
		KernelLoggerFactory.getCoreLogger().setLevel(Level.SEVERE);
		for ( Handler h:KernelLoggerFactory.getCoreLogger().getHandlers() )
			h.setLevel(Level.SEVERE);
		
		ZigZagConsole zzc = new ZigZagConsole();
		zzc.start(sArgs);
	}
	
}
