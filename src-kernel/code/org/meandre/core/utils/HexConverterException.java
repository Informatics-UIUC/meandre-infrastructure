package org.meandre.core.utils;

/** This class is thrown when a HEX string cannot be converted back.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class HexConverterException extends Exception {

	/** A default serial ID */
	private static final long serialVersionUID = 1L;

	/** Create an empty exception.
	 * 
	 */
	public HexConverterException() {
		super();
	}

	/** Creates an execution exception with the given message.
	 * 
	 * @param sMsg The message
	 */
	public HexConverterException(String sMsg) {
		super(sMsg);
	}

	/** Creates an execution exception from the given throwable.
	 * 
	 * @param tObj The throwable object
	 */
	public HexConverterException(Throwable tObj) {
		super(tObj);
	}

	/** Creates an execution exception from the given message and throwable object.
	 * 
	 * @param sMsg The message
	 * @param tObj The throwable object
	 */
	public HexConverterException(String sMsg, Throwable tObj) {
		super(sMsg, tObj);
	}

}
