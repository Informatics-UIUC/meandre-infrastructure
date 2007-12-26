package org.meandre.webui;

/** An exception occured on the WebUI.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class WebUIException extends Exception {

	/** A default serial ID */
	private static final long serialVersionUID = 1L;

	/** Create an empty exception.
	 * 
	 */
	public WebUIException() {
		super();
	}

	/** Creates an execution exception with the given message.
	 * 
	 * @param sMsg The message
	 */
	public WebUIException(String sMsg) {
		super(sMsg);
	}

	/** Creates an execution exception from the given throwable.
	 * 
	 * @param tObj The throwable object
	 */
	public WebUIException(Throwable tObj) {
		super(tObj);
	}

	/** Creates an execution exception from the given message and throwable object.
	 * 
	 * @param sMsg The message
	 * @param tObj The throwable object
	 */
	public WebUIException(String sMsg, Throwable tObj) {
		super(sMsg, tObj);
	}

}
