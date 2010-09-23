package org.meandre.support.io;

/**
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2008, Board of Trustees-University of Illinois.
 * All rights reserved.
 *
 * Developed by:
 *
 * Automated Learning Group
 * National Center for Supercomputing Applications
 * http://www.seasr.org
 *
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal with the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimers.
 *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimers in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the names of Automated Learning Group, The National Center for
 *    Supercomputing Applications, or University of Illinois, nor the names of
 *    its contributors may be used to endorse or promote products derived from
 *    this Software without specific prior written permission.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * WITH THE SOFTWARE.
 */

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Boris Capitanu
 *
 */

public abstract class FileUtils {

    /**
     * Finds files in a directory according to a filter
     *
     * @param dir The directory to start looking for files
     * @param filter The filter that will return true or false depending on whether the file should be included in the result or not
     * @param recurse True to recurse, false otherwise
     * @param fileList The list where the accepted files will be added to
     */
    public static void findFiles(File dir, final FileFilter filter, final boolean recurse, final List<File> fileList) {
        File[] files = dir.listFiles(new FileFilter() {
            public boolean accept(File f) {
                if (f.isFile())
                    return filter.accept(f);

                if (f.isDirectory() && recurse)
                    findFiles(f, filter, recurse, fileList);

                return false;
            }
        });

        fileList.addAll(Arrays.asList(files));
    }

    /**
     * Finds files in a directory according to a filter
     *
     * @param dir The directory to start looking for files
     * @param filter The filter that will return true or false depending on whether the file should be included in the result or not
     * @param recurse True to recurse, false otherwise
     * @param fileList The list where the accepted files will be added to
     */
    public static void findFiles(File dir, final FilenameFilter filter, final boolean recurse, final List<File> fileList) {
        File[] files = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                File f = new File(dir, name);

                if (f.isFile())
                    return filter.accept(dir, name);

                if (f.isDirectory() && recurse)
                    findFiles(f, filter, recurse, fileList);

                return false;
            }
        });

        fileList.addAll(Arrays.asList(files));
    }
    
    /**
     * Deletes a file or directory recursively
     * 
     * @param path The path to delete
     * @return True if successful, False otherwise
     */
    public static boolean deleteFileOrDirectory(File path) {
        if (path.isDirectory()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++)
                deleteFileOrDirectory(files[i]);
        }
        
        return path.delete();
    }

}
