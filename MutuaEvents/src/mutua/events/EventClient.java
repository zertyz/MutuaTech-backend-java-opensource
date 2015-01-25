package mutua.events;

/** <pre>
 * EventClient.java
 * ================
 * (created by luiz, Jan 23, 2015)
 *
 * Base class for all event clients, where 'SERVICE_EVENTS_ENUMERATION' is the
 * enumeration of events recognized by this client, whose methods must be
 * annotated with '@EventListener{enum1, ...}'
 *
 * @see RelatedClass(es)
 * @version $Id$
 * @author luiz
 */

public interface EventClient<SERVICE_EVENTS_ENUMERATION> {

}
