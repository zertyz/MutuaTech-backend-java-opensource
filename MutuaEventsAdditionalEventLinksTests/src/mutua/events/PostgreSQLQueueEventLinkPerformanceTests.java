package mutua.events;

import static org.junit.Assert.*;
import static mutua.tests.MutuaEventsAdditionalEventLinksTestsConfiguration.*;

import java.sql.SQLException;
import java.util.Hashtable;

import mutua.events.TestAdditionalEventServer.ETestAdditionalEventServices;
import mutua.events.TestAdditionalEventServer.TestAdditionalEvent;
import mutua.tests.DatabaseAlgorithmAnalysis;

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
	private static int numberOfThreads      = 4;
	private static int totalNumberOfEntries = PERFORMANCE_TESTS_LOAD_FACTOR * 40000;	// please, be sure the division between this and 'numberOfThreads' is round

	// prepares the test variables
	private final MO[]                                           mos                     = new MO[totalNumberOfEntries];
	private final Hashtable<String, String>                      receivedMOs             = new Hashtable<String, String>(totalNumberOfEntries+1, 1.0f);
	private int                                                  observedNumberOfEntries = -1;
	private final EventClient<ETestAdditionalEventServices>      eventClient;
	final PostgreSQLQueueEventLink<ETestAdditionalEventServices> link;
	final TestAdditionalEventServer                              eventServer;
	
	
	public PostgreSQLQueueEventLinkPerformanceTests() throws SQLException {
		for (int i=0; i<totalNumberOfEntries; i++) {
			mos[i] = new MO(Integer.toString(912300000+i), "I whish I could be different for every object... " + Math.random());
		}
		eventClient = new EventClient<ETestAdditionalEventServices>() {
			@TestAdditionalEvent(ETestAdditionalEventServices.MO_ARRIVED)
			public synchronized void receiveMOFromQueue(MO mo) {
				observedNumberOfEntries++;
				if (receivedMOs.containsKey(mo.phone)) {
					fail("Double consumption attempt for phone '"+mo.phone+"'");
					System.err.println("Double consumption attempt for phone '"+mo.phone+"'");
				}
				receivedMOs.put(mo.phone, mo.text);
			}
		};
		link        = new PostgreSQLQueueEventLink<ETestAdditionalEventServices>(ETestAdditionalEventServices.class, ANNOTATION_CLASSES, "SpecializedMOQueue", new SpecializedMOQueueDataBureau());
		eventServer = new TestAdditionalEventServer(link);
	}

	/**********
	** TESTS **
	**********/
	
	@Test
	public void testIsolatedProducersAndConsumersAlgorithmAnalysis() throws Throwable {
		final int inserts =  totalNumberOfEntries / 2;
		final int selects = inserts;
		
		eventServer.unsetConsumer();
		new DatabaseAlgorithmAnalysis("PostgreSQLQueueEventLink Isolated Producers and Consumers", false, numberOfThreads, inserts, -1, selects) {
			public void resetTables() throws SQLException {
				link.resetQueues();
				observedNumberOfEntries = 0;
				receivedMOs.clear();
			}
			public void insertLoopCode(int i) throws SQLException {
				eventServer.addToMOQueue(mos[i]);
			}
			public void selectLoopCode(int i) throws Throwable {
				if ((i == 0) || (i == inserts)) {
					eventServer.setConsumer(eventClient);
				} else if ((i == (inserts-1))) {
					int c=0;
					while (observedNumberOfEntries != (totalNumberOfEntries / 2)) {
						Thread.sleep(1);
						c++;
						if (c>100000) {
							System.err.println("It is in the records that "+(i+1)+" elements were inserted and that only "+observedNumberOfEntries+" were consumed. Our list contains "+receivedMOs.size()+" elements. Questions: 1) Is the insertion number right? 2) Is the consumption number right? 3) Did we consume 'til the end or we dropped someone along the way?");
							System.err.print("Elements that are missing from our list: {");
							for (int k=0; k<=i; k++) {
								if (!receivedMOs.containsKey(mos[k].phone)) {
									System.err.print(mos[k].phone+"(#"+k+"),");
								}
							}
							System.err.println("}\nNot all "+(totalNumberOfEntries / 2)+" (half) elements were consumed. Please verify if all of them were added. Press CTRL-C to abort and try again.");
							Thread.sleep(10000);
						}
					}
					eventServer.unsetConsumer();
				} else if ((i == (totalNumberOfEntries-1))) {
					int c=0;
					while (observedNumberOfEntries != totalNumberOfEntries) {
						Thread.sleep(1);
						c++;
						if (c>100000) {
							System.err.println("It is in the records that "+(i+1)+" elements were inserted and that only "+observedNumberOfEntries+" were consumed. Our list contains "+receivedMOs.size()+" elements. Questions: 1) Is the insertion number right? 2) Is the consumption number right? 3) Did we consume 'til the end or we dropped someone along the way?");
							System.err.print("Elements that are missing from our list: {");
							for (int k=0; k<=i; k++) {
								if (!receivedMOs.containsKey(mos[k].phone)) {
									System.err.print(mos[k].phone+"(#"+k+"),");
								}
							}
							System.err.println("}\nNot all "+totalNumberOfEntries+" elements were consumed. Please verify if all of them were added. Press CTRL-C to abort and try again.");
							Thread.sleep(10000);
						}
					}
				}
			}
		};
		eventServer.unsetConsumer();

	}
	
	@Test
	public void testMixedProducersAndConsumersAlgorithmAnalysis() throws Throwable {
		final int inserts =  totalNumberOfEntries / 2;
		
		eventServer.setConsumer(eventClient);
		new DatabaseAlgorithmAnalysis("PostgreSQLQueueEventLink Mixed Producers and Consumers", false, numberOfThreads, inserts, -1, -1) {
			public void resetTables() throws SQLException {
				link.resetQueues();
				observedNumberOfEntries = 0;
				receivedMOs.clear();
			}
			public void insertLoopCode(int i) throws SQLException {
				eventServer.addToMOQueue(mos[i]);
			}
		};

		// checks
		int c=0;
		while (observedNumberOfEntries != totalNumberOfEntries) {
			Thread.sleep(1);
			c++;
			if (c>10000) {
				System.err.println("It is in the records that "+totalNumberOfEntries+" elements were inserted and that only "+observedNumberOfEntries+" were consumed. Our list contains "+receivedMOs.size()+" elements. Questions: 1) Is the insertion number right? 2) Is the consumption number right? 3) Did we consume 'til the end or we dropped someone along the way?");
				System.err.print("Elements that are missing from our list: {");
				for (int k=0; k<totalNumberOfEntries; k++) {
					if (!receivedMOs.containsKey(mos[k].phone)) {
						System.err.print(mos[k].phone+"(#"+k+"),");
					}
				}
				System.err.println("}\n");
				fail("Reentrancy problem detected");
			}
		}
		
		eventServer.unsetConsumer();

	}
}
