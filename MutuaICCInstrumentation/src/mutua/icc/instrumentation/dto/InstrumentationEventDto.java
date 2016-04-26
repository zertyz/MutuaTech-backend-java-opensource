package mutua.icc.instrumentation.dto;

import mutua.icc.instrumentation.InstrumentableEvent;
import mutua.icc.instrumentation.InstrumentableProperty;

/** <pre>
 * EventDto.java
 * =============
 * (created by luiz, Jan 21, 2015)
 *
 * Represents an instrumentation event
 *
 * @version $Id$
 * @author luiz
 */

// TODO check properties against the ones registered on IInstrumentableEvent

public class InstrumentationEventDto {
	
	public final long currentTimeMillis;
	public final String threadInfo;
	public final InstrumentableEvent instrumentableEvent;
	/** := {IInstrumentableProperty(parameterName), Object(value), ...} */
	public final Object[] propertiesAndValues;
	
	public InstrumentationEventDto(long currentTimeMillis, Thread thread, InstrumentableEvent event) {
		this.currentTimeMillis   = currentTimeMillis;
		this.threadInfo          = thread.toString();
		this.instrumentableEvent = event;
		this.propertiesAndValues = null;
	}

	public InstrumentationEventDto(long currentTimeMillis, Thread thread, InstrumentableEvent event,
		                           InstrumentableProperty property, Object value) {
		this.currentTimeMillis   = currentTimeMillis;
		this.threadInfo          = thread.toString();
		this.instrumentableEvent = event;
		this.propertiesAndValues = new Object[] {property, value};
	}

	public InstrumentationEventDto(long currentTimeMillis, Thread thread, InstrumentableEvent event,
		                           InstrumentableProperty property1, Object value1,
		                           InstrumentableProperty property2, Object value2) {
		this.currentTimeMillis   = currentTimeMillis;
		this.threadInfo          = thread.toString();
		this.instrumentableEvent = event;
		this.propertiesAndValues = new Object[] {property1, value1, property2, value2};
	}

	public InstrumentationEventDto(long currentTimeMillis, Thread thread, InstrumentableEvent event,
		                           InstrumentableProperty property1, Object value1,
		                           InstrumentableProperty property2, Object value2,
		                           InstrumentableProperty property3, Object value3) {
		this.currentTimeMillis   = currentTimeMillis;
		this.threadInfo          = thread.toString();;
		this.instrumentableEvent = event;
		this.propertiesAndValues = new Object[] {property1, value1, property2, value2, property3, value3};
	}

}