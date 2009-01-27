/**
 * 
 */
package org.meandre.jobs.storage.backend;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.Properties;

import org.meandre.core.logger.KernelLoggerFactory;

/** This class implements Derby specific flavor of the job information 
 * backend adapter
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class MySQLJobInformationBackendAdapter 
extends JobInformationBackendAdapter {

	/** The query map file */
	private static final String QUERY_MAP_FILE = "job_information_query_map_mysql.xml";

	/** Initialize the query map */
	public MySQLJobInformationBackendAdapter() {
		super();
		try {
			Properties props = new Properties();
			props.loadFromXML(MySQLJobInformationBackendAdapter.class.getResourceAsStream(QUERY_MAP_FILE));
			super.propQueryMapping.putAll(props);
		} catch (Exception e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			KernelLoggerFactory.getCoreLogger().severe("MySQL query map missing! "+baos.toString());
		}
	}
	
	/** Creates the required schema if it does not exist. 
	 * 
	 * @throws JobInformationBackendAdapterException Thrown when the connection to the back end could not be retrieved
	 */
	public void createSchema() 
	throws JobInformationBackendAdapterException {

		try {
			int iFailures = 0;
			JobInformationBackendAdapterException [] bae = new JobInformationBackendAdapterException[3];
			
			// Create the server status table
			try {
				String sQueryCSST = propQueryMapping.getProperty(QUERY_CREATE_JOB_STATUS_TABLE);
				executeUpdateQuery(sQueryCSST);
			} catch (JobInformationBackendAdapterException e) {
				bae[iFailures++]=e;
			}
			
			// Create the server info table
			try {
				String sQueryCSIT = propQueryMapping.getProperty(QUERY_CREATE_JOB_CONSOLE_TABLE);
				executeUpdateQuery(sQueryCSIT);
			} catch (JobInformationBackendAdapterException e) {
				bae[iFailures++]=e;
			}

			// Create the server properties table
			try {
				String sQueryCSPT = propQueryMapping.getProperty(QUERY_CREATE_JOB_LOG_TABLE);
				executeUpdateQuery(sQueryCSPT);
			} catch (JobInformationBackendAdapterException e) {
				bae[iFailures++]=e;
			}
			
			
			// Commit the transaction
			if ( bTransactional ) conn.commit();
			
			if ( iFailures==0 ) {
				log.fine(sServerID+" created job information schema");
			}
			else if  ( iFailures<3 ) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				for ( int i=0 ; i<iFailures ; i++ )
					bae[i].printStackTrace(new PrintStream(baos));
				log.severe("Rollback operation failed! "+baos.toString());
				for ( JobInformationBackendAdapterException e:bae )
					throw e;
			}
			
		} catch (SQLException e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			log.severe("Commit operation failed! "+baos.toString());
		}
		catch ( JobInformationBackendAdapterException bae ) {
			try {
				// Roll it back
				if ( bTransactional ) conn.rollback();
				throw bae;
			} catch (SQLException e) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				e.printStackTrace(new PrintStream(baos));
				log.severe("Rollback operation failed! "+baos.toString());
			}
		}
	}
	
}
