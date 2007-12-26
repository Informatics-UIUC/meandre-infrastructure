package org.meandre.core.store.security;

/** This exception is thrown when the security store runs into a problem
 * that does not allow it to complete the operation.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class SecurityStoreException extends Exception {

	/** A default serial ID */
	private static final long serialVersionUID = 1L;

	/** Create an empty exception.
	 * 
	 */
	public SecurityStoreException() {
		super();
	}

	/** Creates an execution exception with the given message.
	 * 
	 * @param sMsg The message
	 */
	public SecurityStoreException(String sMsg) {
		super(sMsg);
	}

	/** Creates an execution exception from the given throwable.
	 * 
	 * @param tObj The throwable object
	 */
	public SecurityStoreException(Throwable tObj) {
		super(tObj);
	}

	/** Creates an execution exception from the given message and throwable object.
	 * 
	 * @param sMsg The message
	 * @param tObj The throwable object
	 */
	public SecurityStoreException(String sMsg, Throwable tObj) {
		super(sMsg, tObj);
	}

}
