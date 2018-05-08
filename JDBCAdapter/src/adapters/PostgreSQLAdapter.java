package adapters;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** <pre>
 * PostgresSQLAdapter.java
 * =======================
 * (created by luiz, Jan 26, 2015)
 *
 * Specializes {@link JDBCAdapter} to deal with the peculiarities of the PostgreSQL database and it's JDBC driver
 *
 * @version $Id$
 * @author luiz
 */

public abstract class PostgreSQLAdapter extends JDBCAdapter {

	
	// Mutua Configurable Class pattern
	///////////////////////////////////
	
	/** Additional URL parameters for PostgreSQL JDBC driver connection properties */
	public static String  CONNECTION_PROPERTIES = "prepareThreshold=1&charSet=UTF8&tcpKeepAlive=true&connectTimeout=30&loginTimeout=30&socketTimeout=300";
	/** The number of concurrent connections allowed to each PostgreSQL server. Suggestion: fine tune to get the optimum number for this particular app/database, paying attention to the fact that a pool smaller than the sum of all consumer threads may be suboptimal, and that a greater than it can be a waste. As an initial value, set this to nDbCPUs * nDbHDs and adjust the consumer threads accordingly */
	public static int CONNECTION_POOL_SIZE = 8;
	
	// public here is only needed because of 'mutua.events.PostgreSQLQueueEventLinkTests.testDeleteEvents'
	public static Connection[] connectionPool = null;
	
	/** method to be called when attempting to configure the default behavior for new instances of 'PostgreSQLAdapter'.
	 *  @param connectionProperties if null, the default value won't be touched. See {@link #CONNECTION_PROPERTIES}
	 *  @param connectionPoolSize   if <= 0, the default value won't be touched. See {@link #CONNECTION_POOL_SIZE} */
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

	
	public PostgreSQLAdapter(boolean allowDataStructuresAssertion, boolean shouldDebugQueries,
	                         String hostname, int port, String database, String user, String password) throws SQLException {
		super(new org.postgresql.Driver().getClass(), allowDataStructuresAssertion, shouldDebugQueries, hostname, port, database, user, password, connectionPool);
	}
	
	@Override
	protected String getShowTablesCommand() {
		return "SELECT tablename FROM pg_catalog.pg_tables WHERE schemaname != 'pg_catalog' AND schemaname != 'information_schema' AND tableowner='"+user+"';";
	};

	@Override
	protected String getShowDatabasesCommand() {
		return "SELECT datname FROM pg_database;";
	}
	
	@Override
	/** For postgreSQL, a different strategy is used to "drop" the database -- drop all tables instead */
	protected String[] getDropDatabaseCommand() {
		String[][] tableDefinitions = getTableDefinitions();
		
		String[] statements = new String[tableDefinitions.length];
		int i = 0;
		for (String[] tableDefinition : tableDefinitions) {
			String databaseName = tableDefinition[0];
			statements[i++] = "DROP TABLE " + databaseName + " CASCADE";
		}
		return statements;
	}

	@Override
	protected Connection createAdministrativeConnection() throws SQLException {
		return createDatabaseConnection();	// it is impossible to connect to PostgreSQL without a database
	}

	@Override
	protected Connection createDatabaseConnection() throws SQLException {
		String url = "jdbc:postgresql://" + hostname + ":"+port+"/" +
		             database + "?" + CONNECTION_PROPERTIES; 

		return DriverManager.getConnection(url,
		                                   user,
		                                   password);
	}
	
	
	// helper methods
	/////////////////
	
	protected static String list(Object[] stringArray, String quote, String separator) {
		StringBuffer sb = new StringBuffer();
		for (Object element : stringArray) {
			sb.append(quote).append(element.toString()).append(quote).append(separator);
		}
		// remove the last 'separator'
		if (sb.length() > 0) {
			sb.deleteCharAt(sb.length()-1);
		}
		return sb.toString();
	}

}