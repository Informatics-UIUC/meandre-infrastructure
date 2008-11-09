/**
 * 
 */
package org.meandre.jobs.storage.helpers;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;

import org.meandre.core.logger.KernelLoggerFactory;
import org.meandre.jobs.storage.backend.JobInformationBackendAdapter;

/**
 * @author Xavier Llor&agrave;
 * 
 */
public class PersistentPrintStream extends PrintStream {

	/** The end of line marker */
	private static final String EOL = System.getProperty("line.separator"); 
	
	/** The main logger to use */
	private static final Logger log = KernelLoggerFactory.getCoreLogger();
	
	/** The wrapped print stream */
	private PrintStream ps;
	
	/** The job information backend object */
	private JobInformationBackendAdapter joba;

	/** The job id associated with this persistent print stream */
	private String sJID;
	
	/** The concurrent queue to send to the persistent storage */
	final private ConcurrentLinkedQueue<String> ccStream = new ConcurrentLinkedQueue<String>();
	
	/** The error flag in the print stream */
	private boolean bError;

	/** This class implements an asynchronous pusher to the backend.
	 * 
	 * @author Xavier Llor&agrave;
	 *
	 */
	private class AsynchronousUpdateThread 
	implements Runnable {

		/** Should the process finish? */
		private boolean bDone = false;
		
		/** The blocking semaphore */
		private Semaphore sem = new Semaphore(1);

		/** The run method for the asynchronous update.
		 * 
		 */
		public void run() {
			String sTmp;
			try {
				sem.acquire();
			} catch (InterruptedException e) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				e.printStackTrace(new PrintStream(baos));
				log.warning("Problem with "+AsynchronousUpdateThread.class.getName()+"!"+EOL+baos.toString());
			}
			while ( !bDone ) {
				if ( ccStream.isEmpty() )
					try {
						sem.acquire();
					} catch (InterruptedException e) {
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						e.printStackTrace(new PrintStream(baos));
						log.warning("Problem with "+AsynchronousUpdateThread.class.getName()+"!"+EOL+baos.toString());
					}
				StringBuffer sb = new StringBuffer();
				while ( (sTmp=ccStream.poll())!=null )
					sb.append(sTmp);
				joba.print(sJID,sb.toString());
			}
			StringBuffer sb = new StringBuffer();
			while ( (sTmp=ccStream.poll())!=null )
				sb.append(sTmp);
			joba.print(sJID,sb.toString());
		}
		
		/** Releases the semaphore.
		 * 
		 */
		public void awake() {
			sem.release();
		}
		
		/** This method provokes the finalization of the updater.
		 * 
		 */
		public void setDone () {
			bDone = true;
			awake();
		}
		
	}
	
	/** The thread updater */
	private AsynchronousUpdateThread updater;
	
	/**
	 * Creates a persistent print stream.
	 * 
	 * @param out
	 *            The output stream to wrap
	 * @param joba
	 *            The job information backend adapter
	 * @param sJID 
	 * 			  The job id
	 */
	public PersistentPrintStream(PrintStream out,
			JobInformationBackendAdapter joba, String sJID) {
		super(out);
		this.ps = out;
		this.joba = joba;
		this.sJID = sJID;
		this.bError = false;
		this.updater = new AsynchronousUpdateThread();
		
		// Start the asynchronous update
		new Thread(updater).start();
	}

	/** Check if there has been an error on the persistent 
	 * print stream.
	 * 
	 * @return The error flag
	 */
	public boolean checkError() {
		return bError || ps.checkError();
	}

	/** Close the persistent print stream.
	 * 
	 */
	public void close() {
		ps.close();
		updater.setDone();
	}

	/** Flush the wrapped print stream.
	 * 
	 */
	public void flush() {
		ps.flush();
		updater.awake();
	}

	/** Print a boolean.
	 * 
	 * @param b The boolean to print
	 */
	public void print(boolean b) {
		print(Boolean.toString(b));
	}

	/** Print a character.
	 * 
	 * @param c The character to print
	 */
	public void print(char c) {
		print(Character.toString(c));
	}

	/** Print a character array.
	 * 
	 * @param s The character array to print
	 */
	public void print(char[] s) {
		print(new String(s));
	}

	/** Print a double.
	 * 
	 * @param d The double to print
	 */
	public void print(double d) {
		print(Double.toString(d));
	}

	/** Print a float.
	 * 
	 * @param f The float to print
	 */
	public void print(float f) {
		print(Float.toString(f));
	}

	/** Print an integer.
	 * 
	 * @param i The integer to print
	 */
	public void print(int i) {
		print(Integer.toString(i));
	}

	/** Print a long.
	 * 
	 * @param l The long to print
	 */
	public void print(long l) {
		print(Long.toString(l));
	}

	/** Print an object.
	 * 
	 * @param obj The boolean to print
	 */
	public void print(Object obj) {
		print(obj.toString());
	}

	/** Prints a string and queues it for storaging.
	 * 
	 * @param s The string to store
	 */
	public void print(String s) {
		ps.print(s);
		ccStream.add(s);
		updater.awake();
	}

	/** Print a new line.
	 */
	public void println() {
		print(EOL);
	}

	/** Print a boolean and add a new end of line.
	 * 
	 * @param b The boolean to print
	 */
	public void println(boolean b) {
		print(Boolean.toString(b)+EOL);
	}

	/** Print a character and add a new end of line.
	 * 
	 * @param c The character to print
	 */
	public void println(char c) {
		print(Character.toString(c)+EOL);
	}

	/** Print a character array and add a new end of line.
	 * 
	 * @param b The character array to print
	 */
	public void println(char[] c) {
		print(new String(c)+EOL);
	}

	/** Print a double and add a new end of line.
	 * 
	 * @param d The double to print
	 */
	public void println(double d) {
		print(Double.toString(d)+EOL);
	}

	/** Print a float and add a new end of line.
	 * 
	 * @param f The float to print
	 */
	public void println(float f) {
		print(Float.toString(f)+EOL);
	}

	/** Print an integer and add a new end of line.
	 * 
	 * @param i The integer to print
	 */
	public void println(int i) {
		print(Integer.toString(i));
	}

	/** Print a long and add a new end of line.
	 * 
	 * @param l The long to print
	 */
	public void println(long l) {
		print(Long.toString(l)+EOL);
	}

	/** Print an object and add a new end of line.
	 * 
	 * @param obj The object to print
	 */
	public void println(Object obj) {
		print(obj.toString()+EOL);
	}

	/** Print a string and add a new end of line.
	 * 
	 * @param s The string to print
	 */
	public void println(String s) {
		print(s+EOL);
	}

	/** Set the error flag.
	 * 
	 */
	protected void setError() {
		bError = true;
	}

	/** Print the subset of the byte array assuming it represents a string.
	 * 
	 * @param ba The string to print
	 */
	public void write(byte[] buf, int off, int len) {
		byte[] ba = new byte[len];
		System.arraycopy(buf, off, ba, 0, len);
		write(ba);		
	}

	/** Print the integer assuming it encodes a character.
	 * 
	 * @param i The integer/character to print
	 */
	public void write(int i) {
		print(Integer.toString(i).charAt(0));
	}

	/** Print a byte array assuming it represents a string  
	 * and add a new end of line.
	 * 
	 * @param ba The string to print
	 */
	public void write(byte[] ba) {
		print(new String(ba));
	}
}
