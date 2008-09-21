/**
 * 
 */
package org.meandre.core.services.coordinator.backend;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.Properties;

import org.meandre.core.logger.KernelLoggerFactory;

/** Implements the Derby backend adapter. This adapter translates
 * the requests into the proper Derby friendly queries.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class MySQLBackendAdapter 
extends BackendAdapter {
	
	/** The name of the resource file containing the query mapping */
	static final String QUERY_MAP_FILE = "query_map_mysql.xml";

	/** Initialize the query map */
	public MySQLBackendAdapter() {
		super();
		try {
			Properties props = new Properties();
			props.loadFromXML(DerbyBackendAdapter.class.getResourceAsStream(QUERY_MAP_FILE));
			super.propQueryMapping.putAll(props);
		} catch (Exception e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			KernelLoggerFactory.getCoreLogger().severe("MySQL query map missing! "+baos.toString());
		}
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

			// Create the server properties table
			String sQueryCSPT = propQueryMapping.getProperty(QUERY_CREATE_SERVER_PROPERTIES_TABLE);
			executeUpdateQuery(sQueryCSPT);
			
			// Create the server log table
			String sQueryCSLT = propQueryMapping.getProperty(QUERY_CREATE_SERVER_LOG_TABLE);
			executeUpdateQuery(sQueryCSLT);
			
			registerServer(true);
			logServerStatusEventUncommited(sServerID,STATUS_INITIALIZED);
			
			// Commit the transaction
			if ( bTransactional ) conn.commit();
			
			// Insert the default properties to the the properties table
			try {
				String sQueryCSPI = propQueryMapping.getProperty(QUERY_INSERT_DEFAULT_SERVER_PROPERTIES);
				executeUpdateQuery(sQueryCSPI);
				// Commit the transaction
				if ( bTransactional ) conn.commit();
			}
			catch ( BackendAdapterException bae ) {
				// Roll it back
				if ( bTransactional ) conn.rollback();
				log.warning(getName()+" found that default properties are already defined. Skipping adding them again");
			}
			
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


}
