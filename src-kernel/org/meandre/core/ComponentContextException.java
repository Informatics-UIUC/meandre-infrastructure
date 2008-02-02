package org.meandre.core;

/** This class is thrown when an exception is thrown when a violation of the
 * component context access is detected.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class ComponentContextException extends Exception {

	/** A default serial ID */
	private static final long serialVersionUID = 1L;

	/** Create an empty exception.
	 * 
	 */
	public ComponentContextException() {
		super();
	}

	/** Creates an execution exception with the given message.
	 * 
	 * @param sMsg The message
	 */
	public ComponentContextException(String sMsg) {
		super(sMsg);
	}

	/** Creates an execution exception from the given throwable.
	 * 
	 * @param tObj The throwable object
	 */
	public ComponentContextException(Throwable tObj) {
		super(tObj);
	}

	/** Creates an execution exception from the given message and throwable object.
	 * 
	 * @param sMsg The message
	 * @param tObj The throwable object
	 */
	public ComponentContextException(String sMsg, Throwable tObj) {
		super(sMsg, tObj);
	}

}
