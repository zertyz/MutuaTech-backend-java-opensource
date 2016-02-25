package mutua.events;

import mutua.imi.IndirectMethodNotFoundException;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.junit.Assert.*;

import org.junit.Test;


/** <pre>
 * TestEvents.java
 * ===============
 * (created by luiz, Jan 23, 2015)
 *
 * Tests the Mutua Events framework
 *
 * @see EventServer
 * @version $Id$
 * @author luiz
 */

public class TestEvents {
	
	@Test
	public void testAllEventsListenedAndConsumed() throws IndirectMethodNotFoundException {
		
		// *Results := {boolean for the first specific event, boolean for the listenable and consumable event},
		//             where the first specific event is either a listenable event or a consumable event
		final boolean[] listenerResults   = {false, false};
		final boolean[] relistenedResults = {false, false};
		final boolean[] consumerResults   = {false, false};
		final boolean[] reconsumedResults = {false, false};
		
		final String    listenableEvent              = "listen to this shit";
		final String    consumableEvent              = "consume this shit";
		final String    listenableAndConsumableEvent = "listen to and consume this shit";

		IEventLink<ETestEventServices> link = new DirectEventLink<ETestEventServices>(ETestEventServices.class, new Class[] {TestEventService.class});
		TestEventServer eventServer = new TestEventServer(link);
		
		// the event clients that will process events
		//////////////////////////////////////////////
		
		class SpecialEventListenerClient implements EventClient<ETestEventServices> {
			
			@TestEventService({ETestEventServices.LISTENABLE_EVENT_EXAMPLE,
			                   ETestEventServices.LISTENABLE_AND_CONSUMABLE_EVENT_EXAMPLE})
			public void logListenedEvents(String e) {
				if (e.equals(listenableEvent)) {
					if (listenerResults[0] == false) {
						listenerResults[0] = true;
					} else if (relistenedResults[0] == false) {
						relistenedResults[0] = true;
					} else {
						fail("listenable event received more than twice");
					}
				} else if (e.equals(listenableAndConsumableEvent)) {
					if (listenerResults[1] == false) {
						listenerResults[1] = true;
					} else if (relistenedResults[1] == false) {
						relistenedResults[1] = true;
					} else {
						fail("listenable and consumable event received more than twice on the listener");
					}
				} else {
					fail("Unknown event '"+e+"'");
				}
			}
		}
		
		class SpecialEventConsumerClient implements EventClient<ETestEventServices> {
			
			@TestEventService({ETestEventServices.CONSUMABLE_EVENT_EXAMPLE,
			                   ETestEventServices.LISTENABLE_AND_CONSUMABLE_EVENT_EXAMPLE})
			public void logConsumedEvent(String e) {
				if (e.equals(consumableEvent)) {
					if (consumerResults[0] == false) {
						consumerResults[0] = true;
					} else if (reconsumedResults[0] == false) {
						reconsumedResults[0] = true;
					} else {
						fail("consumable event received more than twice");
					}
				} else if (e.equals(listenableAndConsumableEvent)) {
					if (consumerResults[1] == false) {
						consumerResults[1] = true;
					} else if (reconsumedResults[1] == false) {
						reconsumedResults[1] = true;
					} else {
						fail("listenable and consumable event received more than twice on the consumer");
					}
				} else {
					fail("Unknown event '"+e+"'");
				}
			}
		};

		// adds 2 identical event listeners...
		for (int i=0; i<2; i++) {
			assertTrue("Could not add a listener", eventServer.addListener(new SpecialEventListenerClient()));
		}
		// and set an event consumer
		eventServer.setConsumer(new SpecialEventConsumerClient());

		// dispatch the events
		eventServer.reportListenableExampleEvent(listenableEvent);
		eventServer.reportConsumableExampleEvent(consumableEvent);
		eventServer.reportListenableAndConsumableExampleEvent(listenableAndConsumableEvent);
		
		// check
		assertTrue("The listenable event was not delivered to the listener (not even for 1 client)",                listenerResults[0]);
		assertTrue("The listenable and consumable event was not delivered to the listener (not even for 1 client)", listenerResults[1]);
		assertTrue("The listenable event was not delivered to both clients",                                        relistenedResults[0]);
		assertTrue("The listenable and consumable event was not delivered to both clients",                         relistenedResults[1]);
		assertTrue("The consumable event was not delivered to the consumer",                   consumerResults[0]);
		assertTrue("The listenable and consumable event was not delivered to the consumer",    consumerResults[1]);
		assertFalse("The consumable event was delivered twice to the consumer",                reconsumedResults[0]);
		assertFalse("The listenable and consumable event was delivered twice to the consumer", reconsumedResults[1]);
	}
	
	@Test
	public void testMissingListenedEvents() throws IndirectMethodNotFoundException {
		IEventLink<ETestEventServices> link = new DirectEventLink<ETestEventServices>(ETestEventServices.class, new Class[] {TestEventService.class});
		TestEventServer eventServer = new TestEventServer(link);
		final String[] observedEvent = {null};
		
		eventServer.addListener(new EventClient<ETestEventServices>() {
			@TestEventService(ETestEventServices.LISTENABLE_AND_CONSUMABLE_EVENT_EXAMPLE)
			public void listenToSomeListenableEvents(String e) {
				observedEvent[0] = e;
			}
		});
		
		eventServer.reportListenableExampleEvent("no one to listen this, and no exception should be raised");
		eventServer.reportListenableAndConsumableExampleEvent("this must be listened, but not consumed because there is no consumer for it -- and this is ok");
		assertEquals("this must be listened, but not consumed because there is no consumer for it -- and this is ok", observedEvent[0]);
	}
	
	@Test
	public void testTwoEventListeners() throws IndirectMethodNotFoundException {

		final boolean[] client1Ran = {false};
		final boolean[] client2Ran = {false};
		final String    broadcastedEvent = "this should be heard by clients 1 and 2";
		
		IEventLink<ETestEventServices> link = new DirectEventLink<ETestEventServices>(ETestEventServices.class, new Class[] {TestEventService.class});
		TestEventServer eventServer = new TestEventServer(link);
		
		eventServer.addListener(new EventClient<ETestEventServices>() {
			@TestEventService(ETestEventServices.LISTENABLE_EVENT_EXAMPLE)
			public void listenToSomeListenableEvents(String receivedEvent) {
				assertEquals(broadcastedEvent, receivedEvent);
				client1Ran[0] = true;
			}
		});
		
		eventServer.addListener(new EventClient<ETestEventServices>() {
			@TestEventService(ETestEventServices.LISTENABLE_EVENT_EXAMPLE)
			public void listenToSomeListenableEvents(String receivedEvent) {
				assertEquals(broadcastedEvent, receivedEvent);
				client2Ran[0] = true;
			}
		});
		
		eventServer.reportListenableExampleEvent(broadcastedEvent);
		assertTrue(client1Ran[0]);
		assertTrue(client2Ran[0]);
	}
	
	@Test
	public void testTwoEventServersAndTwoEventListeners() throws IndirectMethodNotFoundException {
		
		String expectedEvent1 = "this should be heard only by client 1, for server 1";
		String expectedEvent2 = "this should be heard only by client 2, for server 2";
		
		final String[] observedEvent1 = {null};
		final String[] observedEvent2 = {null};
		
		class myEventClient implements EventClient<ETestEventServices> {
			private String[] observedEvent;
			public myEventClient(String[] observedEvent) {
				this.observedEvent = observedEvent;
			}
			@TestEventService(ETestEventServices.LISTENABLE_EVENT_EXAMPLE)
			public void listenToSomeListenableEvents(String e) {
				observedEvent[0] = e;
			}
		};
		
		IEventLink<ETestEventServices> link1 = new DirectEventLink<ETestEventServices>(ETestEventServices.class, new Class[] {TestEventService.class});
		IEventLink<ETestEventServices> link2 = new DirectEventLink<ETestEventServices>(ETestEventServices.class, new Class[] {TestEventService.class});
		TestEventServer eventServer1 = new TestEventServer(link1);
		myEventClient   eventClient1 = new myEventClient(observedEvent1);
		eventServer1.addListener(eventClient1);
		TestEventServer eventServer2 = new TestEventServer(link2);
		myEventClient   eventClient2 = new myEventClient(observedEvent2);
		eventServer2.addListener(eventClient2);
		
		eventServer1.reportListenableExampleEvent(expectedEvent1);
		eventServer2.reportListenableExampleEvent(expectedEvent2);
		
		assertEquals(expectedEvent1, observedEvent1[0]);
		assertEquals(expectedEvent2, observedEvent2[0]);
	}

	@Test(expected=RuntimeException.class)
	public void testReportedExceptions() throws IndirectMethodNotFoundException {
		IEventLink<ETestEventServices> link = new DirectEventLink<ETestEventServices>(ETestEventServices.class, new Class[] {TestEventService.class});
		TestEventServer eventServer = new TestEventServer(link);
		eventServer.addListener(new EventClient<ETestEventServices>() {
			@TestEventService(ETestEventServices.LISTENABLE_EVENT_EXAMPLE)
			public void listenToSomeListenableEvents(String param) {
				throw new RuntimeException(param + ": A shit happened. How will it be reported?");
			}
		});
		
		eventServer.reportListenableExampleEvent("raise a shit");
	}
}

// Annotations for this Event Server -- 'EventConsumer' & 'EventListener' Events Enumeration & Annotation pattern implementation
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

enum ETestEventServices {
	LISTENABLE_EVENT_EXAMPLE,
	CONSUMABLE_EVENT_EXAMPLE,
	LISTENABLE_AND_CONSUMABLE_EVENT_EXAMPLE,
}

@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.METHOD) @interface TestEventService {
	ETestEventServices[] value();
}

class TestEventServer extends EventServer<ETestEventServices> {
	
	public TestEventServer(IEventLink<ETestEventServices> link) {
		super(link);
	}
	
	public void reportListenableExampleEvent(String param) {
		dispatchListenableEvent(ETestEventServices.LISTENABLE_EVENT_EXAMPLE, param);
	}
	
	public void reportConsumableExampleEvent(String param) {
		dispatchConsumableEvent(ETestEventServices.CONSUMABLE_EVENT_EXAMPLE, param);
	}
	
	public void reportListenableAndConsumableExampleEvent(String param) {
		dispatchListenableAndConsumableEvent(ETestEventServices.LISTENABLE_AND_CONSUMABLE_EVENT_EXAMPLE, param);
	}
	
}

