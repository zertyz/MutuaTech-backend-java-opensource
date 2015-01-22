package mutua.icc.instrumentation;

import static mutua.icc.instrumentation.InstrumentationProperties.*;

/** <pre>
 * InstrumentationEvents.java
 * ==========================
 * (created by luiz, Jan 21, 2015)
 *
 * Defines the available events that can participate on instrumentation logs
 *
 * @see RelatedClass(es)
 * @version $Id$
 * @author luiz
 */

public class InstrumentationEvents {
	
	public static IInstrumentableEvent NOPROP_EVENT  = new IInstrumentableEvent("NOPROP_EVENT");
	public static IInstrumentableEvent ONEPROP_EVENT = new IInstrumentableEvent("ONEPROP_EVENT", DAY_OF_WEEK);
	public static IInstrumentableEvent TWOPROP_EVENT = new IInstrumentableEvent("TWOPROP_EVENT", DAY_OF_WEEK, MAIL);

}
