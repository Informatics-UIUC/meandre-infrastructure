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
	
	/** The constant to pull the insert server status query */
	protected final static String QUERY_REGISTER_SERVER_STATUS = "INSERT_SERVER_STATUS";
	
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
		
		// Create the status table
		String sQueryCSST = propQueryMapping.getProperty(QUERY_CREATE_SERVER_STATUS_TABLE);
		executeUpdateQuery(sQueryCSST);
		
		registerServer();
		
	}

	/** Registers the server to the backend store
	 * 
	 * @throws BackendAdapterException The server could not be registered
	 */
	public void registerServer() throws BackendAdapterException {
		// Update the server status
		String sQueryICSS = propQueryMapping.getProperty(QUERY_REGISTER_SERVER_STATUS);
		long lTimestamp = System.currentTimeMillis();
		Object [] oaValues = {
				NetworkTools.getNumericIPValue()+Integer.toHexString(cnf.getBasePort()).toUpperCase(),
				"U",
				lTimestamp,
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
