/**
 * 
 */
package org.meandre.jobs.storage.backend;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import org.meandre.core.logger.KernelLoggerFactory;
import org.meandre.core.services.coordinator.tools.DatabaseBackendAdapterException;
import org.meandre.core.services.coordinator.tools.DatabaseTools;
import org.meandre.core.utils.NetworkTools;

/** Base class for different JobInformation backend adapters
 * 
 * @author Xavier Llor&agrave;
 *
 */
public abstract class JobInformationBackendAdapter {

	/** The common query map file */
	static final String COMMON_MAP_FILE = "job_information_query_map_common.xml";

	/** The database flavor property name */
	public final static String PROPERTY_DB = "DB";
	
	/** The marker used for the queries */
	public final static String PROPERTY_MARKER = "MARKER";

	/** Create job status query constant */
	protected final static String QUERY_CREATE_JOB_STATUS_TABLE = "CREATE_JOB_STATUS_TABLE";
	
	/** Create job console query constant */
	protected final static String QUERY_CREATE_JOB_CONSOLE_TABLE = "CREATE_JOB_CONSOLE_TABLE";

	/** Create job log query constant */
	protected final static String QUERY_CREATE_JOB_LOG_TABLE = "CREATE_JOB_LOG_TABLE";
	
	/** Drop job status query constant */
	protected final static String QUERY_DROP_JOB_STATUS_TABLE = "DROP_JOB_STATUS_TABLE";
	
	/** Drop job console query constant */
	protected final static String QUERY_DROP_JOB_CONSOLE_TABLE = "DROP_JOB_CONSOLE_TABLE";

	/** Drop job log query constant */
	protected final static String QUERY_DROP_JOB_LOG_TABLE = "DROP_JOB_LOG_TABLE";
	
	/** Insert and entry to the console */
	protected final static String QUERY_INSERT_JOB_CONSOLE = "INSERT_JOB_CONSOLE";
	
	/** Insert and entry to the log */
	protected final static String QUERY_INSERT_JOB_LOG = "INSERT_JOB_LOG";

	/** Select the console for a given job */
	protected final static String QUERY_SELECT_JOB_CONSOLE = "SELECT_JOB_CONSOLE";
	
	/** Selects the log for a given job */
	protected final static String QUERY_SELECT_JOB_LOG = "SELECT_JOB_LOG";
	
	/** The logger to use */
	protected Logger log = KernelLoggerFactory.getCoreLogger();

	/** The properties object containing all the query mappings */
	protected Properties propQueryMapping = new Properties();

	/** The connection to the backend storage */
	protected Connection conn;

	/** Is the backend storage transaction aware */
	protected boolean bTransactional;

	/** The port of the Meandre server that is running the stuff */
	private int iPort;

	/** The server ID */
	protected String sServerID;

	/** Initialize the query map */
	public JobInformationBackendAdapter() {
		try {
			propQueryMapping.loadFromXML(JobInformationBackendAdapter.class.getResourceAsStream(COMMON_MAP_FILE));
		} catch (Exception e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			log.severe("Derby query map missing! "+baos.toString());
		}
	}
	

	/** Links this backend adapter to the given store.
	 * 
	 * @param conn The connection to the backend store
	 * @param iPort The port number where the coordinated service runs
	 * @param aspcb The service callback
	 */
	public void linkToService(Connection conn, int iPort ) {
				
		// Get the connection object and the transaction mode
		try {
			this.conn = conn;
			this.bTransactional = !conn.getAutoCommit();
			this.iPort = iPort;
			this.sServerID = NetworkTools.getServerID(this.iPort);
			
			log.info(sServerID+" linked to job information storage facility");
			
		} catch (Exception e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			log.severe("Error stablishind connection and transaction check! "+baos.toString());
		}		
	}
	
	/** This method is only provided to unlink the service and clean the 
	 * connection information.
	 * 
	 * @throws CoordinatorBackendAdapterException The connection could not be properly closed
	 * 
	 */
	public void close() {
		try {
			conn.close();
		} catch (SQLException e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			log.severe("Error closing the link to the job information backend adapter! "+baos.toString());
		}
	}
	
	/** Runs and update query against the backend.
	 * 
	 * @param sQuery The query to run the update
	 * @throws CoordinatorBackendAdapterException Thrown when there was a problem with the backend running the update
	 */
	protected void executeUpdateQuery(String sQuery)
	throws JobInformationBackendAdapterException {
		// Run the update
		try {
			DatabaseTools.executeUpdateQuery(conn, sQuery);
		} catch (DatabaseBackendAdapterException e) {
			throw new JobInformationBackendAdapterException(e);
		}
	}
	
	/** Runs an update prepare statement with the given values.
	 * 
	 * @param sQuery The query to run
	 * @param oaValues The values to use for the update
	 * @throws CoordinatorBackendAdapterException Something when wrong running the update
	 */
	private int executeUpdateQueryWithParams(String sQuery, Object[] oaValues ) 
	throws JobInformationBackendAdapterException {
		
		// Run the update
		try {
			return DatabaseTools.executeUpdateQueryWithParams(conn, sQuery, oaValues);
		} catch (DatabaseBackendAdapterException e) {
			throw new JobInformationBackendAdapterException(e);
		}
	}

	/** Return a list of list with the results of select query in text form.
	 * 
	 * @param sQuery The query to run
	 * @return The resulting list of lists of text
	 * @throws CoordinatorBackendAdapterException Something when wrong running the select
	 */
	@SuppressWarnings("unused")
	private List<List<String>> selectTextColumns (String sQuery ) 
	throws JobInformationBackendAdapterException {
		try {
			return DatabaseTools.selectTextColumns(conn, sQuery);
		} catch (DatabaseBackendAdapterException e) {
			throw new JobInformationBackendAdapterException(e);
		}
	}


	/** Return a list of list with the results of select query in text form.
	 * 
	 * @param sQuery The query to run
	 * @param oaValues The parameters to set in the query
	 * @return The resulting list of lists of text
	 * @throws CoordinatorBackendAdapterException Something when wrong running the select
	 */
	private List<List<String>> selectTextColumnsWithParams (String sQuery, Object [] oaValues ) 
	throws JobInformationBackendAdapterException {
		try {
			return DatabaseTools.selectTextColumnsWithParams(conn, sQuery,oaValues);
		} catch (DatabaseBackendAdapterException e) {
			throw new JobInformationBackendAdapterException(e);
		}
	}

	/** Return a list of list with the results of select query in text form.
	 * 
	 * @param sQuery The query to run
	 * @param iLimit The maxim number of entries returned (0 or negative to return all)
	 * @return The resulting list of lists of text with the column name
	 * @throws CoordinatorBackendAdapterException Something when wrong running the select
	 */
	@SuppressWarnings("unused")
	private List<Map<String,String>> selectTextColumnsWithName (String sQuery, int iLimit ) 
	throws JobInformationBackendAdapterException {
		try {
			return DatabaseTools.selectTextColumnsWithName(conn, sQuery, iLimit);
		} catch (DatabaseBackendAdapterException e) {
			throw new JobInformationBackendAdapterException(e);
		}
	}

	/** Return a list of list with the results of select query in text form.
	 * 
	 * @param sQuery The query to run
	 * @param oaValues The values to use for the query
	 * @return The resulting list of lists of text
	 * @throws CoordinatorBackendAdapterException Something when wrong running the select
	 */
	@SuppressWarnings("unused")
	private List<List<String>> selectTextColumns (String sQuery, Object [] oaValues) 
	throws JobInformationBackendAdapterException {
		try {
			return DatabaseTools.selectTextColumns(conn, sQuery, oaValues);
		} catch (DatabaseBackendAdapterException e) {
			throw new JobInformationBackendAdapterException(e);
		}
	}

	
	/** Returns the backend flavor. The flavor corresponds to the name of
	 * the relational database backend used (e.g. Derby, MySQL, Oracle, etc.)
	 * 
	 * @return The flavor of the backend
	 */
	public String getFlavor () {
		return propQueryMapping.getProperty(PROPERTY_DB);
	}
	
	/** Returns the character used to identify substitutions on the 
	 * templates contained in the query mapping
	 * 
	 * @return The marker for substitutions
	 */
	public String getMarker () {
		return propQueryMapping.getProperty(PROPERTY_MARKER);
	}
	
	/** Return the number of queries stored in the adapter.
	 * 
	 * @return The number of queries
	 */
	public int getNumberOfQueries () {
		return propQueryMapping.size()-2;
	}


	/** Creates the required schema if it does not exist. 
	 * 
	 * @throws JobInformationBackendAdapterException Thrown when the connection to the back end could not be retrieved
	 */
	public abstract void createSchema() throws JobInformationBackendAdapterException;
	
	/** Drops the schema from the backend storage.
	 * 
	 * @throws JobInformationBackendAdapterException
	 */
	public void dropSchema() 
	throws JobInformationBackendAdapterException {
		try {
			// Drop the server log table
			String sQueryDSST = propQueryMapping.getProperty(QUERY_DROP_JOB_STATUS_TABLE);
			executeUpdateQuery(sQueryDSST);
			
			String sQueryDSCT = propQueryMapping.getProperty(QUERY_DROP_JOB_CONSOLE_TABLE);
			executeUpdateQuery(sQueryDSCT);
			
			String sQueryDSLT = propQueryMapping.getProperty(QUERY_DROP_JOB_LOG_TABLE);
			executeUpdateQuery(sQueryDSLT);
	
			// Commit the transaction
			if ( bTransactional ) conn.commit();
			
			log.fine(sServerID+" droped job information schema");
		} catch (SQLException e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			log.severe("Commit operation failed! "+baos.toString());
		}
		
	}

	/** Logs information into the log table.
	 * 
	 * @param sServerID The server ID to use
	 * @param sJobID The job ID login the entry
	 * @param sLevel The lever of the log
	 * @param oLog The log text to trace
	 */
	public void log ( String sServerID, String sJobID, String sLevel, Object oLog ) {
		try {
			String sQueryIJL = propQueryMapping.getProperty(QUERY_INSERT_JOB_LOG);
			Object [] oaValuesUpdate = {
					sServerID,
					sJobID,
					sLevel.trim(),
					oLog.toString().trim()
				};
			executeUpdateQueryWithParams(sQueryIJL, oaValuesUpdate);
			
			// Commit the transaction
			if ( bTransactional ) conn.commit();
			
		} catch (JobInformationBackendAdapterException e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			log.warning("Could not log to the job information storage! "+baos);
		}
		catch (SQLException e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			log.severe("Commit operation failed! "+baos.toString());
		}
	}
	

	/** Prints information into the log table.
	 * 
	 * @param sServerID The server ID to use
	 * @param sJobID The job ID login the entry
	 * @param oText The log text to trace
	 */
	public void print ( String sServerID, String sJobID, Object oLog ) {
		try {
			String sQueryIJC = propQueryMapping.getProperty(QUERY_INSERT_JOB_CONSOLE);
			Object [] oaValuesUpdate = {
					sServerID,
					sJobID,
					oLog.toString().trim()
				};
			executeUpdateQueryWithParams(sQueryIJC, oaValuesUpdate);
			
			// Commit the transaction
			if ( bTransactional ) conn.commit();
			
		} catch (JobInformationBackendAdapterException e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			log.warning("Could not print to the job information storage! "+baos);
		}
		catch (SQLException e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			log.severe("Commit operation failed! "+baos.toString());
		}
	}

	/** Returns the current log for the provided job ID.
	 * 
	 * @param sJobID The job ID
	 * @return The concatenated string of logs
	 */
	public String getLog ( String sJobID ) {
		StringBuffer sb = new StringBuffer();
		
		try {
			String sQueryIJC = propQueryMapping.getProperty(QUERY_SELECT_JOB_LOG);
			Object [] oaValuesUpdate = { sJobID };
			for ( List<String> ls:selectTextColumnsWithParams(sQueryIJC,oaValuesUpdate) ) {
				String s = ls.get(ls.size()-1).trim();
				s = s.replaceAll("\n$|\n\r$|\r\n$", "");
				sb.append(ls.get(ls.size()-2).trim()+": "+s+"\n");
			}
		} catch (JobInformationBackendAdapterException e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			log.warning("Could not retrieve log from job information storage! "+baos);
		}
		
		return sb.toString();
	}
	

	/** Returns the current console for the provided job ID.
	 * 
	 * @param sJobID The job ID
	 * @return The concatenated string of logs
	 */
	public String getConsole ( String sJobID ) {
		StringBuffer sb = new StringBuffer();
		
		try {
			String sQueryIJC = propQueryMapping.getProperty(QUERY_SELECT_JOB_CONSOLE);
			Object [] oaValuesUpdate = { sJobID };
			for ( List<String> ls:selectTextColumnsWithParams(sQueryIJC,oaValuesUpdate) ) {
				String s = ls.get(ls.size()-1);
				s = s.replaceAll("\n$|\n\r$|\r\n$", "");
				sb.append(s+"\n");
			}
		} catch (JobInformationBackendAdapterException e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			log.warning("Could not retrive console from job information storage! "+baos);
		}
		
		return sb.toString();
	}
}
