package mutua.events;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.Hashtable;

import mutua.events.TestEventServer.ETestEventServices;
import mutua.events.annotations.EventConsumer;
import mutua.events.postgresql.QueuesPostgreSQLAdapter;
import mutua.icc.instrumentation.DefaultInstrumentationProperties;
import mutua.icc.instrumentation.Instrumentation;
import mutua.icc.instrumentation.pour.PourFactory.EInstrumentationDataPours;
import mutua.imi.IndirectMethodInvocationInfo;
import mutua.imi.IndirectMethodNotFoundException;

import org.junit.Test;

import adapters.dto.PreparedProcedureInvocationDto;
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
	
	private static Instrumentation<DefaultInstrumentationProperties, String> log = new Instrumentation<DefaultInstrumentationProperties, String>(
			"MutuaEventsAdditionalEventLinksTests", DefaultInstrumentationProperties.DIP_MSG, EInstrumentationDataPours.CONSOLE, null);
	
	// configure the database
	static {
		QueuesPostgreSQLAdapter.log = log; 
		QueuesPostgreSQLAdapter.HOSTNAME = "zertyz.heliohost.org";
		QueuesPostgreSQLAdapter.PORT     = 5432;
		QueuesPostgreSQLAdapter.DATABASE = "zertyz_spikes";
		QueuesPostgreSQLAdapter.USER     = "zertyz_user";
		QueuesPostgreSQLAdapter.PASSWORD = "spikes";
	}

	@Test
	public void testAddToQueueAndConsumeFromIt() throws SQLException, IndirectMethodNotFoundException, InterruptedException {
		log.reportRequestStart("testAddToQueueAndConsumeFromIt");
		String expectedMOPhone = "21991234899";
		String expectedMOText  = "Let me see if it goes and get back like a boomerang...";
		final String[] observedMOPhone = {""};
		final String[] observedMOText  = {""};
		
		PostgreSQLQueueEventLink<ETestEventServices> link = new PostgreSQLQueueEventLink<ETestEventServices>(ETestEventServices.class, "GenericQueue", new GenericQueueDataBureau());
		TestEventServer eventServer = new TestEventServer(link);

		eventServer.addClient(new EventClient<ETestEventServices>() {
			@EventConsumer({"MO_ARRIVED"})
			public void receiveMOFromQueue(MO mo) {
				observedMOPhone[0] = mo.phone;
				observedMOText[0]  = mo.text;
			}
		});
		
		eventServer.addToMOQueue(new MO(expectedMOPhone, expectedMOText));
		log.reportRequestFinish();
		
		link.stop();
		
		assertEquals("Wrong 'phone' dequeued", expectedMOPhone, observedMOPhone[0]);
		assertEquals("Wrong 'text'  dequeued", expectedMOText,  observedMOText[0]);
	}

	@Test
	public void testAddSeveralItemsAndConsumeAllAtOnce() throws SQLException, IndirectMethodNotFoundException, InterruptedException {
		log.reportRequestStart("testAddSeveralItemsAndConsumeAllAtOnce");
		
		PostgreSQLQueueEventLink<ETestEventServices> link = new PostgreSQLQueueEventLink<ETestEventServices>(ETestEventServices.class, "SpecializedMOQueue", new SpecializedMOQueueDataBureau());
		final TestEventServer eventServer = new TestEventServer(link);

		final long phoneStart = 21991234800L;
		final Hashtable<String, String> receivedMOs = new Hashtable<String, String>();
		final int  expectedNumberOfEntries = 100;
		final int[] observedNumberOfEntries = {0};
		final long[] firstAndLastConsumedEntriesTimeMillis = {-1, -1};	// := {first, last}
		
		eventServer.addClient(new EventClient<ETestEventServices>() {
			@EventConsumer({"MO_ARRIVED"})
			public void receiveMOFromQueue(MO mo) {
				if (receivedMOs.containsKey(mo.phone)) {
					fail("Double insertion attempt for phone '"+mo.phone+"'");
				}
				receivedMOs.put(mo.phone, mo.text);
				observedNumberOfEntries[0]++;
				if (firstAndLastConsumedEntriesTimeMillis[0] == -1) {
					firstAndLastConsumedEntriesTimeMillis[0] = System.currentTimeMillis();
				} else {
					firstAndLastConsumedEntriesTimeMillis[1] = System.currentTimeMillis();
				}
			}
		});
		
		class Producer extends Thread {
			long i;
			public Producer(long i) {
				this.i = i;
			}
			@Override
			public void run() {
				eventServer.addToMOQueue(new MO(Long.toString(i), "This is text number "+(i-phoneStart)));
				synchronized (log) {
					log.notify();
				}
			}
		};

		// create the (parallel) producers
		Producer[] producers = new Producer[expectedNumberOfEntries];
		for (long i=phoneStart; i<(phoneStart+expectedNumberOfEntries); i++) {
			producers[(int)(i-phoneStart)] = new Producer(i);
			producers[(int)(i-phoneStart)].start();
			synchronized (log) {
				log.wait();
			}
			//Thread.sleep(500);
		}
		// wait for the producers to finish
		while (true) {
			System.out.println("Waiting...");
			boolean done = true;
			for (int i=0; i<expectedNumberOfEntries; i++) {
				if (producers[i].isAlive()) {
					done = false;
				}
			}
			if (done) {
				break;
			} else {
				Thread.sleep(500);
			}
		}
		
		Thread.sleep(500);	// wait for the pending events to be dispatched
		log.reportRequestFinish();
		
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

class MO {
	public final String phone;
	public final String text;
	public MO(String phone, String text) {
		this.phone = phone;
		this.text  = text;
	}
	@Override
	public String toString() {
		return "phone='"+phone+"', text='"+text+"'";
	}
}


class TestEventServer extends EventServer<ETestEventServices> {
	
	public enum ETestEventServices {
		MO_ARRIVED,
	}
	
	public TestEventServer(IEventLink<ETestEventServices> link) {
		super(link);
	}
	
	public void addToMOQueue(MO mo) {
		dispatchConsumableEvent(ETestEventServices.MO_ARRIVED, mo);
	}	
}


class GenericQueueDataBureau extends IDatabaseQueueDataBureau<ETestEventServices> {
	@Override
	public void serializeQueueEntry(IndirectMethodInvocationInfo<ETestEventServices> entry, PreparedProcedureInvocationDto preparedProcedure) throws PreparedProcedureException {
		StringBuffer serializedParameters = new StringBuffer();
		serializedParameters.append('{');
		for (Object parameter : entry.getParameters()) {
			serializedParameters.append(parameter.toString());
			serializedParameters.append(',');
		}
		serializedParameters.append('}');
		//preparedProcedure.addParameter("METHOD_ID",  serializedMethodId);		(already inserted)
		preparedProcedure.addParameter("PARAMETERS", serializedParameters.toString());
	}
	@Override
	public IndirectMethodInvocationInfo<ETestEventServices> desserializeQueueEntry(int eventId, Object[] databaseRow) {
		String serializedMethodId   = (String)databaseRow[0];
		String serializedParameters = (String)databaseRow[1];
		ETestEventServices methodId = ETestEventServices.valueOf(serializedMethodId);
		String phone = serializedParameters.replaceAll(".*phone='([^']*)'.*", "$1");
		String text  = serializedParameters.replaceAll(".*text='([^']*)'.*", "$1");
		IndirectMethodInvocationInfo<ETestEventServices> entry = new IndirectMethodInvocationInfo<ETestEventServices>(methodId, new MO(phone, text));
		return entry;
	}
}


class SpecializedMOQueueDataBureau extends IDatabaseQueueDataBureau<ETestEventServices> {
	@Override
	public void serializeQueueEntry(IndirectMethodInvocationInfo<ETestEventServices> entry, PreparedProcedureInvocationDto preparedProcedure) throws PreparedProcedureException {
		MO mo = (MO)entry.getParameters()[0];
		preparedProcedure.addParameter("CARRIER", "testCarrier");
		preparedProcedure.addParameter("PHONE",   mo.phone);
		preparedProcedure.addParameter("TEXT",    mo.text);
	}
	@Override
	public IndirectMethodInvocationInfo<ETestEventServices> desserializeQueueEntry(int eventId, Object[] databaseRow) {
		String carrier = (String)databaseRow[1];
		String phone   = (String)databaseRow[2];
		String text    = (String)databaseRow[3];
		MO mo = new MO(phone, text);
		IndirectMethodInvocationInfo<ETestEventServices> entry = new IndirectMethodInvocationInfo<ETestEventServices>(ETestEventServices.MO_ARRIVED, mo);
		return entry;
	}
	@Override
	public String getValuesExpressionForInsertNewQueueElementQuery() {
		return "${METHOD_ID}, ${CARRIER}, ${PHONE}, ${TEXT}";
	}
	@Override
	public String getQueueElementFieldList() {
		return "methodId, carrier, phone, text";
	}
	@Override
	public String getFieldsCreationLine() {
		return 	"carrier   VARCHAR(15) NOT NULL, " +
                "phone     VARCHAR(15) NOT NULL, " +
				"text      VARCHAR(160) NOT NULL, ";
	}
}