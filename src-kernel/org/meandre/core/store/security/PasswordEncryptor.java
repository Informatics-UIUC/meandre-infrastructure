package org.meandre.core.store.security;

import org.mortbay.jetty.security.UnixCrypt;



/** The factory produces the an encyption object that generates
 * password encription. Based on the example published at
 * http://www.devarticles.com/c/a/Java/Password-Encryption-Rationale-and-Java-Example/
 * 
 * @author Xavier Llor&agrave;
 *
 */
public final class PasswordEncryptor {

	/** Encrypts a raw text. 
	 * 
	 * @param plaintext The text to encrypt
	 * @return The encrypted text
	 */
	public static synchronized String encrypt(String plaintext) {
		
		return UnixCrypt.crypt(plaintext, "meandre");
		
	}

}
