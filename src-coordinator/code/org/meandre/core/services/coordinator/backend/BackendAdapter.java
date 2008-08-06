/**
 * 
 */
package org.meandre.core.services.coordinator.backend;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.Properties;
import java.util.logging.Logger;

import org.meandre.core.logger.KernelLoggerFactory;
import org.meandre.core.services.coordinator.logger.CoordinatorLoggerFactory;
import org.meandre.core.utils.NetworkTools;

/** The base class for the backend adapters.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public abstract class BackendAdapter 
extends Thread {

	/** The server was just registered */
	public static final String STATUS_INITIALIZED = "I";

	/** The server is running */
	public static final String STATUS_RUNNING = "R";
	
	/** The server was unregistered */
	public static final String STATUS_UNREGISTERED = "U";
	
	/** The server was shutdown */
	public static final String STATUS_SHUTDOWN = "D";

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
	
	/** The constant to pull the update server status query */
	protected final static String QUERY_UPDATE_SERVER_STATUS = "UPDATE_SERVER_STATUS";
	
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
	
	/** The service description */
	private String sDesc = null;

	/** The shutdown hook */
	private static Hashtable<BackendAdapter,BackendAdapterShutdownHook> beashMap = new Hashtable<BackendAdapter,BackendAdapterShutdownHook>();

	/** The flag that controls the finalization of the thread process. */
	private boolean bNotDone = true;

	/** The update period for the thread process. */
	private long lUpdatePeriod = 5000;

	/** The logger to use */
	private Logger log = null;
	
	/** Initialize the query map */
	public BackendAdapter() {
		
		log = CoordinatorLoggerFactory.getCoordinatorLogger();
		
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
	 * @param sDesc
	 */
	public void linkToService(Connection conn, int iPort, String sDesc) {
				
		// Get the connection object and the transaction mode
		try {
			this.conn = conn;
			this.bTransactional = !conn.getAutoCommit();
			this.iPort = iPort;
			this.sServerID = NetworkTools.getNumericIPValue()+Integer.toHexString(iPort).toUpperCase();
			this.sDesc = sDesc;

			// Set the thread name
			super.setName("COORD:"+sServerID);
			
			log.info(sServerID+" linked to "+sDesc);
			
		} catch (Exception e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			log.severe("Error stablishind connection and transaction check! "+baos.toString());
		}
		
		// Add the shutdown hook
		if ( beashMap.get(this)==null ) {
			BackendAdapterShutdownHook beash = new BackendAdapterShutdownHook(this);
			beashMap.put(this, beash);
			Runtime.getRuntime().addShutdownHook(beash);
		}
	}
	
	/** Return the shutdown hook for this adapter.
	 * 
	 * @return The back end adapter shutdown hook
	 */
	public BackendAdapterShutdownHook getShutdownHook() {
		return beashMap.get(this);
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
			
			log.fine(sServerID+" created schema");
			
		} catch (SQLException e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			log.severe("Commit operation failed! "+baos.toString());
		}
		catch ( BackendAdapterException bae ) {
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
			
			log.fine(sServerID+" droped the schema");
			
		} catch (SQLException e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			log.severe("Commit operation failed! "+baos.toString());
		}
		catch ( BackendAdapterException bae ) {
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
			
			log.fine(sServerID+" droped the schema leaving the log behind");
			
		} catch (SQLException e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			log.severe("Commit operation failed! "+baos.toString());
		}
		catch ( BackendAdapterException bae ) {
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

	/** Registers the server to the backend store
	 * 
	 * @throws BackendAdapterException The server could not be registered
	 */
	public void registerServer() throws BackendAdapterException {
		
		try {	
			// Update the status
			updateServerStatus(STATUS_INITIALIZED);
			
			// Update the server info
			Runtime rt = Runtime.getRuntime();
			String sQueryICSS = propQueryMapping.getProperty(QUERY_REGISTER_SERVER_INFO);
			String [] sNameAndIP = NetworkTools.getStringNameAndIPValue().split("/");
			Object [] oaValues = {
					sServerID,
					sNameAndIP[1],
					sNameAndIP[0],
					iPort,
					sDesc,
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
			
			log.fine(sServerID+" server registered");
			
		} catch (SQLException e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			log.severe("Commit operation failed! "+baos.toString());
		}
		catch ( BackendAdapterException bae ) {
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
	

	/** Update the server status to the backend store
	 * 
	 * @param sStatus The status value to update
	 * @throws BackendAdapterException The server could not be registered
	 */
	public void updateServerStatus( String sStatus ) throws BackendAdapterException {
		try {
			// Update the server status
			String sQueryUCSS = propQueryMapping.getProperty(QUERY_UPDATE_SERVER_STATUS);
			long lTimestamp = System.currentTimeMillis();
			Object [] oaValuesUpdate = {
					STATUS_RUNNING,
					lTimestamp,
					sServerID
				};
			if ( executeUpdateQueryWithParams(sQueryUCSS, oaValuesUpdate) <= 0 ) {
				//Server does not exist
				String sQueryICSS = propQueryMapping.getProperty(QUERY_REGISTER_SERVER_STATUS);
				Object [] oaValues = {
						sServerID,
						STATUS_INITIALIZED,
						lTimestamp,
						Runtime.getRuntime().freeMemory()
					};
				executeUpdateQueryWithParams(sQueryICSS, oaValues);
			}
			
			// Commit the transaction
			if ( bTransactional ) conn.commit();
			
			log.fine(sServerID+" updated its status");
			
		} catch (SQLException e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			log.severe("Commit operation failed! "+baos.toString());
		}
		catch ( BackendAdapterException bae ) {
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
			
			// Update the server status before going down
			updateServerStatus(STATUS_UNREGISTERED);
			
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
			
			log.fine(sServerID+" unregistered himself");
						
		} catch (SQLException e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			log.severe("Commit operation failed! "+baos.toString());
		}
		catch ( BackendAdapterException bae ) {
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
	private int executeUpdateQueryWithParams(String sQuery, Object[] oaValues ) 
	throws BackendAdapterException {
		
		// Run the update
		try {
			PreparedStatement pstm = conn.prepareStatement(sQuery);
			for ( int i=1,iMax=oaValues.length ; i<=iMax ; i++ )
				pstm.setObject(i, oaValues[i-1]);
			return pstm.executeUpdate();
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

	/** Sets the update period of the heart beat
	 * 
	 * @param iUpdatePeriod The period to set
	 */
	public void setUpdatePeriod(long iUpdatePeriod) {
		this.lUpdatePeriod = iUpdatePeriod;
	}

	/** Gets the update period of the heart beat
	 * 
	 * @return the iUpdatePeriod The period to set
	 */
	public long getUpdatePeriod() {
		return lUpdatePeriod;
	}

	/** This methos is only provided to the shutdown hook to allow it
	 * to properly close the connection to the backend storage system.
	 * 
	 * @throws BackendAdapterException The connection could not be properly closed
	 * 
	 */
	void close() 
	throws BackendAdapterException {
		try {
			this.bNotDone = false;
			unregisterServer();
			conn.close();
			log.info(sServerID+" shutdown");
		} catch (SQLException e) {
			throw new BackendAdapterException(e);
		}
	}
	
	/** The thread process that handles the distribution via the backend
	 * connection to the data store. This thread periodically updates the
	 * status (heart beat) and also does consistency check if unresponsive 
	 * nodes arise.
	 */
	public void run () {
		log.info("Starting "+super.getName());
		while ( bNotDone ) {
			// Sleep
			try {
				try {
					updateServerStatus(STATUS_RUNNING);
				} catch (BackendAdapterException e) {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					e.printStackTrace(new PrintStream(baos));
					log.warning(sServerID+" deamon thread could not update its status. "+baos.toString());
				}
				
				Thread.sleep(this.lUpdatePeriod);
				
			} catch (InterruptedException e) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				e.printStackTrace(new PrintStream(baos));
				log.warning(sServerID+" deamon thread was abruptly interrupted. "+baos.toString());
			}
			
			// TODO Do something
			if ( bNotDone )  {
				
			}
		}
		log.info("Coordinator "+super.getName()+" has stopped");
		
	}
	
}
