/**
 * 
 */
package org.meandre.core.services.coordinator.backend;

/** This exception is thrown when the backend adapter runs into a problem.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class CoordinatorBackendAdapterException extends Exception {

	/** A default serial ID */
	private static final long serialVersionUID = 1L;

	/** Create an empty exception.
	 * 
	 */
	public CoordinatorBackendAdapterException() {
		super();
	}

	/** Creates an execution exception with the given message.
	 * 
	 * @param sMsg The message
	 */
	public CoordinatorBackendAdapterException(String sMsg) {
		super(sMsg);
	}

	/** Creates an execution exception from the given throwable.
	 * 
	 * @param tObj The throwable object
	 */
	public CoordinatorBackendAdapterException(Throwable tObj) {
		super(tObj);
	}

	/** Creates an execution exception from the given message and throwable object.
	 * 
	 * @param sMsg The message
	 * @param tObj The throwable object
	 */
	public CoordinatorBackendAdapterException(String sMsg, Throwable tObj) {
		super(sMsg, tObj);
	}


}
