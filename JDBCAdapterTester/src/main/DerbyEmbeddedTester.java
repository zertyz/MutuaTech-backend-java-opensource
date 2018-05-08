package main;

import java.sql.SQLException;

import mutua.icc.instrumentation.Instrumentation;
import adapters.DerbyEmbeddedAdapter;
import adapters.JDBCAdapter;
import static main.DerbyEmbeddedAdapterConfiguration.DerbyStatements.*;
import static main.DerbyEmbeddedAdapterConfiguration.DerbyParameters.*;

/** <pre>
 * EmbeddedDerbyTester.java
 * ========================
 * (created by luiz, Jan 18, 2016)
 *
 * Some spikes about Embedded Derby integration with {@link JDBCAdapter} as well as
 * {@link DerbyEmbeddedAdapter} database administration commands
 *
 * @version $Id$
 * @author luiz
*/

public class DerbyEmbeddedTester {

	public static void embeddedDerbyTesterMain(String[] args) throws SQLException {
		Instrumentation.reportDebug("Attempting to get a Derby Embedded connection...");
		DerbyEmbeddedAdapter db = DerbyEmbeddedAdapterConfiguration.getDBAdapter();
		db.resetDatabase();

		// INSERT
		System.out.println("InsertTestRecord prepared SQL: " + InsertTestRecord.getPreparedProcedureSQL());
		int result = db.invokeUpdateProcedure(InsertTestRecord,
		                                      ID,    11,
		                                      PHONE, "21991234899");
		System.out.println("Result: " + result);
		
		// QUERY
		System.out.println("GetPhoneFromId prepared SQL: " + GetPhoneFromId.getPreparedProcedureSQL());
		String phone = (String) db.invokeScalarProcedure(GetPhoneFromId,
		                                                 ID, 11);
		System.out.println("Result: " + phone);
		
		// DELETE
		System.out.println("DeleteTestRecord prepared SQL: " + DeleteTestRecord.getPreparedProcedureSQL());
		result = db.invokeUpdateProcedure(DeleteTestRecord,
		                                  ID, 11);
		System.out.println("Result: " + result);
		
	}	

}
