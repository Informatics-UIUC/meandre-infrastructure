package org.meandre.core.engine;

/** This class is thrown when an exception is thrown when a probe
 * runs into an unexpected problem.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class ProbeException extends Exception {

	/** A default serial ID */
	private static final long serialVersionUID = 1L;

	/** Create an empty exception.
	 * 
	 */
	public ProbeException() {
		super();
	}

	/** Creates an execution exception with the given message.
	 * 
	 * @param sMsg The message
	 */
	public ProbeException(String sMsg) {
		super(sMsg);
	}

	/** Creates an execution exception from the given throwable.
	 * 
	 * @param tObj The throwable object
	 */
	public ProbeException(Throwable tObj) {
		super(tObj);
	}

	/** Creates an execution exception from the given message and throwable object.
	 * 
	 * @param sMsg The message
	 * @param tObj The throwable object
	 */
	public ProbeException(String sMsg, Throwable tObj) {
		super(sMsg, tObj);
	}

}
