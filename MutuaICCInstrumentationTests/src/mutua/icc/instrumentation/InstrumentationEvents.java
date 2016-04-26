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

public enum InstrumentationEvents implements IInstrumentableEvent {
	
	
	NOPROP_EVENT  ("NOPROP_EVENT"),
	ONEPROP_EVENT ("ONEPROP_EVENT", DAY_OF_WEEK),
	TWOPROP_EVENT ("TWOPROP_EVENT", DAY_OF_WEEK, MAIL),

	
	;
	
	
	private InstrumentableEvent instrumentableEvent;
	
	private InstrumentationEvents(String name, InstrumentableProperty property) {
		instrumentableEvent = new InstrumentableEvent(name, property);
	}
	
	private InstrumentationEvents(String name, InstrumentableProperty property1, InstrumentableProperty property2) {
		instrumentableEvent = new InstrumentableEvent(name, property1, property2);
	}
	
	private InstrumentationEvents(String name) {
		instrumentableEvent = new InstrumentableEvent(name);
	}

	@Override
	public InstrumentableEvent getInstrumentableEvent() {
		return instrumentableEvent;
	}


}
