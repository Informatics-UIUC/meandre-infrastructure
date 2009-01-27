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
public class DerbyCoordinatorBackendAdapter 
extends CoordinatorBackendAdapter {
	
	/** The name of the resource file containing the query mapping */
	static final String QUERY_MAP_FILE = "coordinator_query_map_derby.xml";

	/** Initialize the query map */
	public DerbyCoordinatorBackendAdapter() {
		super();
		try {
			Properties props = new Properties();
			props.loadFromXML(DerbyCoordinatorBackendAdapter.class.getResourceAsStream(QUERY_MAP_FILE));
			super.propQueryMapping.putAll(props);
		} catch (Exception e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			KernelLoggerFactory.getCoreLogger().severe("Derby query map missing! "+baos.toString());
		}
	}
	
	/** Creates the required schema if it does not exist. 
	 * 
	 * @throws CoordinatorBackendAdapterException Thrown when the connection to the back end could not be retrieved
	 */
	public void createSchema() 
	throws CoordinatorBackendAdapterException {
		
		try {
			int iFailures = 0;
			CoordinatorBackendAdapterException [] bae = new CoordinatorBackendAdapterException[4];
			
			// Create the server status table
			try {
				String sQueryCSST = propQueryMapping.getProperty(QUERY_CREATE_SERVER_STATUS_TABLE);
				executeUpdateQuery(sQueryCSST);
			} catch (CoordinatorBackendAdapterException e) {
				bae[iFailures++]=e;
			}
			
			// Create the server info table
			try {
				String sQueryCSIT = propQueryMapping.getProperty(QUERY_CREATE_SERVER_INFO_TABLE);
				executeUpdateQuery(sQueryCSIT);
			} catch (CoordinatorBackendAdapterException e) {
				bae[iFailures++]=e;
			}

			// Create the server properties table
			try {
				String sQueryCSPT = propQueryMapping.getProperty(QUERY_CREATE_SERVER_PROPERTIES_TABLE);
				executeUpdateQuery(sQueryCSPT);
			} catch (CoordinatorBackendAdapterException e) {
				bae[iFailures++]=e;
			}
			
			// Create the server log table
			try {
				String sQueryCSLT = propQueryMapping.getProperty(QUERY_CREATE_SERVER_LOG_TABLE);
				executeUpdateQuery(sQueryCSLT);
			} catch (CoordinatorBackendAdapterException e) {
				bae[iFailures++]=e;
			}
			
			registerServer(true);
			logServerStatusEventUncommited(sServerID,STATUS_INITIALIZED);
				
			// Commit the transaction
			if ( bTransactional ) conn.commit();
			
			if ( iFailures==0 ) {
	
				// Insert the default properties to the the properties table
				try {
					String sQueryCSPI = propQueryMapping.getProperty(QUERY_INSERT_DEFAULT_SERVER_PROPERTIES);
					executeUpdateQuery(sQueryCSPI);
					// Commit the transaction
					if ( bTransactional ) conn.commit();
				}
				catch ( CoordinatorBackendAdapterException e ) {
					// Roll it back
					if ( bTransactional ) conn.rollback();
					log.warning(getName()+" found that default properties are already defined. Skipping adding them again");
				}
				
				log.fine(sServerID+" created coordinator schema");
			}
			else if  ( iFailures<4 ) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				for ( int i=0 ; i<iFailures ; i++ )
					bae[i].printStackTrace(new PrintStream(baos));
				log.severe("Rollback operation failed! "+baos.toString());
				for ( CoordinatorBackendAdapterException e:bae )
					throw e;
			}
			
		} catch (SQLException e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			log.severe("Commit operation failed! "+baos.toString());
		}
		catch ( CoordinatorBackendAdapterException bae ) {
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
