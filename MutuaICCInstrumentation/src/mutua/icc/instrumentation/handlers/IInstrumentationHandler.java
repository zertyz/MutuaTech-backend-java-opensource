package mutua.icc.instrumentation.handlers;

import mutua.icc.instrumentation.dto.InstrumentationEventDto;

/** <pre>
 * IInstrumentationHandler.java
 * ============================
 * (created by luiz, Apr 19, 2016)
 *
 * Defines the methods by which instrumentation handlers get notified of
 * instrumentation events
 *
 * @see InstrumentationEventDto
 * @see InstrumentationHandlerLog
 * @see InstrumentationHandlerReport
 * @see InstrumentationHandlerProfiler
 * @version $Id$
 * @author luiz
*/

public interface IInstrumentationHandler {
	
	/** Event raised when a new request starts */
	void onRequestStart(InstrumentationEventDto requestStartInstrumentationEvent);
	
	/** Event raised when new information is available within the request */
	void onInstrumentationEvent(InstrumentationEventDto instrumentationEvent);
	
	/** Event raised when a request is completed */
	void onRequestFinish(InstrumentationEventDto requestFinishInstrumentationEvent);
	
	/** Event raised when the application is shutting down gracefully */
	void close();

}
