package org.meandre.core;

/** This class is thrown when an exception is thrown while executing and ExecutableComponent.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class ComponentExecutionException extends Exception {

	/** A default serial ID */
	private static final long serialVersionUID = 1L;

	/** Create an empty exception.
	 * 
	 */
	public ComponentExecutionException() {
		super();
	}

	/** Creates an execution exception with the given message.
	 * 
	 * @param sMsg The message
	 */
	public ComponentExecutionException(String sMsg) {
		super(sMsg);
	}

	/** Creates an execution exception from the given throwable.
	 * 
	 * @param tObj The throwable object
	 */
	public ComponentExecutionException(Throwable tObj) {
		super(tObj);
	}

	/** Creates an execution exception from the given message and throwable object.
	 * 
	 * @param sMsg The message
	 * @param tObj The throwable object
	 */
	public ComponentExecutionException(String sMsg, Throwable tObj) {
		super(sMsg, tObj);
	}

}
