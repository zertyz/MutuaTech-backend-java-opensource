package mutua.icc.instrumentation;

import mutua.icc.instrumentation.InstrumentableEvent.ELogSeverity;

/** <pre>
 * DefaultInstrumentationEvents.java
 * =================================
 * (created by luiz, Jan 21, 2015)
 *
 * Defines the internal instrumentation events used by the Instrumentation facility
 *
 * @version $Id$
 * @author luiz
 */

public class DefaultInstrumentationEvents {

	// 'InstrumentableEvent's
	/////////////////////////
	
	// ... 'Instrumentation' internal events
	public static final InstrumentableEvent APP_START_EVENT;
	public static final InstrumentableEvent APP_SHUTDOWN_EVENT;
	public static final InstrumentableEvent REQUEST_START_EVENT;
	public static final InstrumentableEvent REQUEST_FINISH_EVENT;
	public static final InstrumentableEvent UNCOUGHT_EXCEPTION_EVENT;
	public static final InstrumentableEvent REPORTED_THROWABLE_EVENT;
	public static final InstrumentableEvent DEBUG_EVENT;

	// ... 'RAM' event handler internal events
	public static final InstrumentableEvent UNFINISHED_REQUEST_EVENT;
	
	// ... 'Profile' event handler internal events
	public static final InstrumentableEvent PROFILED_REQUEST_EVENT;
	
	// 'InstrumentableProperty'ies
	//////////////////////////////
	
	// ... 'Instrumentation' internal properties
	public static final InstrumentableProperty MSG_PROPERTY;
	public static final InstrumentableProperty THROWABLE_PROPERTY;
	
	
	static {
		MSG_PROPERTY       = new InstrumentableProperty("msg",        String.class);
		THROWABLE_PROPERTY = new InstrumentableProperty("stackTrace", Throwable.class);

		APP_START_EVENT          = new InstrumentableEvent("APP_START",          ELogSeverity.CRITICAL);
		APP_SHUTDOWN_EVENT       = new InstrumentableEvent("APP_SHUTDOWN",       ELogSeverity.CRITICAL);
		REQUEST_START_EVENT      = new InstrumentableEvent("REQUEST START",      ELogSeverity.CRITICAL);
		REQUEST_FINISH_EVENT     = new InstrumentableEvent("REQUEST FINISH",     ELogSeverity.CRITICAL);
		UNCOUGHT_EXCEPTION_EVENT = new InstrumentableEvent("UNCOUGHT_EXCEPTION", ELogSeverity.ERROR,    THROWABLE_PROPERTY);
		REPORTED_THROWABLE_EVENT = new InstrumentableEvent("REPORTED_THROWABLE", ELogSeverity.ERROR,    THROWABLE_PROPERTY);
		DEBUG_EVENT              = new InstrumentableEvent("DEBUG",              ELogSeverity.DEBUG);

		UNFINISHED_REQUEST_EVENT = new InstrumentableEvent("UNFINISHED REQUEST", ELogSeverity.ERROR,    (InstrumentableProperty[])null);
		
		PROFILED_REQUEST_EVENT   = new InstrumentableEvent("PROFILED REQUEST",   ELogSeverity.INFO);

	}
	
}
