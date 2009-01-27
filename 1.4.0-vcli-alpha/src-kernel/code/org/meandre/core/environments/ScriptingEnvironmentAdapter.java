package org.meandre.core.environments;

import java.io.ByteArrayOutputStream;

/** This interface provides the basics for scripting environments.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public interface ScriptingEnvironmentAdapter {

	/** Traps the input and output error streams.
	 * 
	 */
	public void trapOutputAndErrorStreams ();
	
	/** Untraps the input and output error streams.
	 * 
	 */
	public void untrapOutputAndErrorStreams () ;

	/** Process the given script on an already prepared adapter.
	 *
	 * @param sScript The script to process
	 * @throws Exception Something when wrong
	 */
	public abstract void process(String sScript) throws Exception;

	/** Returns the interpreter output stream.
	 *
	 * @return The output stream
	 */
	public abstract ByteArrayOutputStream getOutput();

	/** Returns the interpreter error stream.
	 *
	 * @return The error stream
	 */
	public abstract ByteArrayOutputStream getError();

}