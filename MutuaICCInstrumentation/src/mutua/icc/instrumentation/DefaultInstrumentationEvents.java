package mutua.icc.instrumentation;

import static mutua.icc.instrumentation.DefaultInstrumentationProperties.*;

/** <pre>
 * DefaultInstrumentationEvents.java
 * =================================
 * (created by luiz, Jan 21, 2015)
 *
 * Defines the common instrumentation events that can participate on instrumentation logs
 *
 * @see RelatedClass(es)
 * @version $Id$
 * @author luiz
 */

public class DefaultInstrumentationEvents {

	public static IInstrumentableEvent DIE_APP_START           = new IInstrumentableEvent("APP_START");
	public static IInstrumentableEvent DIE_APP_SHUTDOWN        = new IInstrumentableEvent("APP_SHUTDOWN");
	public static IInstrumentableEvent DIE_UNCOUGHT_EXCEPTION  = new IInstrumentableEvent("UNCOUGHT_EXCEPTION", DIP_MSG, DIP_THROWABLE);
	public static IInstrumentableEvent DIE_REPORTED_THROWABLE  = new IInstrumentableEvent("REPORTED_THROWABLE", DIP_MSG, DIP_THROWABLE);
	public static IInstrumentableEvent DIE_ERROR               = new IInstrumentableEvent("ERROR",              DIP_MSG);
	public static IInstrumentableEvent DIE_WARNING             = new IInstrumentableEvent("WARNING",            DIP_MSG);
	public static IInstrumentableEvent DIE_DEBUG               = new IInstrumentableEvent("DEBUG",              DIP_MSG);
		
}
