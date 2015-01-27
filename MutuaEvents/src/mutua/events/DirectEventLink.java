package mutua.events;

import mutua.imi.IndirectMethodInvocationInfo;
import mutua.imi.IndirectMethodInvoker;
import mutua.imi.IndirectMethodNotFoundException;

/** <pre>
 * DirectEventLink.java
 * ====================
 * (created by luiz, Jan 23, 2015)
 *
 * Implements a in-memory, same thread, blocking server dispatching 'IEventLink'
 *
 * @see IEventLink
 * @version $Id$
 * @author luiz
 */

public class DirectEventLink<SERVICE_EVENTS_ENUMERATION> extends IEventLink<SERVICE_EVENTS_ENUMERATION> {

	public DirectEventLink(Class<SERVICE_EVENTS_ENUMERATION> eventsEnumeration) {
		super(eventsEnumeration);
	}

	@Override
	public void reportListenableEvent(IndirectMethodInvocationInfo<SERVICE_EVENTS_ENUMERATION> event) {
		for (EventClient<SERVICE_EVENTS_ENUMERATION> client : clientsAndListenerMethodInvokers.keySet()) try {
			IndirectMethodInvoker<SERVICE_EVENTS_ENUMERATION> imi = clientsAndListenerMethodInvokers.get(client);
			imi.invokeMethod(event);
		} catch (IndirectMethodNotFoundException e) {
			// nothing to do -- there is no problema if no one is listening
		}
	}

	@Override
	public void reportConsumableEvent(IndirectMethodInvocationInfo<SERVICE_EVENTS_ENUMERATION> event) {
		for (EventClient<SERVICE_EVENTS_ENUMERATION> client : clientsAndConsumerMethodInvokers.keySet()) try {
			IndirectMethodInvoker<SERVICE_EVENTS_ENUMERATION> imi = clientsAndConsumerMethodInvokers.get(client);
			imi.invokeMethod(event);
		} catch (IndirectMethodNotFoundException e) {
			//e.printStackTrace();
		}
	}

}
