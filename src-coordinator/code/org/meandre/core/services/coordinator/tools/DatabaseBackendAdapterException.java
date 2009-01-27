/**
 * 
 */
package org.meandre.core.services.coordinator.tools;

/** This exception is thrown when the database backend adapter runs into a problem.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class DatabaseBackendAdapterException extends Exception {

	/** A default serial ID */
	private static final long serialVersionUID = 1L;

	/** Create an empty exception.
	 * 
	 */
	public DatabaseBackendAdapterException() {
		super();
	}

	/** Creates an execution exception with the given message.
	 * 
	 * @param sMsg The message
	 */
	public DatabaseBackendAdapterException(String sMsg) {
		super(sMsg);
	}

	/** Creates an execution exception from the given throwable.
	 * 
	 * @param tObj The throwable object
	 */
	public DatabaseBackendAdapterException(Throwable tObj) {
		super(tObj);
	}

	/** Creates an execution exception from the given message and throwable object.
	 * 
	 * @param sMsg The message
	 * @param tObj The throwable object
	 */
	public DatabaseBackendAdapterException(String sMsg, Throwable tObj) {
		super(sMsg, tObj);
	}


}
