/**
 * 
 */
package org.meandre.core.services.coordinator.backend;

/** This exception is thrown when the backend adapter runs into a problem.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class BackendAdapterException extends Exception {

	/** A default serial ID */
	private static final long serialVersionUID = 1L;

	/** Create an empty exception.
	 * 
	 */
	public BackendAdapterException() {
		super();
	}

	/** Creates an execution exception with the given message.
	 * 
	 * @param sMsg The message
	 */
	public BackendAdapterException(String sMsg) {
		super(sMsg);
	}

	/** Creates an execution exception from the given throwable.
	 * 
	 * @param tObj The throwable object
	 */
	public BackendAdapterException(Throwable tObj) {
		super(tObj);
	}

	/** Creates an execution exception from the given message and throwable object.
	 * 
	 * @param sMsg The message
	 * @param tObj The throwable object
	 */
	public BackendAdapterException(String sMsg, Throwable tObj) {
		super(sMsg, tObj);
	}


}
