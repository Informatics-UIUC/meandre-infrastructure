/**
 * 
 */
package org.meandre.core.services.arbitration.backend;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.meandre.core.logger.KernelLoggerFactory;
import org.meandre.core.utils.NetworkTools;

/** The base class for the backend adapters.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public abstract class BackendAdapter {

	/** The database flavour property name */
	public final static String DB = "DB";
	
	/** The marker used for the queries */
	public final static String MARKER = "MARKER";

	/** The common query map file */
	static final String COMMON_MAP_FILE = "query_map_common.xml";

	/** The porperty map containing the mapping */
	protected final Properties propQueryMapping = new Properties();
	
	/** The constant to pull the create server status table query */
	protected final static String QUERY_CREATE_SERVER_STATUS_TABLE = "CREATE_SERVER_STATUS_TABLE";

	/** The constant to pull the create server information table query */
	protected final static String QUERY_CREATE_SERVER_INFO_TABLE = "CREATE_SERVER_INFO_TABLE";

	/** The constant to pull the create server log table query */
	protected final static String QUERY_CREATE_SERVER_LOG_TABLE = "CREATE_SERVER_LOG_TABLE";

	/** The constant to pull the drop server status table query */
	protected final static String QUERY_DROP_SERVER_STATUS_TABLE = "DROP_SERVER_STATUS_TABLE";

	/** The constant to pull the drop server information table query */
	protected final static String QUERY_DROP_SERVER_INFO_TABLE = "DROP_SERVER_INFO_TABLE";

	/** The constant to pull the drop server log table query */
	protected final static String QUERY_DROP_SERVER_LOG_TABLE = "DROP_SERVER_LOG_TABLE";

	/** The constant to pull the insert server status query */
	protected final static String QUERY_REGISTER_SERVER_STATUS = "INSERT_SERVER_STATUS";
	
	/** The constant to pull the insert info status i */
	protected final static String QUERY_REGISTER_SERVER_INFO = "INSERT_SERVER_INFO";

	/** The constant to pull the delete server status query */
	protected final static String QUERY_UNREGISTER_SERVER_STATUS = "DELETE_SERVER_STATUS";

	/** The constant to pull the delete server status query */
	protected final static String QUERY_UNREGISTER_SERVER_INFO = "DELETE_SERVER_INFO";
	
	/** The constant to pull the unreister log entry query */
	protected final static String QUERY_UNREGISTER_SERVER_LOG = "LOG_SERVER_UNREGISTER";
	
	/** The connection to the data base backend */
	private Connection conn = null;

	/** True if the connection is not on auto commit */
	private boolean bTransactional = false;
	
	/** The port where the coordinated service runs */
	private int iPort = -1;
	
	/** The server ID */
	private String sServerID = null;
	
	/** Initialize the query map */
	public BackendAdapter() {
		try {
			propQueryMapping.loadFromXML(DerbyBackendAdapter.class.getResourceAsStream(COMMON_MAP_FILE));
		} catch (Exception e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			KernelLoggerFactory.getCoreLogger().severe("Derby query map missing! "+baos.toString());
		}
	}
	
	/** Links this backend adapter to the given store.
	 * 
	 * @param conn The connection to the backend store
	 * @param iPort The port number where the coordinated service runs
	 */
	public void linkToConnectionAndPort(Connection conn, int iPort) {
		// Store the configuration objects
		
		// Get the connection object and the transaction mode
		try {
			this.conn = conn;
			this.bTransactional = !conn.getAutoCommit();
			this.iPort = iPort;
			this.sServerID = NetworkTools.getNumericIPValue()+Integer.toHexString(iPort).toUpperCase();
		} catch (Exception e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			KernelLoggerFactory.getCoreLogger().severe("Error stablishind connection and transaction check! "+baos.toString());
		}
		
		// Add the shutdown hook
		Runtime.getRuntime().addShutdownHook(
				new BackendAdapterShutdownHook(this)
			);
	}
	
	/** Creates the required schema if it does not exist. 
	 * 
	 * @throws BackendAdapterException Thrown when the connection to the back end could not be retrieved
	 */
	public void createSchema() 
	throws BackendAdapterException {
		
		try {
			// Create the server status table
			String sQueryCSST = propQueryMapping.getProperty(QUERY_CREATE_SERVER_STATUS_TABLE);
			executeUpdateQuery(sQueryCSST);
			
			// Create the server info table
			String sQueryCSIT = propQueryMapping.getProperty(QUERY_CREATE_SERVER_INFO_TABLE);
			executeUpdateQuery(sQueryCSIT);
			
			// Create the server info table
			String sQueryCSLT = propQueryMapping.getProperty(QUERY_CREATE_SERVER_LOG_TABLE);
			executeUpdateQuery(sQueryCSLT);
			
			registerServer();
			
			// Commit the transaction
			if ( bTransactional ) conn.commit();
			
		} catch (SQLException e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			KernelLoggerFactory.getCoreLogger().severe("Commit operation failed! "+baos.toString());
		}
		catch ( BackendAdapterException bae ) {
			try {
				// Roll it back
				if ( bTransactional ) conn.rollback();
				throw bae;
			} catch (SQLException e) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				e.printStackTrace(new PrintStream(baos));
				KernelLoggerFactory.getCoreLogger().severe("Rollback operation failed! "+baos.toString());
			}
		}
	}
	
	/** Deletes the tables created after the schema.
	 * 
	 * @throws BackendAdapterException The tables in the schema could not be dropped
	 */
	public void dropSchema () throws BackendAdapterException {

		try {
			// Drop the server status and info table
			dropSchemaLeavingLogsBehind();
			
			// Drop the server log table
			String sQueryCSLT = propQueryMapping.getProperty(QUERY_DROP_SERVER_LOG_TABLE);
			executeUpdateQuery(sQueryCSLT);

			// Commit the transaction
			if ( bTransactional ) conn.commit();
			
		} catch (SQLException e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			KernelLoggerFactory.getCoreLogger().severe("Commit operation failed! "+baos.toString());
		}
		catch ( BackendAdapterException bae ) {
			try {
				// Roll it back
				if ( bTransactional ) conn.rollback();
				throw bae;
			} catch (SQLException e) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				e.printStackTrace(new PrintStream(baos));
				KernelLoggerFactory.getCoreLogger().severe("Rollback operation failed! "+baos.toString());
			}
		}
	
	}
	
	/** Deletes the tables created after the schema except the login tables so they 
	 * can be used for diagnostics.
	 * 
	 * @throws BackendAdapterException The tables in the schema could not be dropped
	 */
	public void dropSchemaLeavingLogsBehind () throws BackendAdapterException {

		try {
			// Drop the server status table
			String sQueryCSST = propQueryMapping.getProperty(QUERY_DROP_SERVER_STATUS_TABLE);
			executeUpdateQuery(sQueryCSST);
			
			// Drop the server info table
			String sQueryCSIT = propQueryMapping.getProperty(QUERY_DROP_SERVER_INFO_TABLE);
			executeUpdateQuery(sQueryCSIT);
			
			// Commit the transaction
			if ( bTransactional ) conn.commit();
			
		} catch (SQLException e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			KernelLoggerFactory.getCoreLogger().severe("Commit operation failed! "+baos.toString());
		}
		catch ( BackendAdapterException bae ) {
			try {
				// Roll it back
				if ( bTransactional ) conn.rollback();
				throw bae;
			} catch (SQLException e) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				e.printStackTrace(new PrintStream(baos));
				KernelLoggerFactory.getCoreLogger().severe("Rollback operation failed! "+baos.toString());
			}
		}
	}

	/** Registers the server to the backend store
	 * 
	 * @throws BackendAdapterException The server could not be registered
	 */
	public void registerServer() throws BackendAdapterException {
		
		try {	
			// Update the status
			updateServerStatus();
			
			// Update the server info
			Runtime rt = Runtime.getRuntime();
			String sQueryICSS = propQueryMapping.getProperty(QUERY_REGISTER_SERVER_INFO);
			String [] sNameAndIP = NetworkTools.getStringNameAndIPValue().split("/");
			Object [] oaValues = {
					sServerID,
					sNameAndIP[1],
					sNameAndIP[0],
					iPort,
					rt.maxMemory(),
					rt.availableProcessors(),
					System.getProperty("os.arch"),
					System.getProperty("os.name"),
					System.getProperty("os.version"),
					System.getProperty("java.version"),
					System.getProperty("java.vm.version"),
					System.getProperty("java.vm.vendor"),
					System.getProperty("user.name"),
					System.getProperty("user.home")
				};
			executeUpdateQueryWithParams(sQueryICSS, oaValues);
			

			// Commit the transaction
			if ( bTransactional ) conn.commit();
			
		} catch (SQLException e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			KernelLoggerFactory.getCoreLogger().severe("Commit operation failed! "+baos.toString());
		}
		catch ( BackendAdapterException bae ) {
			try {
				// Roll it back
				if ( bTransactional ) conn.rollback();
				throw bae;
			} catch (SQLException e) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				e.printStackTrace(new PrintStream(baos));
				KernelLoggerFactory.getCoreLogger().severe("Rollback operation failed! "+baos.toString());
			}
		}
	}
	

	/** Update the server status to the backend store
	 * 
	 * @throws BackendAdapterException The server could not be registered
	 */
	public void updateServerStatus() throws BackendAdapterException {
		try {
			// Update the server status
			String sQueryICSS = propQueryMapping.getProperty(QUERY_REGISTER_SERVER_STATUS);
			long lTimestamp = System.currentTimeMillis();
			Object [] oaValues = {
					sServerID,
					"R",
					lTimestamp,
					Runtime.getRuntime().freeMemory(),
					"R",
					lTimestamp
				};
			executeUpdateQueryWithParams(sQueryICSS, oaValues);
			
			// Commit the transaction
			if ( bTransactional ) conn.commit();
			
		} catch (SQLException e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			KernelLoggerFactory.getCoreLogger().severe("Commit operation failed! "+baos.toString());
		}
		catch ( BackendAdapterException bae ) {
			try {
				// Roll it back
				if ( bTransactional ) conn.rollback();
				throw bae;
			} catch (SQLException e) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				e.printStackTrace(new PrintStream(baos));
				KernelLoggerFactory.getCoreLogger().severe("Rollback operation failed! "+baos.toString());
			}
		}
	}

	
	/** Unregisters the server from the backend store
	 * 
	 * @throws BackendAdapterException The server could not be unregistered
	 */
	public void unregisterServer() throws BackendAdapterException {
		
		try {
			
			// Get server ID
			Object [] oaValues = {
					sServerID
				};
			
			// Log the unregister operation
			String sQueryLUO = propQueryMapping.getProperty(QUERY_UNREGISTER_SERVER_LOG);
			executeUpdateQueryWithParams(sQueryLUO, oaValues);
				
			// Delete the server status
			String sQueryDCSS = propQueryMapping.getProperty(QUERY_UNREGISTER_SERVER_STATUS);
			executeUpdateQueryWithParams(sQueryDCSS, oaValues);
			
			// Delete server information
			String sQueryDCSI = propQueryMapping.getProperty(QUERY_UNREGISTER_SERVER_INFO);
			executeUpdateQueryWithParams(sQueryDCSI, oaValues);	

			// Commit the transaction
			if ( bTransactional ) conn.commit();
			
		} catch (SQLException e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			KernelLoggerFactory.getCoreLogger().severe("Commit operation failed! "+baos.toString());
		}
		catch ( BackendAdapterException bae ) {
			try {
				// Roll it back
				if ( bTransactional ) conn.rollback();
				throw bae;
			} catch (SQLException e) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				e.printStackTrace(new PrintStream(baos));
				KernelLoggerFactory.getCoreLogger().severe("Rollback operation failed! "+baos.toString());
			}
		}
	
	}
	
	/** Runs and update query against the backend.
	 * 
	 * @param sQuery The query to run the update
	 * @throws BackendAdapterException Thrown when there was a problem with the backend running the update
	 */
	private void executeUpdateQuery(String sQuery)
	throws BackendAdapterException {
		
		// Run the update
		try {
			Statement stm = conn.createStatement();
			stm.executeUpdate(sQuery);
		} catch (SQLException e) {
			throw new BackendAdapterException(e);
		}
	}
	
	/** Runs an update prepare statement with the given values.
	 * 
	 * @param sQuery The query to run
	 * @param oaValues The values to use for the update
	 * @throws BackendAdapterException Something when wrong running the update
	 */
	private void executeUpdateQueryWithParams(String sQuery, Object[] oaValues ) 
	throws BackendAdapterException {
		
		// Run the update
		try {
			PreparedStatement pstm = conn.prepareStatement(sQuery);
			for ( int i=1,iMax=oaValues.length ; i<=iMax ; i++ )
				pstm.setObject(i, oaValues[i-1]);
			pstm.execute();
		} catch (SQLException e) {
			throw new BackendAdapterException(e);
		}
	}

	/** Returns the backend flavor. The flavor corresponds to the name of
	 * the relational database backend used (e.g. Derby, MySQL, Oracle, etc.)
	 * 
	 * @return The flavor of the backend
	 */
	public String getFlavor () {
		return propQueryMapping.getProperty(DB);
	}
	
	/** Returns the character used to identify substitutions on the 
	 * templates contained in the query mapping
	 * 
	 * @return The marker for substitutions
	 */
	public String getMarker () {
		return propQueryMapping.getProperty(MARKER);
	}
	
	/** Return the number of queries stored in the adapter.
	 * 
	 * @return The number of queries
	 */
	public int getNumberOfQueries () {
		return propQueryMapping.size()-2;
	}

	/** This methos is only provided to the shutdown hook to allow it
	 * to properly close the connection to the backend storage system.
	 * 
	 * @throws BackendAdapterException The connection could not be properly closed
	 * 
	 */
	void close() throws BackendAdapterException {
		try {
			conn.close();
		} catch (SQLException e) {
			throw new BackendAdapterException(e);
		}
	}
	
	
}
