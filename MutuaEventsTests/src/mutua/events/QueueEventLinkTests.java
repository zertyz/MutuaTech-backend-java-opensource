package mutua.events;

import java.beans.ConstructorProperties;

import mutua.events.TestQueueEventServer.EQueueEventLinkServices;
import mutua.events.annotations.EventConsumer;
import mutua.imi.IndirectMethodNotFoundException;

import org.junit.Test;

/** <pre>
 * QueueEventLinkTests.java
 * ========================
 * (created by luiz, Jan 26, 2015)
 *
 * Tests the 'QueueEventLink' event dispatcher
 *
 * @see RelatedClass(es)
 * @version $Id$
 * @author luiz
 */

public class QueueEventLinkTests {
	
	@Test
	public void testSimpleEnqueueingAndDequeueing() throws InterruptedException {
		TestQueueEventServer eventServer = new TestQueueEventServer();
		for (int i=0; i<100; i++) {
			eventServer.addToTheQueue("request "+i);
		}
		Thread.sleep(6000);
	}
}


class TestQueueEventServer extends EventServer<EQueueEventLinkServices> {

	
	public enum EQueueEventLinkServices {
		ENQUEUEABLE_EVENT,
	}
	
	class TestQueueEventClient implements EventClient<EQueueEventLinkServices> {
		
		@EventConsumer({"ENQUEUEABLE_EVENT"})
		public void consumeFromTheQueue(String info) throws InterruptedException {
			Thread.sleep(5000);
			System.out.println("Consuming '"+info+"'");
		}
		
	}
	
	private static IEventLink<EQueueEventLinkServices> link = new QueueEventLink<EQueueEventLinkServices>(EQueueEventLinkServices.class, 10, 10);

	protected TestQueueEventServer() {
		super(link);
		try {
			addClient(new TestQueueEventClient());
		} catch (IndirectMethodNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void addToTheQueue(String info) {
		System.out.println("Adding '"+info+"'");
		dispatchNeedToBeConsumedEvent(EQueueEventLinkServices.ENQUEUEABLE_EVENT, info);
	}

}