package mutua.events;

import mutua.events.TestEventServer.ETestEventServices;
import mutua.events.annotations.EventConsumer;
import mutua.events.annotations.EventListener;
import mutua.imi.IndirectMethodNotFoundException;

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
		IEventLink<ETestEventServices> link = new DirectEventLink<ETestEventServices>(ETestEventServices.class);
		TestEventServer eventServer = new TestEventServer(link);
		eventServer.addClient(new EventClient<ETestEventServices>() {
			
			@EventListener({"LISTENABLE_EVENT_EXAMPLE",
			                "LISTENABLE_AND_CONSUMABLE_EVENT_EXAMPLE",
			                "LISTENABLE_AND_NEED_TO_BE_CONSUMED_EVENT_EXAMPLE"})
			public void logListenedEvents(String param) {
				System.out.println("listened event: " + param);
			}
			
			@EventConsumer({"CONSUMABLE_EVENT_EXAMPLE",
			                "LISTENABLE_AND_CONSUMABLE_EVENT_EXAMPLE",
					        "NEED_TO_BE_CONSUMED_EVENT_EXAMPLE",
					        "LISTENABLE_AND_NEED_TO_BE_CONSUMED_EVENT_EXAMPLE"})
			public void logConsumedEvent(String param) {
				System.out.println("consumed event: " + param);
			}
		});
		
		eventServer.reportListenableExampleEvent("listen to this shit");
		eventServer.reportConsumableExampleEvent("consume this shit");
		eventServer.reportListenableAndConsumableExampleEvent("listen to and consume this shit");
	}
	
	@Test
	public void testMissingListenedEvents() throws IndirectMethodNotFoundException {
		IEventLink<ETestEventServices> link = new DirectEventLink<ETestEventServices>(ETestEventServices.class);
		TestEventServer eventServer = new TestEventServer(link);
		eventServer.addClient(new EventClient<ETestEventServices>() {
			
			@EventListener({"LISTENABLE_AND_CONSUMABLE_EVENT_EXAMPLE"})
			public void listenToSomeListenableEvents(String param) {
				System.out.println("listened event: " + param);
			}
		});
		
		eventServer.reportListenableExampleEvent("no one to listen this, and no exception should be raised");
		eventServer.reportListenableAndConsumableExampleEvent("this must be listened, but not consumed because there is no consumer for it -- and this is ok");
	}
	
	@Test
	public void testTwoEventListeners() throws IndirectMethodNotFoundException {
		IEventLink<ETestEventServices> link = new DirectEventLink<ETestEventServices>(ETestEventServices.class);
		TestEventServer eventServer = new TestEventServer(link);
		eventServer.addClient(new EventClient<ETestEventServices>() {
			@EventListener({"LISTENABLE_EVENT_EXAMPLE"})
			public void listenToSomeListenableEvents(String param) {
				System.out.println("listened event by client 1: " + param);
			}
		});
		eventServer.addClient(new EventClient<ETestEventServices>() {
			@EventListener({"LISTENABLE_EVENT_EXAMPLE"})
			public void listenToSomeListenableEvents(String param) {
				System.out.println("listened event by client 2: " + param);
			}
		});
		
		eventServer.reportListenableExampleEvent("this should be heard by clients 1 and 2");
	}
	
	@Test
	public void testTwoEventServersAndTwoEventListeners() throws IndirectMethodNotFoundException {
		
		class myEventClient implements EventClient<ETestEventServices> {
			private String clientId;
			public myEventClient(String clientId) {
				this.clientId = clientId;
			}
			@EventListener({"LISTENABLE_EVENT_EXAMPLE"})
			public void listenToSomeListenableEvents(String param) {
				System.out.println("listened event by 'clientId' "+clientId+": " + param);
			}
		};
		
		IEventLink<ETestEventServices> link1 = new DirectEventLink<ETestEventServices>(ETestEventServices.class);
		IEventLink<ETestEventServices> link2 = new DirectEventLink<ETestEventServices>(ETestEventServices.class);
		TestEventServer eventServer1 = new TestEventServer(link1);
		myEventClient   eventClient1 = new myEventClient("client 1 for server 1");
		eventServer1.addClient(eventClient1);
		TestEventServer eventServer2 = new TestEventServer(link2);
		myEventClient   eventClient2 = new myEventClient("client 2 for server 2");
		eventServer2.addClient(eventClient2);
		
		eventServer1.reportListenableExampleEvent("this should be heard only by client 1, for server 1");
		eventServer2.reportListenableExampleEvent("this should be heard only by client 2, for server 2");
	}

	@Test(expected=RuntimeException.class)
	public void testReportedExceptions() throws IndirectMethodNotFoundException {
		IEventLink<ETestEventServices> link = new DirectEventLink<ETestEventServices>(ETestEventServices.class);
		TestEventServer eventServer = new TestEventServer(link);
		eventServer.addClient(new EventClient<ETestEventServices>() {
			@EventListener({"LISTENABLE_EVENT_EXAMPLE"})
			public void listenToSomeListenableEvents(String param) {
				throw new RuntimeException(param + ": A shit happened. How will it be reported?");
			}
		});
		
		eventServer.reportListenableExampleEvent("raise a shit");
	}
}


class TestEventServer extends EventServer<ETestEventServices> {
	
	public enum ETestEventServices {
		LISTENABLE_EVENT_EXAMPLE,
		CONSUMABLE_EVENT_EXAMPLE,
		LISTENABLE_AND_CONSUMABLE_EVENT_EXAMPLE,
		NEED_TO_BE_CONSUMED_EVENT_EXAMPLE,
		LISTENABLE_AND_NEED_TO_BE_CONSUMED_EVENT_EXAMPLE,
	}
	
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
	
	public boolean reportNeedToBeConsumedExampleEvent(String param) throws IndirectMethodNotFoundException {
		return dispatchNeedToBeConsumedEvent(ETestEventServices.NEED_TO_BE_CONSUMED_EVENT_EXAMPLE, param);
	}
	
	public boolean reportListenableAndNeedToBeConsumedExampleEvent(String param) throws IndirectMethodNotFoundException {
		return dispatchListenableAndNeedToBeConsumedEvent(ETestEventServices.LISTENABLE_AND_NEED_TO_BE_CONSUMED_EVENT_EXAMPLE, param);
	}
	
}

