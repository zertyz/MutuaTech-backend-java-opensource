package mutua.events;

import static org.junit.Assert.*;
import static mutua.tests.MutuaEventsAdditionalEventLinksTestsConfiguration.LOG;

import java.sql.SQLException;
import java.util.Hashtable;

import mutua.events.TestEventServer.ETestEventServices;
import mutua.events.annotations.EventConsumer;
import mutua.imi.IndirectMethodInvocationInfo;
import mutua.imi.IndirectMethodNotFoundException;

import org.junit.Test;

import adapters.IJDBCAdapterParameterDefinition;
import adapters.exceptions.PreparedProcedureException;

/** <pre>
 * PostgreSQLQueueEventLinkTests.java
 * ==================================
 * (created by luiz, Jan 30, 2015)
 *
 * Test the behavior and caveats of the 'PostgreSQLQueueEventLink' 'IEventLink' implementation for event handling
 *
 * @see RelatedClass(es)
 * @version $Id$
 * @author luiz
 */

public class PostgreSQLQueueEventLinkTests {
	
	@Test
	public void testDyingConsumersAndFallbackQueue() throws SQLException, IndirectMethodNotFoundException, InterruptedException {
		LOG.reportRequestStart("testDyingConsumersAndFallbackQueue");
		String expectedMOPhone = "21991234899";
		String expectedMOText  = "Let me see if it goes and get back like a boomerang...";
		final String[] observedMOPhone = {""};
		final String[] observedMOText  = {""};
		final boolean[] firstRun       = {true};
		
		// set the number of workers to 1 to see if it will die
		int oldValue = PostgreSQLQueueEventLink.QUEUE_NUMBER_OF_WORKER_THREADS;
		PostgreSQLQueueEventLink.QUEUE_NUMBER_OF_WORKER_THREADS = 1;
		
		PostgreSQLQueueEventLink<ETestEventServices> link = new PostgreSQLQueueEventLink<ETestEventServices>(ETestEventServices.class, "DyingQueue", new GenericQueueDataBureau());
		link.resetQueues();
		TestEventServer eventServer = new TestEventServer(link);
				
		eventServer.addClient(new EventClient<ETestEventServices>() {
			@EventConsumer({"MO_ARRIVED"})
			public void receiveMOFromQueue(MO mo) {
				if (firstRun[0]) {
					firstRun[0] = false;
					throw new RuntimeException("The first invocation must die, but the second should keep on");
				}
				observedMOPhone[0] = mo.phone;
				observedMOText[0]  = mo.text;
			}
		});
		
		assertEquals("No fallback elements should be present before the first failure", 0, link.popFallbackEventIds().length);

		// add the first event -- this one must raise an exception to see if the worker thread will die
		eventServer.addToMOQueue(new MO(expectedMOPhone, expectedMOText));
		
		// wait for the first event to be consumed
		Thread.sleep(100);
		
		assertFalse("First event was not consumed", firstRun[0]);
		assertEquals("A fallback element should be present after a processing failure", 1, link.popFallbackEventIds().length);
		
		
		// add the second event -- this one must be consumed if the worker didn't die because of the exception
		eventServer.addToMOQueue(new MO(expectedMOPhone, expectedMOText));
		
		Thread.sleep(100);		
		LOG.reportRequestFinish();
		link.stop();

		assertEquals("No fallback elements should be present, since no other failure hapenned", 0, link.popFallbackEventIds().length);

		assertEquals("Wrong 'phone' dequeued", expectedMOPhone, observedMOPhone[0]);
		assertEquals("Wrong 'text'  dequeued", expectedMOText,  observedMOText[0]);
		
		// reset the number of workers
		PostgreSQLQueueEventLink.QUEUE_NUMBER_OF_WORKER_THREADS = oldValue;
	}

	@Test	
	public void testAddToQueueAndConsumeFromIt() throws SQLException, IndirectMethodNotFoundException, InterruptedException {
		LOG.reportRequestStart("testAddToQueueAndConsumeFromIt");
		String expectedMOPhone = "21991234899";
		String expectedMOText  = "Let me see if it goes and get back like a boomerang...";
		final String[] observedMOPhone = {""};
		final String[] observedMOText  = {""};
		
		PostgreSQLQueueEventLink<ETestEventServices> link = new PostgreSQLQueueEventLink<ETestEventServices>(ETestEventServices.class, "GenericQueue", new GenericQueueDataBureau());
		link.resetQueues();
		TestEventServer eventServer = new TestEventServer(link);
		
		eventServer.addClient(new EventClient<ETestEventServices>() {
			@EventConsumer({"MO_ARRIVED"})
			public void receiveMOFromQueue(MO mo) {
				assertTrue("Attempting to reconsume event", "".equals(observedMOPhone[0]));
				assertTrue("Attempting to reconsume event", "".equals(observedMOText[0]));
				observedMOPhone[0] = mo.phone;
				observedMOText[0]  = mo.text;
			}
		});
		
		eventServer.addToMOQueue(new MO(expectedMOPhone, expectedMOText));

		Thread.sleep(100);		
		LOG.reportRequestFinish();		
		link.stop();
		
		assertEquals("Wrong 'phone' dequeued", expectedMOPhone, observedMOPhone[0]);
		assertEquals("Wrong 'text'  dequeued", expectedMOText,  observedMOText[0]);
	}
	
	@Test
	public void testAddSeveralItemsAndConsumeAllAtOnce() throws SQLException, IndirectMethodNotFoundException, InterruptedException {
		LOG.reportRequestStart("testAddSeveralItemsAndConsumeAllAtOnce");		
		
		PostgreSQLQueueEventLink<ETestEventServices> link = new PostgreSQLQueueEventLink<ETestEventServices>(ETestEventServices.class, "SpecializedMOQueue", new SpecializedMOQueueDataBureau());
		link.resetQueues();
		final TestEventServer eventServer = new TestEventServer(link);
		

		final long phoneStart = 21991234800L;
		final Hashtable<String, String> receivedMOs = new Hashtable<String, String>();
		final int  expectedNumberOfEntries = 100;
		final int[] observedNumberOfEntries = {0};
		final long[] firstAndLastConsumedEntriesTimeMillis = {-1, -1};	// := {first, last}
		
		// consumers
		eventServer.addClient(new EventClient<ETestEventServices>() {
			@EventConsumer({"MO_ARRIVED"})
			public void receiveMOFromQueue(MO mo) {
				synchronized (observedNumberOfEntries) {
					observedNumberOfEntries[0]++;
				}
				if (receivedMOs.containsKey(mo.phone)) {
					fail("Double consumption attempt for phone '"+mo.phone+"'");
				}
				receivedMOs.put(mo.phone, mo.text);
				if (firstAndLastConsumedEntriesTimeMillis[0] == -1) {
					firstAndLastConsumedEntriesTimeMillis[0] = System.currentTimeMillis();
				} else {
					firstAndLastConsumedEntriesTimeMillis[1] = System.currentTimeMillis();
				}
			}
		});

		// producer
		for (long phone=phoneStart; phone<phoneStart+expectedNumberOfEntries; phone++) {
			eventServer.addToMOQueue(new MO(Long.toString(phone), "This is text number "+(phone-phoneStart)));
		}
		
		// wait for the pending events to be dispatched
		int attempts = 10;
		while (link.hasPendingEvents()) {
			Thread.sleep(1000/attempts);
			attempts--;
			if (attempts == 0) {
				fail("pending events never got processed");
//				System.err.println("pending events never got processed");
//				break;
			}
		}

		LOG.reportRequestFinish();
		link.stop();
		
		assertEquals("Wrong number of elements consumed", expectedNumberOfEntries, observedNumberOfEntries[0]);
		for (int i=0; i<expectedNumberOfEntries; i++) {
			String phone = Long.toString(phoneStart + i);
			String expectedText  = "This is text number "+i;
			String observedText  = receivedMOs.get(phone);
			assertEquals("Wrong specialized queue entry received for phone '"+phone+"'",  expectedText,  observedText);
		}
		
		System.out.println("\n\n" +
		                   "### Elapsed time between first and last entry consumption: " + (firstAndLastConsumedEntriesTimeMillis[1] - firstAndLastConsumedEntriesTimeMillis[0]) +
		                   "\n\n");
	}

}

class GenericQueueDataBureau extends IDatabaseQueueDataBureau<ETestEventServices> {
	
	enum GenericParameters implements IJDBCAdapterParameterDefinition {

		METHOD_ID,
		SERIALIZED_PARAMETERS,;

		@Override
		public String getParameterName() {
			return name();
		}
	}
	
	@Override
	public Object[] serializeQueueEntry(IndirectMethodInvocationInfo<ETestEventServices> entry) throws PreparedProcedureException {
		StringBuffer serializedParameters = new StringBuffer();
		serializedParameters.append('{');
		for (Object parameter : entry.getParameters()) {
			serializedParameters.append(parameter.toString());
			serializedParameters.append(',');
		}
		serializedParameters.append('}');
		String serializedMethodId = entry.getMethodId().toString();
		return new Object[] {
			GenericParameters.METHOD_ID, serializedMethodId,
			GenericParameters.SERIALIZED_PARAMETERS, serializedParameters.toString()};
	}
	@Override
	public IndirectMethodInvocationInfo<ETestEventServices> deserializeQueueEntry(int eventId, Object[] databaseRow) {
		String serializedMethodId   = (String)databaseRow[0];
		String serializedParameters = (String)databaseRow[1];
		ETestEventServices methodId = ETestEventServices.valueOf(serializedMethodId);
		String phone = serializedParameters.replaceAll(".*phone='([^']*)'.*", "$1");
		String text  = serializedParameters.replaceAll(".*text='([^']*)'.*", "$1");
		IndirectMethodInvocationInfo<ETestEventServices> entry = new IndirectMethodInvocationInfo<ETestEventServices>(methodId, new MO(phone, text));
		return entry;
	}

	@Override
	public IJDBCAdapterParameterDefinition[] getParametersListForInsertNewQueueElementQuery()  {
		return GenericParameters.values();
	}
	
	@Override
	public String getQueueElementFieldList() {
		return "methodId, genericParameters";
	}

	@Override
	public String getFieldsCreationLine() {
		return 	"methodId          TEXT NOT NULL, " +
                "genericParameters TEXT NOT NULL, ";
	}
}