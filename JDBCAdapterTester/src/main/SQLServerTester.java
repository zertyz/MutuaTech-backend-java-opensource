package main;

import java.sql.SQLException;

import adapters.SQLServerAdapter;

/** <pre>
 * SQLServerTester.java  --  $Id: SQLServerTester.java,v 1.1 2010/07/01 22:03:06 luiz Exp $
 * ====================
 * (created by luiz, Jun 29, 2010)
 *
 * Some 'JDBCAdapter' / 'SQLServerAdapter' spikes
 */

public class SQLServerTester {
/*
	public static void sqlserverMain(String[] args) throws SQLException {
		System.out.println("SQLServerHelperTester is running...");
		SQLServerAdapter db = SQLServerAdapterConfiguration.getDBAdapter();
		
		// QUERY Veículos
		PreparedProcedureInvocationDto invocation;
		String[] queries = {"GetVeiculos", "GetCaracteristicas", "GetFichas", "GetLojas"};
		for (int i=0; i<queries.length; i++) {
			String query = queries[i];
			System.out.println("##########################################");
			System.out.println("## Now running query '"+query+"'");
			System.out.println("##########################################");
			invocation = new PreparedProcedureInvocationDto(query);
			Object[][] result = db.invokeArrayProcedure(invocation);	// dumb way to know the number of columns
			ResultSet rs = db.invokeVirtualTableProcedure(invocation);
			while (rs.next()) {
				System.out.print("{");
				for (int col=0; col<result[0].length; col++) {
					String value = rs.getString(col+1);
					System.out.print(value);
					System.out.print(",");
				}
				System.out.println("},");
			}
			rs.close();
		}
	}
*/
}
