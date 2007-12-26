package org.meandre.core.engine;

/** This class is thrown when an exception is thrown when the conductor
 * could not start create and executor object out of flow description.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class ConductorException extends Exception {

	/** A default serial ID */
	private static final long serialVersionUID = 1L;

	/** Create an empty exception.
	 * 
	 */
	public ConductorException() {
		super();
	}

	/** Creates an execution exception with the given message.
	 * 
	 * @param sMsg The message
	 */
	public ConductorException(String sMsg) {
		super(sMsg);
	}

	/** Creates an execution exception from the given throwable.
	 * 
	 * @param tObj The throwable object
	 */
	public ConductorException(Throwable tObj) {
		super(tObj);
	}

	/** Creates an execution exception from the given message and throwable object.
	 * 
	 * @param sMsg The message
	 * @param tObj The throwable object
	 */
	public ConductorException(String sMsg, Throwable tObj) {
		super(sMsg, tObj);
	}

}
