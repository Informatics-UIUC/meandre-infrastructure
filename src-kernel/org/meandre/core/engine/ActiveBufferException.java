package org.meandre.core.engine;

/** This class is thrown when an exception is thrown while manipulating and active buffer.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class ActiveBufferException extends Exception {

	/** A default serial ID */
	private static final long serialVersionUID = 1L;

	/** Create an empty exception.
	 * 
	 */
	public ActiveBufferException() {
		super();
	}

	/** Creates an execution exception with the given message.
	 * 
	 * @param sMsg The message
	 */
	public ActiveBufferException(String sMsg) {
		super(sMsg);
	}

	/** Creates an execution exception from the given throwable.
	 * 
	 * @param tObj The throwable object
	 */
	public ActiveBufferException(Throwable tObj) {
		super(tObj);
	}

	/** Creates an execution exception from the given message and throwable object.
	 * 
	 * @param sMsg The message
	 * @param tObj The throwable object
	 */
	public ActiveBufferException(String sMsg, Throwable tObj) {
		super(sMsg, tObj);
	}

}
