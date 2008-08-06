package org.meandre.core.logger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/** This class is used to display the formatted logs.
 * 
 * @author Amit Kumar
 * Modified by Xavier Llor&agrave;
 * Created on Jul 18, 2008 11:53:22 PM
 *
 */
public class MeandreCoordinatorFormatter extends Formatter {
	
	/** The maximum lenght of a thread name */
	private final static int MAX_THREAD_NAME_LENGTH = 40;
	
	/** The new line separator */
	private final static String NEW_LINE = System.getProperty("line.separator");
	
	private final static SimpleDateFormat FORMATER = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS");
	
	/** Formats the record.
	 * 
	 * @param The log record to format
	 * @return The formated record
	 */
	  public String format(LogRecord record) {
		  String className = record.getSourceClassName();
		  
		  String threadName = Thread.currentThread().getName();
		  if(threadName!=null && threadName.length()>MAX_THREAD_NAME_LENGTH ){
			  threadName = threadName.substring(threadName.length()-MAX_THREAD_NAME_LENGTH);
		  }
		  
		  String sTimeStamp = FORMATER.format(new Date(record.getMillis()));
		  
		  return sTimeStamp+"::"+
		   		record.getLevel()+":  "+
		   		record.getMessage()+ "  " + 
		   		" ["+className+"."+record.getSourceMethodName() + "]"+
		   		" <"+threadName+":"+record.getThreadID()+">"+
		   		NEW_LINE;
	  }
}
