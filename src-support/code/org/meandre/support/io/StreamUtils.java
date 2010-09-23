/**
*
* University of Illinois/NCSA
* Open Source License
*
* Copyright (c) 2008, NCSA.  All rights reserved.
*
* Developed by:
* The Automated Learning Group
* University of Illinois at Urbana-Champaign
* http://www.seasr.org
*
* Permission is hereby granted, free of charge, to any person obtaining
* a copy of this software and associated documentation files (the
* "Software"), to deal with the Software without restriction, including
* without limitation the rights to use, copy, modify, merge, publish,
* distribute, sublicense, and/or sell copies of the Software, and to
* permit persons to whom the Software is furnished to do so, subject
* to the following conditions:
*
* Redistributions of source code must retain the above copyright
* notice, this list of conditions and the following disclaimers.
*
* Redistributions in binary form must reproduce the above copyright
* notice, this list of conditions and the following disclaimers in
* the documentation and/or other materials provided with the distribution.
*
* Neither the names of The Automated Learning Group, University of
* Illinois at Urbana-Champaign, nor the names of its contributors may
* be used to endorse or promote products derived from this Software
* without specific prior written permission.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
* EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
* IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE
* FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
* CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
* WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
*
*/

package org.meandre.support.io;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;

/**
 * @author Boris Capitanu
 */
public abstract class StreamUtils {

    public static int DEFAULT_BUFFER_SIZE = 65536;

    /**
     * Reads the content of an InputStream into a byte array
     *
     * @param dataStream The data stream
     * @return A byte array containing the data from the data stream
     * @throws IOException Thrown if a problem occurred while reading from the stream
     */
    public static byte[] getBytesFromStream(InputStream dataStream) throws IOException {
        InputStream bufStream = (dataStream instanceof BufferedInputStream) ?
                dataStream : new BufferedInputStream(dataStream);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        copyStream(bufStream, baos, DEFAULT_BUFFER_SIZE);

        return baos.toByteArray();
    }

    /**
     * Returns an InputStream for the specified resource.
     *
     * @param uri The resource location (can be a URL or a local file)
     * @return The InputStream to use to read from the resource
     * @throws IOException Thrown if the resource is invalid, does not exist, or cannot be opened
     */
    public static InputStream getInputStreamForResource(URI uri) throws IOException {
        return getURLforResource(uri).openStream();
    }

    /**
     * Returns an OutputStream for the specified resource
     *
     * @param uri The resource location (specified as either file:// or local path)
     * @return The OutputStream to use to write to the resource
     * @throws IOException Thrown if the resource is invalid
     */
    public static OutputStream getOutputStreamForResource(URI uri) throws IOException {
        URL url = getURLforResource(uri);

        if (url.getProtocol().equalsIgnoreCase("file"))
            try {
                return new FileOutputStream(new File(url.toURI()));
            }
            catch (URISyntaxException e) {
                // should never happen
                throw new RuntimeException(e);
            }
        else
            // TODO: add webdav support
            throw new UnsupportedOperationException("Can only write to file:// or local resources");
    }

    /**
     * Creates a URL object corresponding to a URI
     *
     * @param uri The URI (can reference URLs and local files)
     * @return The URL object
     * @throws MalformedURLException
     */
    public static URL getURLforResource(URI uri) throws MalformedURLException {
        try  {
            return uri.toURL();
        }
        catch (IllegalArgumentException e) {
            // URI not absolute - trying as local file
            try {
                return new File(URLDecoder.decode(uri.toString(), "UTF-8")).toURI().toURL();
            }
            catch (UnsupportedEncodingException e1) {
                // should never happen
                throw new RuntimeException(e1);
            }
        }
    }

    /**
     * Writes a resource resolvable through the specified class to an output stream
     *
     * @param clazz The class used to resolve the resource
     * @param resourceName The resource name
     * @param outputStream The output stream to write to
     * @throws IOException
     */
    public static void writeClassResourceToStream(Class<?> clazz, String resourceName, OutputStream outputStream) throws IOException {
        InputStream resStream = clazz.getResourceAsStream(resourceName);
        if (resStream == null)
            throw new ResourceNotFoundException(resourceName);

        copyStream(resStream, outputStream, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Reads the data read from one stream and writes that data out to another stream
     *
     * @param inputStream The input stream
     * @param outputStream The output stream
     * @param bufferSize The buffer size to use
     * @throws IOException
     */
    public static void copyStream(InputStream inputStream, OutputStream outputStream, int bufferSize) throws IOException {
        int len;
        byte[] buffer = new byte[bufferSize];

        while ((len = inputStream.read(buffer)) != -1)
            outputStream.write(buffer, 0, len);
    }
    
    public static class ResourceNotFoundException extends IOException {
        /**
         * 
         */
        private static final long serialVersionUID = -8970528487457774078L;

        public ResourceNotFoundException(String resName) {
            super("Could not find resource " + resName);
        }
    }
}
