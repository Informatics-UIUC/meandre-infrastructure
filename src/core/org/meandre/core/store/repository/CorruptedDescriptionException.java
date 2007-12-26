package org.meandre.core.store.repository;

/** This class is thrown when a corruption of a component description is found.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class CorruptedDescriptionException extends Exception {

	/** A default serial ID */
	private static final long serialVersionUID = 1L;

	/** Create an empty exception.
	 * 
	 */
	public CorruptedDescriptionException() {
		super();
	}

	/** Creates an execution exception with the given message.
	 * 
	 * @param sMsg The message
	 */
	public CorruptedDescriptionException(String sMsg) {
		super(sMsg);
	}

	/** Creates an execution exception from the given throwable.
	 * 
	 * @param tObj The throwable object
	 */
	public CorruptedDescriptionException(Throwable tObj) {
		super(tObj);
	}

	/** Creates an execution exception from the given message and throwable object.
	 * 
	 * @param sMsg The message
	 * @param tObj The throwable object
	 */
	public CorruptedDescriptionException(String sMsg, Throwable tObj) {
		super(sMsg, tObj);
	}

}
