package mutua.events;

import mutua.events.TestAdditionalEventServer.ETestEventServices;

/** <pre>
 * TestEventServer.java
 * ====================
 * (created by luiz, Sep 9, 2015)
 *
 * Defines events (or job types) that can be present on the queue and
 * how to produce them
 *
 * @see RelatedClass(es)
 * @version $Id$
 * @author luiz
 */

public class TestAdditionalEventServer extends EventServer<ETestEventServices> {
	
	public enum ETestEventServices {
		MO_ARRIVED,
	}

	public TestAdditionalEventServer(IEventLink<ETestEventServices> link) {
		super(link);
	}
	
	public int addToMOQueue(MO mo) {
		int eventId = dispatchConsumableEvent(ETestEventServices.MO_ARRIVED, mo);
		return eventId;
	}	
}


