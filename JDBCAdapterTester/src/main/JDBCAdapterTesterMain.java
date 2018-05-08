package main;

import mutua.icc.instrumentation.Instrumentation;
import mutua.icc.instrumentation.InstrumentableEvent.ELogSeverity;
import mutua.icc.instrumentation.handlers.IInstrumentationHandler;
import mutua.icc.instrumentation.handlers.InstrumentationHandlerLogConsole;

/* Main.java  --  $Id: Main.java,v 1.1 2010/07/01 22:03:06 luiz Exp $
 * =========
 * (created by luiz, Dec 15, 2008)
 *
 * Some 'MysqlHelper' spikes
 */

public class JDBCAdapterTesterMain {

	public static void main(String[] args) {

		IInstrumentationHandler log = new InstrumentationHandlerLogConsole("JDBCAdapterTester", ELogSeverity.DEBUG);
		Instrumentation.configureDefaultValuesForNewInstances(log, log, log);

		try {
			
			// test MySQL connectivity
			//MySQLTester.mysqltesterMain(args);
			
			// test SQL Server connectivity
			//SQLServerTester.sqlserverMain(args);
			
			// test PostgreSQL connectivity
			PostgreSQLTester.postgreSQLTesterMain(args);
			
			// TODO JDBCAdapter does not allow two different database engines to coexist on the same VM due to the connection pool implementation
			
			// test embedded Derby connectivity
			//DerbyEmbeddedTester.embeddedDerbyTesterMain(args);
			
		} catch (Throwable e) {
			e.printStackTrace();
			Instrumentation.reportThrowable(e, "Error running 'JDBCAdapterTester'");
		}
	}

}
