package org.meandre.core.logger;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;
/** This class is used to display the formatted logs.
 * 
 * @author Amit Kumar
 * Created on Jul 18, 2008 11:53:22 PM
 *
 */
class ShortFormatter extends Formatter {
	  public String format(LogRecord record) {
		  String className = record.getSourceClassName();
		  if(className!=null){
			  int i = className.lastIndexOf(".");
			  if(i!=-1){
				  className = record.getSourceClassName().substring(i+1);
			  }
		  }
		String threadName= Thread.currentThread().getName();
		if(threadName!=null){
			String split[] = threadName.split("/");
			if(split!=null && split.length>2){
				String componentName = split[split.length-2];
				String flowName = split[split.length -1];
				threadName = flowName +":"+componentName;
			}
		}
	     return record.getLevel() + " ["+threadName+":"+record.getThreadID()+"]"+" [" +
	         className+":"+record.getSourceMethodName() + "] " +
	         record.getMessage() + "\n";
	  }
}
