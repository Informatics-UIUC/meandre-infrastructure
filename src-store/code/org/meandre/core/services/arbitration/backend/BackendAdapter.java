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

import org.meandre.configuration.CoreConfiguration;
import org.meandre.core.logger.KernelLoggerFactory;
import org.meandre.core.store.Store;
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
	
	/** The store to be used with this backend adapter */
	private Store store  = null;

	/** The core configuration object used by this backend adapter */
	private CoreConfiguration cnf = null;
	
	/** The constant to pull the create server status table query */
	protected final static String QUERY_CREATE_SERVER_STATUS_TABLE = "CREATE_SERVER_STATUS_TABLE";
	
	/** The constant to pull the create server information table query */
	protected final static String QUERY_CREATE_SERVER_INFO_TABLE = "CREATE_SERVER_INFO_TABLE";

	/** The constant to pull the drop server status table query */
	protected final static String QUERY_DROP_SERVER_STATUS_TABLE = "DROP_SERVER_STATUS_TABLE";
	
	/** The constant to pull the drop server information table query */
	protected final static String QUERY_DROP_SERVER_INFO_TABLE = "DROP_SERVER_INFO_TABLE";

	/** The constant to pull the insert server status query */
	protected final static String QUERY_REGISTER_SERVER_STATUS = "INSERT_SERVER_STATUS";
	
	/** The constant to pull the insert info status i */
	protected final static String QUERY_REGISTER_SERVER_INFO = "INSERT_SERVER_INFO";
	
	/** The constant to pull the delete server status query */
	protected final static String QUERY_UNREGISTER_SERVER_STATUS = "DELETE_SERVER_STATUS";
	
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
	 * @param cnf The core configuration object
	 * @param store The store to use
	 */
	public void linkToCoreAndStore( CoreConfiguration cnf, Store store ) {
		// Store the configuration objects
		this.cnf = cnf;
		this.store = store;
		
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
		
		// Create the server status table
		String sQueryCSST = propQueryMapping.getProperty(QUERY_CREATE_SERVER_STATUS_TABLE);
		executeUpdateQuery(sQueryCSST);
		
		// Create the server info table
		String sQueryCSIT = propQueryMapping.getProperty(QUERY_CREATE_SERVER_INFO_TABLE);
		executeUpdateQuery(sQueryCSIT);
		
		registerServer();
		
	}
	
	/** Deletes the tables created after the schema.
	 * 
	 * @throws BackendAdapterException The tables in the schema could not be dropped
	 */
	public void dropSchema () throws BackendAdapterException {

		// Create the server status table
		String sQueryCSST = propQueryMapping.getProperty(QUERY_DROP_SERVER_STATUS_TABLE);
		executeUpdateQuery(sQueryCSST);
		
		// Create the server info table
		String sQueryCSIT = propQueryMapping.getProperty(QUERY_DROP_SERVER_INFO_TABLE);
		executeUpdateQuery(sQueryCSIT);
		
	}

	/** Registers the server to the backend store
	 * 
	 * @throws BackendAdapterException The server could not be registered
	 */
	public void registerServer() throws BackendAdapterException {
		// Update the status
		updateServerStatus();
		
		// Update the server info
		Runtime rt = Runtime.getRuntime();
		String sQueryICSS = propQueryMapping.getProperty(QUERY_REGISTER_SERVER_INFO);
		String [] sNameAndIP = NetworkTools.getStringNameAndIPValue().split("/");
		Object [] oaValues = {
				NetworkTools.getNumericIPValue()+Integer.toHexString(cnf.getBasePort()).toUpperCase(),
				sNameAndIP[1],
				sNameAndIP[0],
				cnf.getBasePort(),
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
	

	/** Update the server status to the backend store
	 * 
	 * @throws BackendAdapterException The server could not be registered
	 */
	public void updateServerStatus() throws BackendAdapterException {
		// Update the server status
		String sQueryICSS = propQueryMapping.getProperty(QUERY_REGISTER_SERVER_STATUS);
		long lTimestamp = System.currentTimeMillis();
		Object [] oaValues = {
				NetworkTools.getNumericIPValue()+Integer.toHexString(cnf.getBasePort()).toUpperCase(),
				"U",
				lTimestamp,
				Runtime.getRuntime().freeMemory(),
				"U",
				lTimestamp
			};
		executeUpdateQueryWithParams(sQueryICSS, oaValues);
	}

	
	/** Unregisters the server from the backend store
	 * 
	 * @throws BackendAdapterException The server could not be unregistered
	 */
	public void unregisterServer() throws BackendAdapterException {
		// Update the server status
		String sQueryDCSS = propQueryMapping.getProperty(QUERY_UNREGISTER_SERVER_STATUS);
		Object [] oaValues = {
				NetworkTools.getNumericIPValue()+Integer.toHexString(cnf.getBasePort()).toUpperCase()
			};
		executeUpdateQueryWithParams(sQueryDCSS, oaValues);
		
		// TODO Unregister the information of the server
	}
	
	/** Fetch the current connection object to the backend storage.
	 * 
	 * @return The connection object
	 * @throws BackendAdapterException Thrown when the connection object could not be retrieved
	 */
	private Connection getConnectionObject () 
	throws BackendAdapterException {
		Connection conn = store.getConnectionToDB();
		if ( conn==null )
			throw new BackendAdapterException("The connection to the store backend could not be retrieved");
		return conn;
	}

	/** Runs and update query against the backend.
	 * 
	 * @param sQuery The query to run the update
	 * @throws BackendAdapterException Thrown when there was a problem with the backend running the update
	 */
	private void executeUpdateQuery(String sQuery)
	throws BackendAdapterException {
		
		// Get the connection and check it is OK
		Connection conn = getConnectionObject();
		
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
		// Get the connection and check it is OK
		Connection conn = getConnectionObject();
		
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
	
	
}
