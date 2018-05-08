package adapters;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** <pre>
 * SQLServerAdapter.java  --  $Id: SQLServerHelper.java,v 1.1 2010/07/01 22:02:14 luiz Exp $
 * =====================
 * (created by luiz, Jun 29, 2010)
 *
 * Specializes 'JDBCHelper' to deal with the peculiarities of the SQL Server database
 * and JTDS JDBC Driver
 */

public abstract class SQLServerAdapter extends JDBCAdapter {


	protected SQLServerAdapter(boolean allowDataStructuresAssertion, boolean shouldDebugQueries,
                               String hostname, int port, String database, String user, String password) throws SQLException {
		super(new net.sourceforge.jtds.jdbc.Driver().getClass(), allowDataStructuresAssertion, shouldDebugQueries, hostname, port, database, user, password, new Connection[1]);
	}
	
	@Override
	protected String getShowTablesCommand() {
		return null;
	}
	
	@Override
	protected String getShowDatabasesCommand() {
		return null;
	}
	
	@Override
	protected String[] getDropDatabaseCommand() {
		return null;
	}

	@Override
	protected Connection createAdministrativeConnection() throws SQLException {
		return null;	// disable administration. The driver seems not to allow us to operate on creating / dropping databases from the sql client
	}

	@Override
	protected Connection createDatabaseConnection() throws SQLException {
		String url = "jdbc:jtds:sqlserver://" + hostname + ":"+port+"/" +
		             database; 

		return DriverManager.getConnection(url,
		                                   user,
		                                   password);
	}

}
