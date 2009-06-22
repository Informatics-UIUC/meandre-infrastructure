package org.meandre.support.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public abstract class FileUtils {

    /**
     * Creates an MD5 checksum for a file
     *
     * @param file The file
     * @return The byte[] containing the MD5 checksum
     * @throws IOException Thrown when an I/O error occurs
     */
    public static byte[] createMD5Checksum(File file) throws IOException {
        MessageDigest complete;
        InputStream fis = new FileInputStream(file);

        try {
            byte[] buffer = new byte[4096];

            try {
                complete = MessageDigest.getInstance("MD5");
            }
            catch (NoSuchAlgorithmException e) {
                return null;
            }

            int numRead;
            do {
                if ((numRead = fis.read(buffer)) > 0) {
                    complete.update(buffer, 0, numRead);
                }
            } while (numRead > 0);
        }
        finally {
            fis.close();
        }

        return complete.digest();
    }

}
