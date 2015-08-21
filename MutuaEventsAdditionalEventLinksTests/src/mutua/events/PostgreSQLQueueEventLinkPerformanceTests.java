package mutua.events;

import java.sql.SQLException;

import mutua.events.TestEventServer.ETestEventServices;
import mutua.smsappmodule.DatabaseAlgorithmAnalysis;
import mutua.smsappmodule.config.SMSAppModuleConfigurationTests;

import org.junit.Test;

/** <pre>
 * PostgreSQLQueueEventLinkPerformanceTests.java
 * =============================================
 * (created by luiz, Aug 20, 2015)
 *
 * Measures and tests the f(n) of the O(f(n)) algorithm complexity implementations
 * of {@link PostgreSQLQueueEventLink}
 *
 * @version $Id$
 * @author luiz
 */

public class PostgreSQLQueueEventLinkPerformanceTests {
	
	// algorithm settings
	private static int numberOfThreads = 4;
	private static int totalNumberOfEntries = SMSAppModuleConfigurationTests.PERFORMANCE_TESTS_LOAD_FACTOR * 40000;	// please, be sure the division between this and 'numberOfThreads' is round

		
	/**********
	** TESTS **
	**********/
	
	@Test
	public void testAlgorithmAnalysis() throws SQLException, InterruptedException {
		int inserts =  totalNumberOfEntries / 2;
		int selects = inserts;
		
		final PostgreSQLQueueEventLink<ETestEventServices> link = new PostgreSQLQueueEventLink<ETestEventServices>(ETestEventServices.class, "SpecializedMOQueue", new SpecializedMOQueueDataBureau());
		final TestEventServer eventServer = new TestEventServer(link);
		
		// prepare the tables & variables
		final MO[] mos   = new MO[inserts*2];
		for (int i=0; i<inserts*2; i++) {
			mos[i] = new MO(Integer.toString(912300000+i), "I whish I could be different for every object... " + Math.random());
		}

		
		new DatabaseAlgorithmAnalysis("PostgreSQLQueueEventLink", numberOfThreads, inserts, -1, selects) {
			public void resetTables() throws SQLException {
				link.resetQueues();
			}
			public void insertLoopCode(int i) throws SQLException {
				eventServer.addToMOQueue(mos[i]);
			}
			public void selectLoopCode(int i) throws SQLException {
//				subscriptionDB.getSubscriptionRecord(users[i]);
			}
		};

	}
}
