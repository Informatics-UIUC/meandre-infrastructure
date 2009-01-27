package org.meandre.zigzag.console;

import java.io.IOException;
import java.io.OutputStream;

/** A null output stream.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class NullOuputStream extends OutputStream {

	/** Null flush.
	 * 
	 */
	public void flush () {
	}
	
	/** Null close.
	 * 
	 */
	public void close () {
	}
	
	/** Null write.
	 * 
	 * @param b The bytes to write
	 */
	public void write(byte[] b) throws IOException {
	}
	
	/** Null write.
	 * 
	 * @param b The bytes to write
	 * @param off The offset
	 * @param len The length
	 */
	public void write(byte[] b, int off, int len) throws IOException {
	}
	
	/** Null write.
	 * 
	 * @param i The value to write
	 */
	public void write(int i) throws IOException {
	}

}
