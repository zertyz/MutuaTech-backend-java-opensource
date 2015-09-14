package mutua.events;

import static org.junit.Assert.fail;

import java.sql.SQLException;
import java.util.Hashtable;

import mutua.events.TestEventServer.ETestEventServices;
import mutua.events.annotations.EventConsumer;
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
	public void testAlgorithmAnalysis() throws Throwable {
		final int inserts =  totalNumberOfEntries / 2;
		final int selects = inserts;
		
		final PostgreSQLQueueEventLink<ETestEventServices> link = new PostgreSQLQueueEventLink<ETestEventServices>(ETestEventServices.class, "SpecializedMOQueue", new SpecializedMOQueueDataBureau());
		final TestEventServer eventServer = new TestEventServer(link);
		final boolean[]                 wasClientAdded          = {false};
		final int[]                     observedNumberOfEntries = {0};
		final Hashtable<String, String> receivedMOs             = new Hashtable<String, String>(totalNumberOfEntries+1, 1.0f);
		
		// prepare the tables & variables
		final MO[] mos   = new MO[inserts*2];
		for (int i=0; i<inserts*2; i++) {
			mos[i] = new MO(Integer.toString(912300000+i), "I whish I could be different for every object... " + Math.random());
		}
		
		final EventClient<ETestEventServices> eventClient = new EventClient<ETestEventServices>() {
			@EventConsumer({"MO_ARRIVED"})
			public void receiveMOFromQueue(MO mo) {
				synchronized (observedNumberOfEntries) {
					observedNumberOfEntries[0]++;
				}
				if (receivedMOs.containsKey(mo.phone)) {
					fail("Double consumption attempt for phone '"+mo.phone+"'");
//					System.err.println("Double consumption attempt for phone '"+mo.phone+"'");
//					System.exit(1);
				}
				receivedMOs.put(mo.phone, mo.text);
			}
		};
		
		
		new DatabaseAlgorithmAnalysis("PostgreSQLQueueEventLink", false, numberOfThreads, inserts, -1, selects) {
			public void resetTables() throws SQLException {
				link.resetQueues();
			}
			public void insertLoopCode(int i) throws SQLException {
				eventServer.addToMOQueue(mos[i]);
			}
			public void selectLoopCode(int i) throws Throwable {
				if ((i == 0) || (i == inserts)) {
					eventServer.addClient(eventClient);
				} else if ((i == (inserts-1))) {
//System.err.print("Waiting for first pass of selects to finish... ");
					while (observedNumberOfEntries[0] != (totalNumberOfEntries / 2)) {
//System.err.println("still waiting... " + observedNumberOfEntries[0] + " != " + (totalNumberOfEntries / 2));
						Thread.sleep(1);
					}
					eventServer.deleteClient(eventClient);
//System.err.println("First pass finished.");
				} else if ((i == (totalNumberOfEntries-1))) {
//System.err.print("Waiting for second pass of selects to finish... ");
					while (observedNumberOfEntries[0] != totalNumberOfEntries) {
						Thread.sleep(1);
					}
//System.err.println("Second pass finished.");
				}
			}
		};

	}
}
