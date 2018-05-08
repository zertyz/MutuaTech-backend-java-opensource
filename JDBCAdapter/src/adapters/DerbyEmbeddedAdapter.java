package adapters;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

import mutua.icc.configuration.annotations.ConfigurableElement;
import mutua.icc.instrumentation.Instrumentation;

/** <pre>
 * DerbyEmbeddedAdapter.java
 * =========================
 * (created by luiz, Jan 14, 2016)
 *
 * Specializes {@link JDBCAdapter} to deal with the peculiarities of the PostgreSQL database and it's JDBC driver
 *
 * @version $Id$
 * @author luiz
 */

public abstract class DerbyEmbeddedAdapter extends JDBCAdapter {

	// for the shutdown hook
	private static DerbyEmbeddedAdapter firstInstance = null;
	
	// Mutua Configurable Class pattern
	///////////////////////////////////
	
	@ConfigurableElement("Additional URL parameters for Embedded Derby JDBC driver connection properties")
	public static String  CONNECTION_PROPERTIES = "";
	@ConfigurableElement("The total number of concurrent connections allowed to embedded Derby on this VM. Suggestion: fine tune to get the optimum number for this particular app/database. As an initial value, set this to half the number of all consumer threads")
	public static int CONNECTION_POOL_SIZE = 4;
	
	protected static Connection[] connectionPool = null;
	
	/** method to be called when attempting to configure the default behavior for new instances of 'DerbyEmbeddedAdapter'.
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

	
	public DerbyEmbeddedAdapter(boolean allowDataStructuresAssertion, boolean shouldDebugQueries,
	                            String hostname, int port, String database, String user, String password) throws SQLException {
		super(new org.apache.derby.jdbc.EmbeddedDriver().getClass(), allowDataStructuresAssertion, shouldDebugQueries, hostname, port, database, user, password, connectionPool);
		// register the shutdown hook
		if (firstInstance == null) {
			firstInstance = this;
			addDerbyShutdownHook();
		}
	}

	@Override
	protected Connection createAdministrativeConnection() throws SQLException {
		return createDatabaseConnection();
	}

	@Override
	protected Connection createDatabaseConnection() throws SQLException {
		String url = "jdbc:derby:" + database + ";" + (allowDataStructuresAssertion ? "create=true":"") + ";" +
		             CONNECTION_PROPERTIES;
	
		return DriverManager.getConnection(url);
	}

	@Override
	protected String getShowTablesCommand() {
		return "select TABLENAME, SCHEMANAME from SYS.SYSTABLES,SYS.SYSSCHEMAS where TABLETYPE='T' and SYS.SYSTABLES.SCHEMAID=SYS.SYSSCHEMAS.SCHEMAID";
	}

	@Override
	protected String getShowDatabasesCommand() {
		return "SELECT database FROM (values ('"+database+"')) as x(database)";
	}

	@Override
	protected String[] getDropDatabaseCommand() {
		// to clean a database, these commands should be executed https://db.apache.org/derby/docs/10.0/manuals/reference/sqlj28.html
		// and their data may be gathered by the following sys tables:
		// SYS.SYSVIEWS
		// SYS.SYSTABLES;
		// SYS.SYSSCHEMAS;
		// SYS.SYSTRIGGERS;
		// select username from sys.sysusers
		// select roleid from sys.sysroles where cast(isdef as char(1)) = 'Y'" --> DROP ROLE ?;
		// ... and so on. continue from http://svn.apache.org/viewvc/db/derby/code/trunk/java/testing/org/apache/derbyTesting/junit/CleanDatabaseTestSetup.java?view=markup
		try {
			ArrayList<String> dropCommands = new ArrayList<String>();
			Connection conn = createAdministrativeConnection();
			
			// drop tables
			for (Object[] tableAndSchema : getArrayFromQueryExecution(conn, "select TABLENAME, SCHEMANAME from SYS.SYSTABLES,SYS.SYSSCHEMAS where TABLETYPE='T' and SYS.SYSTABLES.SCHEMAID=SYS.SYSSCHEMAS.SCHEMAID")) {
				String tableName  = (String)tableAndSchema[0];
				String schemaName = (String)tableAndSchema[1];
				dropCommands.add("DROP TABLE " + schemaName + "." + tableName);
			}
			
			// drop schemas
			// "select SCHEMANAME from SYS.SYSSCHEMAS where SCHEMANAME=AUTHORIZATIONID"
			
			conn.close();
			return dropCommands.toArray(new String[dropCommands.size()]);
		} catch (SQLException e) {
			Instrumentation.reportThrowable(e, "Error while assembling the drop database command set");
			return null;
		}
		
		
	}
	
	/** Method to safely disconnect from the local database prior to VM shutdown. Possibly this may be included in the JDBCAdapter? */
	private void addDerbyShutdownHook() throws SQLException {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					System.out.println("JVM shutdown request detected. Shuttingdown Derby...");
					DriverManager.getConnection("jdbc:derby:;shutdown=true");
				} catch (SQLException e) {
					System.out.println("Successfully shutdown Derby");
				}
			}
		});
	}
}
