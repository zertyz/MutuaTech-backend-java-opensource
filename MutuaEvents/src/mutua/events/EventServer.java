package mutua.events;

import mutua.imi.IndirectMethodInvocationInfo;
import mutua.imi.IndirectMethodNotFoundException;

/** <pre>
 * EventServer.java
 * ================
 * (created by luiz, Jan 23, 2015)
 *
 * Base class for all event servers. Helps with the dispatch of events.
 * Listenable events are events that will be delivered for the 'EventListener' annotated methods of all clients;
 * Consumable events are events that may be consumed by an 'EventConsumer' annotated method of one of the clients;
 * Need to be Consumed events are like the above, except that they should be consumed. False is returned if it can't be consumed.
 *
 * @see RelatedClass(es)
 * @version $Id$
 * @author luiz
 */

public class EventServer<SERVICE_EVENTS_ENUMERATION> {

	protected final IEventLink<SERVICE_EVENTS_ENUMERATION> link;
	
	protected EventServer(IEventLink<SERVICE_EVENTS_ENUMERATION> link) {
		this.link = link;
	}
	
	protected void dispatchListenableEvent(SERVICE_EVENTS_ENUMERATION serviceId, Object... parameters) {
		link.reportListenableEvent(new IndirectMethodInvocationInfo<SERVICE_EVENTS_ENUMERATION>(serviceId, parameters));
	}
	
	protected void dispatchConsumableEvent(SERVICE_EVENTS_ENUMERATION serviceId, Object... parameters) {
		link.reportConsumableEvent(new IndirectMethodInvocationInfo<SERVICE_EVENTS_ENUMERATION>(serviceId, parameters));
	}

	protected void dispatchListenableAndConsumableEvent(SERVICE_EVENTS_ENUMERATION serviceId, Object... parameters) {
		IndirectMethodInvocationInfo<SERVICE_EVENTS_ENUMERATION> event = new IndirectMethodInvocationInfo<SERVICE_EVENTS_ENUMERATION>(serviceId, parameters);
		link.reportListenableEvent(event);
		link.reportConsumableEvent(event);
	}
	
	protected boolean dispatchNeedToBeConsumedEvent(SERVICE_EVENTS_ENUMERATION serviceId, Object... parameters) {
		if (link.areEventsNotConsumable(serviceId)) {
			return false;
		} else {
			link.reportConsumableEvent(new IndirectMethodInvocationInfo<SERVICE_EVENTS_ENUMERATION>(serviceId, parameters));
			return true;
		}
	}
	
	protected boolean dispatchListenableAndNeedToBeConsumedEvent(SERVICE_EVENTS_ENUMERATION serviceId, Object... parameters) {
		if (link.areEventsNotConsumable(serviceId)) {
			return false;
		} else {
			IndirectMethodInvocationInfo<SERVICE_EVENTS_ENUMERATION> event = new IndirectMethodInvocationInfo<SERVICE_EVENTS_ENUMERATION>(serviceId, parameters);
			link.reportListenableEvent(event);
			link.reportConsumableEvent(event);
			return true;
		}
	}
	
	/** @see IEventLink#addClient(EventClient) */
	public boolean addClient(EventClient<SERVICE_EVENTS_ENUMERATION> client) throws IndirectMethodNotFoundException {
		return link.addClient(client);
	}
	
	/** @see IEventLink#deleteClient(EventClient) */
	public boolean deleteClient(EventClient<SERVICE_EVENTS_ENUMERATION> client) {
		return link.deleteClient(client);
	}
	
}

