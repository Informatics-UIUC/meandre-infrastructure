/**
 * 
 */
package org.meandre.core.services.coordinator.backend;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import org.meandre.core.logger.KernelLoggerFactory;
import org.meandre.core.services.coordinator.CoordinatorServiceCallBack;
import org.meandre.core.services.coordinator.logger.CoordinatorLoggerFactory;
import org.meandre.core.utils.NetworkTools;

/** The base class for the backend adapters.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public abstract class BackendAdapter 
extends Thread {

	/** The heartbeat update rate */
	private static final String PROP_HEARTBEAT_RATE = "HEARTBEAT_RATE";
	
	/** The maximun number of update failures */
	private static final String PROP_MAX_UPDATE_FAILURES = "MAX_UPDATE_FAILURES";

	/** The server was just registered */
	public static final String STATUS_INITIALIZED = "I";

	/** The server is running */
	public static final String STATUS_RUNNING = "R";
	
	/** The server was unregistered */
	public static final String STATUS_UNREGISTERED = "U";

	/** The server was shutdown */
	public static final String STATUS_SHUTDOWN = "S";

	/** The server was assumed shutdown */
	public static final String STATUS_DIRTY_SHUTDOWN = "D";

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
	
	/** The constant to pull the create server information table query */
	protected final static String QUERY_CREATE_SERVER_PROPERTIES_TABLE = "CREATE_SERVER_PROPERTIES_TABLE";

	/** The constant to pull the create server log table query */
	protected final static String QUERY_CREATE_SERVER_LOG_TABLE = "CREATE_SERVER_LOG_TABLE";

	protected final static String QUERY_INSERT_DEFAULT_SERVER_PROPERTIES = "INSERT_DEFAULT_SERVER_PROPERTIES";
	
	/** The constant to pull the drop server status table query */
	protected final static String QUERY_DROP_SERVER_STATUS_TABLE = "DROP_SERVER_STATUS_TABLE";

	/** The constant to pull the drop server information table query */
	protected final static String QUERY_DROP_SERVER_INFO_TABLE = "DROP_SERVER_INFO_TABLE";

	/** The constant to pull the drop server properties table query */
	protected final static String QUERY_DROP_SERVER_PROPERTIES_TABLE = "DROP_SERVER_PROPERTIES_TABLE";

	/** Gets the server properties */
	protected final static String QUERY_GET_SERVER_PROPERTIES = "GET_SERVER_PROPERTIES";

	/** The constant to pull the drop server log table query */
	protected final static String QUERY_DROP_SERVER_LOG_TABLE = "DROP_SERVER_LOG_TABLE";

	/** The constant to pull the insert server status query */
	protected final static String QUERY_REGISTER_SERVER_STATUS = "INSERT_SERVER_STATUS";
	
	/** The constant to pull the delete server status query */
	protected final static String QUERY_DELETE_SERVER_STATUS = "DELETE_SERVER_STATUS";

	/** The constant to pull the update server status query */
	protected final static String QUERY_UPDATE_SERVER_STATUS = "UPDATE_SERVER_STATUS";
	
	/** The constant to pull the update server status in presence of a failure query */
	protected final static String QUERY_UPDATE_SERVER_STATUS_FAILURE = "UPDATE_SERVER_STATUS_FAILURE";
	
	/** The constant to pull the insert info status */
	protected final static String QUERY_REGISTER_SERVER_INFO = "INSERT_SERVER_INFO";
	
	/** The constant to pull the delete server status query */
	protected final static String QUERY_DELETE_SERVER_INFO = "DELETE_SERVER_INFO";

	/** The constant to pull the update info  */
	protected final static String QUERY_UPDATE_SERVER_INFO = "UPDATE_SERVER_INFO";

	/** The constant to pull the delete server status query */
	protected final static String QUERY_UNREGISTER_SERVER_STATUS = "DELETE_SERVER_STATUS";

	/** The constant to pull the delete server status query */
	protected final static String QUERY_UNREGISTER_SERVER_INFO = "DELETE_SERVER_INFO";
	
	/** The constant to pull the unregister log entry query */
	protected final static String QUERY_LOG_SERVER_EVENT = "LOG_SERVER_EVENT";
	
	/** The constant to pull the query that marks out of sync servers */
	protected final static String QUERY_MARK_OUTOFSYNC_SERVERS = "MARK_OUTOFSYNC_SERVERS";
	
	/** The constant to pull all the entries in the query table */
	protected final static String QUERY_DUMP_LOG_ENTRIES = "DUMP_LOG_ENTRIES";
	
	/** The constant to pull the list the dead nodes */
	protected final static String QUERY_LIST_DIRTY_NODES = "LIST_DIRTY_NODES";
	
	/** Selects the number of failed updates */
	protected final static String QUERY_SELECT_FAILED_UPDATE_COUNTS = "SELECT_FAILED_UPDATE_COUNTS";
	
	/** The connection to the data base backend */
	protected Connection conn = null;

	/** True if the connection is not on auto commit */
	protected boolean bTransactional = false;
	
	/** The port where the coordinated service runs */
	private int iPort = -1;
	
	/** The server ID */
	protected String sServerID = null;
	
	/** The service description */
	private String sDesc = null;

	/** The shutdown hook */
	private static Hashtable<BackendAdapter,BackendAdapterShutdownHook> beashMap = new Hashtable<BackendAdapter,BackendAdapterShutdownHook>();

	/** The flag that controls the finalization of the thread process. */
	private boolean bNotDone = true;

	/** The update period for the thread process. */
	private long lUpdatePeriod = 5000;
	
	/** The maximum number of failures allows */
	private int iMaxFailures = 5;
	
	/** The server properties */
	private Properties propsServer = new Properties();

	/** The logger to use */
	protected Logger log = null;

	/** The coordinated service call back interface */
	private CoordinatorServiceCallBack aspcbCallBack;
	
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
	 * @param aspcb The service callback
	 */
	public void linkToService(Connection conn, int iPort, CoordinatorServiceCallBack aspcb ) {
				
		// Get the connection object and the transaction mode
		try {
			this.conn = conn;
			this.bTransactional = !conn.getAutoCommit();
			this.iPort = iPort;
			this.sServerID = NetworkTools.getNumericIPValue()+Integer.toHexString(iPort).toUpperCase();
			this.sDesc = aspcb.getDescription();
			this.aspcbCallBack = aspcb;

			// Set the thread name
			super.setName("MCT:"+sServerID);
			
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
			log.info(getName()+" registered shutdown hook");
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
	public abstract void createSchema() throws BackendAdapterException ;
	
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
			
			// Drop the server properties table
			String sQueryCSPT = propQueryMapping.getProperty(QUERY_DROP_SERVER_PROPERTIES_TABLE);
			executeUpdateQuery(sQueryCSPT);
			
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
	

	/** Loads the server propoerties the server to the backend store
	 * 
	 * @throws BackendAdapterException The server properties could not be retrieved
	 */
	private void updateServerPropertiesUncommited() throws BackendAdapterException {
		
		try {
			String sQuery = propQueryMapping.getProperty(QUERY_GET_SERVER_PROPERTIES);
			Statement stm = conn.createStatement();
			ResultSet rs = stm.executeQuery(sQuery);
			propsServer.clear();
			while ( rs.next() ) {
				propsServer.put(rs.getString(1).toString().trim(), rs.getObject(2).toString().trim());
			}
			// Update the properties
			lUpdatePeriod = Long.parseLong(propsServer.getProperty(PROP_HEARTBEAT_RATE));
			iMaxFailures = Integer.parseInt(propsServer.getProperty(PROP_MAX_UPDATE_FAILURES));
		} catch (SQLException e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			log.severe("Get server properties failed! "+baos.toString());
		}
	}
	

	/** Registers the server to the backend store
	 * 
	 * @param bCreate Create the entry if needed, update otherwise
	 * @throws BackendAdapterException The server could not be registered
	 */
	public void registerServer ( boolean bCreate ) throws BackendAdapterException {
		
		try {	
			// Log the status to dirty if status was left behind
			logServerStatusEventUncommited(sServerID,STATUS_DIRTY_SHUTDOWN);
			
			// Update the status
			updateServerStatus(STATUS_INITIALIZED);
			
			// Update the server info
			updateServerInfo();			

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
					sStatus,
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

	/** Update the server status to the backend store
	 *
	 * @throws BackendAdapterException The server could not be registered
	 */
	public void updateServerInfo() throws BackendAdapterException {
		try {
			// Update the server status
			Runtime rt = Runtime.getRuntime();
			String sQueryUCSI = propQueryMapping.getProperty(QUERY_UPDATE_SERVER_INFO);
			String [] sNameAndIP = NetworkTools.getStringNameAndIPValue().split("/");
			Object [] oaValuesUpdate = {
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
					System.getProperty("user.home"),
					sServerID
				};
			if ( executeUpdateQueryWithParams(sQueryUCSI, oaValuesUpdate) <= 0 ) {
				//Server does not exist
				String sQueryICSS = propQueryMapping.getProperty(QUERY_REGISTER_SERVER_INFO);
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

	/** Update the server status to the backend store
	 * 
	 * @param sStatus The status value to update
	 * @throws BackendAdapterException The server could not be registered
	 */
	protected void updateDirtyServerStatusUncommitedTransaction( String sServerID, String sStatus ) 
	throws BackendAdapterException {
		// Update the server status
		String sQueryUCSS = propQueryMapping.getProperty(QUERY_UPDATE_SERVER_STATUS_FAILURE);
		long lTimestamp = System.currentTimeMillis();
		Object [] oaValuesUpdate = {
				sStatus,
				lTimestamp,
				sServerID
			};			
		executeUpdateQueryWithParams(sQueryUCSS, oaValuesUpdate);
		
		
		// Check if it is beyond the maximum allowed failures
		String sQuerySFUC = propQueryMapping.getProperty(QUERY_SELECT_FAILED_UPDATE_COUNTS);
		Object [] oaValuesSelect = { sServerID };
		List<List<String>> lst = selectTextColumns(sQuerySFUC,oaValuesSelect);
		
		if ( lst.size()>0 ) {
			if ( iMaxFailures<Integer.parseInt(lst.get(0).get(0)) ) {
				String sQueryLUO = propQueryMapping.getProperty(QUERY_LOG_SERVER_EVENT);
				String sQueryDSS = propQueryMapping.getProperty(QUERY_DELETE_SERVER_STATUS);
				String sQueryDSI = propQueryMapping.getProperty(QUERY_DELETE_SERVER_INFO);
				Object [] oaValues = {
						sServerID
					};
				// Log the unregister operation
				executeUpdateQueryWithParams(sQueryLUO, oaValues);
				// Delete the entries for status and info
				executeUpdateQueryWithParams(sQueryDSS, oaValues);
				executeUpdateQueryWithParams(sQueryDSI, oaValues);
				
				log.warning("Server "+sServerID+" marked as unreacheble");
			}
		}
			
	}

	/** Unregisters the server from the backend store
	 * 
	 * @throws BackendAdapterException The server could not be unregistered
	 */
	public void unregisterServer() throws BackendAdapterException {
		try {
			unregisterServerUncommited(sServerID,STATUS_UNREGISTERED);
			
			// Commit the transaction
			if ( bTransactional ) conn.commit();
			
			log.fine(sServerID+" unregistered itself");
						
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
	protected void unregisterServerUncommited( String sID, String sStatus ) throws BackendAdapterException {
					
		// Get server ID
		Object [] oaValues = {
				sID
			};
		
		// Update the server status before going down
		updateServerStatus(sStatus);
		
		// Log the unregister operation
		String sQueryLUO = propQueryMapping.getProperty(QUERY_LOG_SERVER_EVENT);
		executeUpdateQueryWithParams(sQueryLUO, oaValues);
			
		// Delete the server status
		String sQueryDCSS = propQueryMapping.getProperty(QUERY_UNREGISTER_SERVER_STATUS);
		executeUpdateQueryWithParams(sQueryDCSS, oaValues);
		
		// Delete server information
		String sQueryDCSI = propQueryMapping.getProperty(QUERY_UNREGISTER_SERVER_INFO);
		executeUpdateQueryWithParams(sQueryDCSI, oaValues);	
	
	}
	
	
	/** Logs a server event to the backend store
	 * 
	 * @param sID The server ID
	 * @param sStatus The status to log
	 * @throws BackendAdapterException The server could not be unregistered
	 */
	protected void logServerStatusEventUncommited( String sID, String sStatus ) throws BackendAdapterException {
		
		String sQueryUCSS = propQueryMapping.getProperty(QUERY_UPDATE_SERVER_STATUS);
		long lTimestamp = System.currentTimeMillis();
		Object [] oaValuesUpdate = {
				sStatus,
				lTimestamp,
				sServerID
			};
		executeUpdateQueryWithParams(sQueryUCSS, oaValuesUpdate);
				
		// Get server ID
		Object [] oaValues = {
				sID
			};
		
		// Log the server status operation
		String sQueryLUO = propQueryMapping.getProperty(QUERY_LOG_SERVER_EVENT);
		executeUpdateQueryWithParams(sQueryLUO, oaValues);
		
	}
	
	/** Runs and update query against the backend.
	 * 
	 * @param sQuery The query to run the update
	 * @throws BackendAdapterException Thrown when there was a problem with the backend running the update
	 */
	protected void executeUpdateQuery(String sQuery)
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

	/** Return a list of list with the results of select query in text form.
	 * 
	 * @param sQuery The query to run
	 * @return The resulting list of lists of text
	 * @throws BackendAdapterException Something when wrong running the select
	 */
	private List<List<String>> selectTextColumns (String sQuery ) throws BackendAdapterException {
		List<List<String>> lstRes = new LinkedList<List<String>>();
		try {
			Statement pstm = conn.createStatement();
			ResultSet rs = pstm.executeQuery(sQuery);
			int iColCount = rs.getMetaData().getColumnCount();
			
			while(rs.next()) {
				List<String> lstRow = new LinkedList<String>();
				for( int j=1 ; j<=iColCount ; j++) { 
					lstRow.add(rs.getString(j));
				}
				lstRes.add(lstRow);
			}
		} catch (SQLException e) {
			throw new BackendAdapterException(e);
		}
		return lstRes;
	}


	/** Return a list of list with the results of select query in text form.
	 * 
	 * @param sQuery The query to run
	 * @param oaValues The values to use for the query
	 * @return The resulting list of lists of text
	 * @throws BackendAdapterException Something when wrong running the select
	 */
	private List<List<String>> selectTextColumns (String sQuery, Object [] oaValues) 
	throws BackendAdapterException {
		List<List<String>> lstRes = new LinkedList<List<String>>();
		try {
			PreparedStatement pstm = conn.prepareStatement(sQuery);
			for ( int i=1,iMax=oaValues.length ; i<=iMax ; i++ )
				pstm.setObject(i, oaValues[i-1]);
			ResultSet rs = pstm.executeQuery();
			int iColCount = rs.getMetaData().getColumnCount();
			
			while(rs.next()) {
				List<String> lstRow = new LinkedList<String>();
				for( int j=1 ; j<=iColCount ; j++) { 
					lstRow.add(rs.getString(j));
				}
				lstRes.add(lstRow);
			}
		} catch (SQLException e) {
			throw new BackendAdapterException(e);
		}
		return lstRes;
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

	/** Gets the update period of the heart beat
	 * 
	 * @return the iUpdatePeriod The period to set
	 */
	public long getUpdatePeriod() {
		return lUpdatePeriod;
	}

	/** Returns the maximum number of allowed failures.
	 * 
	 * @return The maximum number of failures
	 */
	public int getMaximunNumberOfFailures() {
		return iMaxFailures;
	}
	
	/** This methos is only provided to the shutdown hook to allow it
	 * to properly close the connection to the backend storage system.
	 * 
	 * @throws BackendAdapterException The connection could not be properly closed
	 * 
	 */
	public void close() 
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
					updateServerPropertiesUncommited();
					updateServerStatus(STATUS_RUNNING);
				} catch (BackendAdapterException e) {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					e.printStackTrace(new PrintStream(baos));
					log.warning(sServerID+" deamon thread could not update its status. "+baos.toString());
				}
								
				if ( bNotDone )  {
					// If smaller than a certain probability
					// TODO: Get the probability update in
					try {
						// 1. run the update
						
						String sQueryUOS = propQueryMapping.getProperty(QUERY_MARK_OUTOFSYNC_SERVERS);
						Object[] oaValues = { lUpdatePeriod/100 };
						executeUpdateQueryWithParams(sQueryUOS, oaValues);
						
						// Commit the transaction
						if ( bTransactional ) conn.commit();
						
						// 2. ping dead guys
						String sQueryLDS = propQueryMapping.getProperty(QUERY_LIST_DIRTY_NODES);
						List<List<String>> lstDirty = selectTextColumns(sQueryLDS);
						for ( List<String> lst:lstDirty )
							if ( !aspcbCallBack.ping(lst.get(1), Integer.parseInt(lst.get(2)))) {
								updateDirtyServerStatusUncommitedTransaction(lst.get(0),STATUS_DIRTY_SHUTDOWN);
								log.fine(sServerID+" marking "+lst.get(0)+" as dirty");
							}
								
						// Commit the transaction
						if ( bTransactional ) conn.commit();
											
					} catch (SQLException e) {
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						e.printStackTrace(new PrintStream(baos));
						log.severe(sServerID+" Commit operation failed! "+baos.toString());
					}
					catch ( BackendAdapterException bae ) {
						try {
							// Roll it back
							if ( bTransactional ) conn.rollback();
						} catch (SQLException e) {
							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							e.printStackTrace(new PrintStream(baos));
							log.severe(sServerID+" Rollback operation failed! "+baos.toString());
						}
					}
				}
	
				// Sleep
				Thread.sleep(this.lUpdatePeriod);
				
			} catch (InterruptedException e) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				e.printStackTrace(new PrintStream(baos));
				log.warning(sServerID+" deamon thread was abruptly interrupted. "+baos.toString());
			}
			
		}
		log.info("Coordinator "+super.getName()+" has stopped");
		
	}
	
}
