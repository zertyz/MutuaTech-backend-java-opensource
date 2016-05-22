package mutua.icc.instrumentation.handlers;

import mutua.icc.instrumentation.dto.InstrumentationEventDto;

/** <pre>
 * InstrumentationHandlerNull.java
 * ===============================
 * (created by luiz, May 22, 2016)
 *
 * Instrumentation handler to be used by simple & test applications, specially not to persist
 * or output any activity related to reports, profiling or even logs. 
 *
 * @version $Id$
 * @author luiz
 */

public class InstrumentationHandlerNull implements IInstrumentationHandler {
	
	public static InstrumentationHandlerNull instance = new InstrumentationHandlerNull();
	
	private InstrumentationHandlerNull() {}

	@Override
	public void onRequestStart(InstrumentationEventDto requestStartInstrumentationEvent) {}

	@Override
	public void onInstrumentationEvent(InstrumentationEventDto instrumentationEvent) {}

	@Override
	public void onRequestFinish(InstrumentationEventDto requestFinishInstrumentationEvent) {}

	@Override
	public void close() {}

}
