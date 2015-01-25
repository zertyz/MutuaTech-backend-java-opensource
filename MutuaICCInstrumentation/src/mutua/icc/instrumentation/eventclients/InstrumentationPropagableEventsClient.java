package mutua.icc.instrumentation.eventclients;

import mutua.events.EventClient;
import mutua.events.annotations.EventListener;

/** <pre>
 * InstrumentationPropagableEventsClient.java
 * ==========================================
 * (created by luiz, Jan 24, 2015)
 *
 * Base class for all instrumentation propagable events clients, where
 * 'E' is the enumeration of events recognized by this client, whose
 * methods must be annotated with '@EventListener{enum1, ...}'
 *
 * @see RelatedClass(es)
 * @version $Id$
 * @author luiz
 */

public interface InstrumentationPropagableEventsClient<E> extends EventClient<E> {

}
