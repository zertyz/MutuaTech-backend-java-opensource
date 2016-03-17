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
	
	/** @see IEventLink#reportConsumableEvent */
	protected int dispatchConsumableEvent(SERVICE_EVENTS_ENUMERATION serviceId, Object... parameters) {
		return link.reportConsumableEvent(new IndirectMethodInvocationInfo<SERVICE_EVENTS_ENUMERATION>(serviceId, parameters));
	}

	protected void dispatchListenableAndConsumableEvent(SERVICE_EVENTS_ENUMERATION serviceId, Object... parameters) {
		IndirectMethodInvocationInfo<SERVICE_EVENTS_ENUMERATION> event = new IndirectMethodInvocationInfo<SERVICE_EVENTS_ENUMERATION>(serviceId, parameters);
		link.reportListenableEvent(event);
		link.reportConsumableEvent(event);
	}
	
	/** @see IEventLink#addListener(EventClient) */
	public boolean addListener(EventClient<SERVICE_EVENTS_ENUMERATION> listenerClient) throws IndirectMethodNotFoundException {
		return link.addListener(listenerClient);
	}
	
	/** @see IEventLink#removeListener(EventClient) */
	public boolean removeListener(EventClient<SERVICE_EVENTS_ENUMERATION> listenerClient) {
		return link.removeListener(listenerClient);
	}
	
	/** @see IEventLink#setConsumer(EventClient) */
	public void setConsumer(EventClient<SERVICE_EVENTS_ENUMERATION> consumerClient) throws IndirectMethodNotFoundException {
		link.setConsumer(consumerClient);
	}
	
	/** @see IEventLink#unsetConsumer() */
	public void unsetConsumer() {
		link.unsetConsumer();
	}
	
}

