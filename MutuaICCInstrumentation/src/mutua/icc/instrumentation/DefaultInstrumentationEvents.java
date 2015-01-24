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

public enum DefaultInstrumentationEvents implements IInstrumentableEvent {

	
	DIE_APP_START          ("APP_START"),
	DIE_APP_SHUTDOWN       ("APP_SHUTDOWN"),
	DIE_UNCOUGHT_EXCEPTION ("UNCOUGHT_EXCEPTION", DIP_MSG, DIP_THROWABLE),
	DIE_REPORTED_THROWABLE ("REPORTED_THROWABLE", DIP_MSG, DIP_THROWABLE),
	DIE_ERROR              ("ERROR",              DIP_MSG),
	DIE_WARNING            ("WARNING",            DIP_MSG),
	DIE_DEBUG              ("DEBUG",              DIP_MSG),
	
	
	;
	
	
	private InstrumentableEvent instrumentableEvent;
	
	private DefaultInstrumentationEvents(String name, IInstrumentableProperty property) {
		instrumentableEvent = new InstrumentableEvent(name, property);
	}
	
	private DefaultInstrumentationEvents(String name, IInstrumentableProperty property1, IInstrumentableProperty property2) {
		instrumentableEvent = new InstrumentableEvent(name, property1, property2);
	}
	
	private DefaultInstrumentationEvents(String name) {
		instrumentableEvent = new InstrumentableEvent(name);
	}

	@Override
	public InstrumentableEvent getInstrumentableEvent() {
		return instrumentableEvent;
	}
}
