package mutua.events;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import mutua.events.TestAdditionalEventServer.ETestAdditionalEventServices;

/** <pre>
 * TestAdditionalEventServer.java
 * ==============================
 * (created by luiz, Sep 9, 2015)
 *
 * Defines events (or job types) that can be present on the queue and
 * how to produce them.
 * 
 * Uses the 'EventConsumer' & 'EventListener' Events Enumeration & Annotation pattern
 *
 * @see RelatedClass(es)
 * @version $Id$
 * @author luiz
 */

public class TestAdditionalEventServer extends EventServer<ETestAdditionalEventServices> {
	
	@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.METHOD) public @interface TestAdditionalEvent {
		ETestAdditionalEventServices[] value();
	}
	
	public enum ETestAdditionalEventServices {
		MO_ARRIVED,
	}
	
	@Override
	public boolean addListener(EventClient<ETestAdditionalEventServices> listenerClient) {
		throw new RuntimeException("No listeneable events currently available for PostgreSQL Queues...");
	}

	public TestAdditionalEventServer(IEventLink<ETestAdditionalEventServices> link) {
		super(link);
	}
	
	public int addToMOQueue(MO mo) {
		int eventId = dispatchConsumableEvent(ETestAdditionalEventServices.MO_ARRIVED, mo);
		return eventId;
	}	
}


