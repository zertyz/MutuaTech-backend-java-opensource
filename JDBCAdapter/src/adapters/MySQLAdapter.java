package adapters;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import mutua.icc.configuration.annotations.ConfigurableElement;

/** <pre>
 * MySQLAdapter.java  --  $Id: MySQLHelper.java,v 1.1 2010/07/01 22:02:14 luiz Exp $
 * =================
 * (created by luiz, Jun 29, 2010)
 *
 * Specializes 'JDBCHelper' to deal with the peculiarities of the MySQL database
 * and MySQL JDBC Driver
 */

public abstract class MySQLAdapter extends JDBCAdapter {

	// Mutua Configurable Class pattern
	///////////////////////////////////
	
	@ConfigurableElement("Additional URL parameters for MySQL JDBC driver connection properties")
	public static String  CONNECTION_PROPERTIES = "characterEncoding=UTF8&characterSetResults=UTF8&autoReconnect=true&connectTimeout=10000&socketTimeout=10000";
	@ConfigurableElement("The number of concurrent connections allowed to each MySQL server. Suggestion: fine tune to get the optimum number for this particular app/database, paying attention to the fact that a pool smaller than the sum of all consumer threads may be suboptimal, and that a greater than it can be a waste. As an initial value, set this to nDbCPUs * nDbHDs and adjust the consumer threads accordingly")
	public static int CONNECTION_POOL_SIZE = 8;
	
	protected static Connection[] connectionPool = null;
	
	/** method to be called when attempting to configure the default behavior for new instances of 'MySQLAdapter'.
	 *  @param connectionProperties if null, the default value won't be touched
	 *  @param connectionPoolSize   if <= 0, the default value won't be touched */
	public static void configureDefaultValuesForNewInstances(String connectionProperties, int connectionPoolSize) {
		
		CONNECTION_PROPERTIES         = connectionProperties != null ? connectionProperties : CONNECTION_PROPERTIES;
		CONNECTION_POOL_SIZE          = connectionPoolSize   >  0    ? connectionPoolSize   : CONNECTION_POOL_SIZE;
		
		// prepare the connection pool
		if ((connectionPool == null) || (connectionPool.length != CONNECTION_POOL_SIZE)) {
			connectionPool = new Connection[CONNECTION_POOL_SIZE];
		}
	}
	
	static {
		configureDefaultValuesForNewInstances(null, -1);
	}

	
	protected MySQLAdapter(boolean allowDataStructuresAssertion, boolean shouldDebugQueries,
                           String hostname, int port, String database, String user, String password) throws SQLException {
		super(com.mysql.jdbc.Driver.class, allowDataStructuresAssertion, shouldDebugQueries, hostname, port, database, user, password, connectionPool);
	}
	
	@Override
	protected String getShowTablesCommand() {
		return "show tables;";
	}
	
	@Override
	protected String getShowDatabasesCommand() {
		return "show databases;";
	}

	@Override
	protected String[] getDropDatabaseCommand() {
		return new String[] {"DROP DATABASE " + database};
	}

	@Override
	protected Connection createAdministrativeConnection() throws SQLException {
		String url = "jdbc:mysql://" + hostname + ":"+port+"/?" +
		             CONNECTION_PROPERTIES; 

		return DriverManager.getConnection(url,
		                                   user,
		                                   password);
	}

	@Override
	protected Connection createDatabaseConnection() throws SQLException {
		String url = "jdbc:mysql://" + hostname + ":"+port+"/" +
		             database + "?" + CONNECTION_PROPERTIES; 

		return DriverManager.getConnection(url,
		                                   user,
		                                   password);
	}


}
