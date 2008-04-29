/**
 * 
 */
package org.meandre.zigzag.console;

import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.Level;

import jline.ConsoleReader;

import org.meandre.core.logger.KernelLoggerFactory;
import org.meandre.core.utils.Constants;
import org.meandre.zigzag.parser.ParseException;
import org.meandre.zigzag.parser.ZigZag;
import org.meandre.zigzag.semantic.FlowGenerator;

/** The ZigZag interpreter console
 * 
 * @author Xavier Llor&agrave
 *
 */
public class ZigZagConsole {

	/** The ZigZag parser */
	private ZigZag parser;
	
	/** The flow generator object */
	private FlowGenerator fg;

	/** Should the console be disposed */
	private boolean bNotDone;

	/** Creates a ZigZag console object.
	 * 
	 */
	public ZigZagConsole () {
		bNotDone = true;
		parser = null;    
	    resetFlowDescriptor();
	}
	
	/** Resets the current flow descriptor.
	 * 
	 */
	public void resetFlowDescriptor() {
		fg = new FlowGenerator();
	    fg.setPrintStream(new PrintStream(new NullOuputStream()));
	}
	
	/** Fires the console interpreter.
	 * 
	 * @param sArgs The commandline arguaments
	 * @throws IOException Problem arised on the console reader
	 */
	public void start ( String [] sArgs ) throws IOException {
		
		System.out.println("Meandre ZigZag scripting language interpreter console ["+ZigZag.ZIGZAG_VERSION+"/"+Constants.MEANDRE_VERSION+"]");
		System.out.println("All rigths reserved by DITA, NCSA, UofI (2007-2008).");
		System.out.println("THIS SOFTWARE IS PROVIDED UNDER University of Illinois/NCSA OPEN SOURCE LICENSE.");
		System.out.println();
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
	
		if ( sCmd.endsWith("help") ) {
			printHelp(saLine);
			bProcessed = true;
		}
		else if ( sCmd.endsWith("quit") ) {
			// Quit the console
			bNotDone = false;
			bProcessed = true;
		}
		else if ( sCmd.endsWith("reset") ) {
			// Resets the flow description
			resetFlowDescriptor();
			System.out.println();
			System.out.println("\t Flow information reseted.");
			System.out.println();
			bProcessed = true;
		}
			
		return bProcessed;
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
			System.out.println("\t help : Prints the help. Type help [command] for deatiled help.");
			System.out.println("\t reset: Resets the current flow.");
			System.out.println("\t quit : Quits the console.");
		}
		else if ( sCmd.equals("help") ) {
			System.out.println("\t help [command]:");
			System.out.println("\t\t Prints the help for the provided command.");
		}
		else if ( sCmd.equals("quit") ) {
			System.out.println("\t quit: Quits the console.");
		}
		else if ( sCmd.equals("reset") ) {
			System.out.println("\t reset: Resets the current flow build so far.");
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
