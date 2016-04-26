package mutua.icc.instrumentation;

import mutua.icc.instrumentation.handlers.IInstrumentationHandler;

/** <pre>
 * InstrumentableEvent.java
 * ========================
 * (created by luiz, Jan 24, 2015)
 *
 * This class defines an "Instrumentable Event", along with information on how it should behave
 * when the event hits 
 *
 * @version $Id$
 * @author luiz
 */

public class InstrumentableEvent {
	
	public final String                                     eventName;
	public final Class<? extends IInstrumentationHandler>[] targetHandlers;
	public final int                                        eventFlags;
	
	public InstrumentableEvent(String eventName, int eventFlags, Class<? extends IInstrumentationHandler>... targetHandlers) {
		this.eventName      = eventName;
		this.eventFlags     = eventFlags;
		this.targetHandlers = targetHandlers;
	}
}
