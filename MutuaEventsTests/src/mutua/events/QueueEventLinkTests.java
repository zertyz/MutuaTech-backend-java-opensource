package mutua.events;

import static org.junit.Assert.*;

import java.util.ArrayList;

import mutua.events.TestQueueEventServer.EQueueEventLinkServices;
import mutua.events.annotations.EventConsumer;
import mutua.imi.IndirectMethodInvocationInfo;
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
	public void testLateConsumerAddition() throws IndirectMethodNotFoundException, InterruptedException {
		String expectedEventInfo = "added before there were consumers";
		TestQueueEventClient eventClient = new TestQueueEventClient(0, 1);
		IEventLink<EQueueEventLinkServices> link = new QueueEventLink<EQueueEventLinkServices>(EQueueEventLinkServices.class, 10, 10);
		link.reportConsumableEvent(new IndirectMethodInvocationInfo<EQueueEventLinkServices>(EQueueEventLinkServices.ENQUEUEABLE_EVENT, 0, expectedEventInfo));
		link.addClient(eventClient);
		Thread.sleep(1000);
		assertEquals("Late added client wasn't able to consume the event", expectedEventInfo, eventClient.infos[0]);
		
	}

	@Test
	public void testSimpleEnqueueingAndDequeueing() throws InterruptedException {
		int numberOfEntries = 100;
		TestQueueEventClient eventClient = new TestQueueEventClient(500, numberOfEntries);
		TestQueueEventServer eventServer = new TestQueueEventServer(eventClient);
		for (int i=0; i<numberOfEntries; i++) {
			eventServer.addToTheQueue(i, "request "+i);
		}
		Thread.sleep(3000);
		for (int i=0; i<numberOfEntries; i++) {
			assertEquals("Consumed event doesn't match", "request "+i, eventClient.infos[i]);
		}
	}
	
}


class TestQueueEventClient implements EventClient<EQueueEventLinkServices> {
	
	public String[] infos;
	
	private long delay;
	
	public TestQueueEventClient(long delay, int length) {
		this.delay = delay;
		this.infos = new String[length];
	}
	
	@EventConsumer({"ENQUEUEABLE_EVENT"})
	public void consumeFromTheQueue(int i, String info) throws InterruptedException {
		if (delay > 0) {
			Thread.sleep(delay);
		}
		System.out.println("Consuming ["+i+"]='"+info+"'");
		infos[i] = info;
	}
	
}

class TestQueueEventServer extends EventServer<EQueueEventLinkServices> {

	public enum EQueueEventLinkServices {
		ENQUEUEABLE_EVENT,
	}
	
	private static IEventLink<EQueueEventLinkServices> link = new QueueEventLink<EQueueEventLinkServices>(EQueueEventLinkServices.class, 10, 10);

	protected TestQueueEventServer(TestQueueEventClient eventClient) {
		super(link);
		try {
			addClient(eventClient);
		} catch (IndirectMethodNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void addToTheQueue(int i, String info) {
		System.out.println("Adding ["+i+"]:='"+info+"'");
		dispatchNeedToBeConsumedEvent(EQueueEventLinkServices.ENQUEUEABLE_EVENT, i, info);
	}

}