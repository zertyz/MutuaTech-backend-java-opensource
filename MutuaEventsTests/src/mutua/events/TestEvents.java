package mutua.events;

import mutua.events.TestEventServer.ETestEventService;
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

class TestEventServer extends EventServer<ETestEventService> {
	
	public enum ETestEventService {
		LISTENABLE_EVENT_EXAMPLE,
		CONSUMABLE_EVENT_EXAMPLE,
		LISTENABLE_AND_CONSUMABLE_EVENT_EXAMPLE,
		NEED_TO_BE_CONSUMED_EVENT_EXAMPLE,
		LISTENABLE_AND_NEED_TO_BE_CONSUMED_EVENT_EXAMPLE,
	}
	
	public TestEventServer(IEventLink<ETestEventService> link) {
		super(link);
	}
	
	public void reportListenableExampleEvent(String param) {
		dispatchListenableEvent(ETestEventService.LISTENABLE_EVENT_EXAMPLE, param);
	}
	
	public void reportConsumableExampleEvent(String param) {
		dispatchConsumableEvent(ETestEventService.CONSUMABLE_EVENT_EXAMPLE, param);
	}
	
	public void reportListenableAndConsumableExampleEvent(String param) {
		dispatchListenableAndConsumableEvent(ETestEventService.LISTENABLE_AND_CONSUMABLE_EVENT_EXAMPLE, param);
	}
	
	public boolean reportNeedToBeConsumedExampleEvent(String param) {
		return dispatchNeedToBeConsumedEvent(ETestEventService.NEED_TO_BE_CONSUMED_EVENT_EXAMPLE, param);
	}
	
	public boolean reportListenableAndNeedToBeConsumedExampleEvent(String param) {
		return dispatchListenableAndNeedToBeConsumedEvent(ETestEventService.LISTENABLE_AND_NEED_TO_BE_CONSUMED_EVENT_EXAMPLE, param);
	}
	
}

public class TestEvents {
	
	@Test
	public void doTheShitTest() throws IndirectMethodNotFoundException {
		IEventLink<ETestEventService> link = new DirectEventLink<ETestEventService>(ETestEventService.class);
		TestEventServer eventServer = new TestEventServer(link);
		eventServer.addClient(new EventClient() {
			
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

}
