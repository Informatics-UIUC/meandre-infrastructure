package org.meandre.core.utils;
import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
/**
 * Breadth first search over a directory and all its sub directories. Only
 * returns directories, never the other files that may be in a directory.
 * Returns the root directory of the search on the first call to next();
 *
 * <p>The class itself is an iterator over files (Iterator<File>). To use
 * it, call the constructor and declare it as an Iterator. Example:
 *
 * <p>Iterator<File> filesInMyDir = new DirectoryTreeIterator(myDir);
 *
 *
 *
 * @author Peter Groves
 */

public class DirectoryTreeIterator implements Iterator<File>{

    /** holds a File locator for each subdirectory. calls to next() will
     * pop the first element off this list.
     */
    LinkedList<File> _dirs;

    /**
     * create a directory tree iterator for the input directory.
     */
    public DirectoryTreeIterator(File treeDir){
        assert(treeDir.isDirectory());
        //log("Building DirectoryTree list");
        LinkedList<File> dirsToVisit = new LinkedList<File>();
        _dirs = new LinkedList<File>();
        _dirs.add(treeDir);


        dirsToVisit.addLast(treeDir);
        while(!dirsToVisit.isEmpty()){
            File visitDir = dirsToVisit.removeFirst();
            File[] dirContents = visitDir.listFiles();
            for(int i = 0; i < dirContents.length; i++){
                if(dirContents[i].isDirectory()){
                    dirsToVisit.addLast(dirContents[i]);
                    _dirs.addLast(dirContents[i]);
                    //log("\t" + dirContents[i]);
                    
                }
            }
        }
        return;
    }

    public boolean hasNext(){
        return (!_dirs.isEmpty());
    }

    public File next(){
        return (_dirs.removeFirst());
    }

    public void remove(){
        System.out.println("DirectoryTreeIterator.remove() NOT implemented");
    }
    
    @SuppressWarnings("unused")
	private void log(String msg){
    	System.out.println(msg);
    }

}
