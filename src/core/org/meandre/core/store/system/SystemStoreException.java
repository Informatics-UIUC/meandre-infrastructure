package org.meandre.core.store.system;

/** This class is thrown when an execption is produced in the system store.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class SystemStoreException extends Exception {

	/** A default serial ID */
	private static final long serialVersionUID = 1L;

	/** Create an empty exception.
	 * 
	 */
	public SystemStoreException() {
		super();
	}

	/** Creates an execution exception with the given message.
	 * 
	 * @param sMsg The message
	 */
	public SystemStoreException(String sMsg) {
		super(sMsg);
	}

	/** Creates an execution exception from the given throwable.
	 * 
	 * @param tObj The throwable object
	 */
	public SystemStoreException(Throwable tObj) {
		super(tObj);
	}

	/** Creates an execution exception from the given message and throwable object.
	 * 
	 * @param sMsg The message
	 * @param tObj The throwable object
	 */
	public SystemStoreException(String sMsg, Throwable tObj) {
		super(sMsg, tObj);
	}

}
