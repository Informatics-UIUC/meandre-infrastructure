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
	 * @param iLimit The maxim number of entries returned (0 or negative to return all)
	 * @return The resulting list of lists of text with the column name
	 * @throws CoordinatorBackendAdapterException Something when wrong running the select
	 */
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
		// TODO Auto-generated method stub
		
	}
}
