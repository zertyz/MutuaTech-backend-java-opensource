package mutua.events;

import static org.junit.Assert.assertEquals;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.Test;

import mutua.events.annotations.EEventNotifyeeType;
import mutua.imi.IndirectMethodInvocationInfo;
import mutua.imi.IndirectMethodNotFoundException;

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
		TestQueueEventClient consumerClient = new TestQueueEventClient(0, 1);
		IEventLink<EQueueEventLinkServices> link = new QueueEventLink<EQueueEventLinkServices>(EQueueEventLinkServices.class, new Class[] {TestQueueEventConsumer.class}, 10, 10);
		link.reportConsumableEvent(new IndirectMethodInvocationInfo<EQueueEventLinkServices>(EQueueEventLinkServices.ENQUEUEABLE_EVENT, 0, expectedEventInfo));
		link.setConsumer(consumerClient);
		Thread.sleep(1000);
		assertEquals("Late added client wasn't able to consume the event", expectedEventInfo, consumerClient.infos[0]);
		
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
	
	@TestQueueEventConsumer(EQueueEventLinkServices.ENQUEUEABLE_EVENT)
	public void consumeFromTheQueue(int i, String info) throws InterruptedException {
		if (delay > 0) {
			Thread.sleep(delay);
		}
		System.out.println("Consuming ["+i+"]='"+info+"'");
		infos[i] = info;
	}
	
}

// 'EventConsumer' & 'EventListener' Events Enumeration & Annotation pattern implementation
///////////////////////////////////////////////////////////////////////////////////////////

enum EQueueEventLinkServices {
	ENQUEUEABLE_EVENT,
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface TestQueueEventConsumer {
	EQueueEventLinkServices[] value();
}


class TestQueueEventServer extends EventServer<EQueueEventLinkServices> {

	private static IEventLink<EQueueEventLinkServices> link = new QueueEventLink<EQueueEventLinkServices>(EQueueEventLinkServices.class, 
		new Class[] {TestQueueEventConsumer.class}, 10, 10);

	protected TestQueueEventServer(TestQueueEventClient eventClient) {
		super(link);
		try {
			setConsumer(eventClient);
		} catch (IndirectMethodNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void addToTheQueue(int i, String info) {
		System.out.println("Adding ["+i+"]:='"+info+"'");
		dispatchConsumableEvent(EQueueEventLinkServices.ENQUEUEABLE_EVENT, i, info);
	}

}