package mutua.icc.instrumentation.handlers;

import java.util.ArrayList;

import static mutua.icc.instrumentation.DefaultInstrumentationEvents.UNFINISHED_REQUEST_EVENT;

import mutua.icc.instrumentation.dto.InstrumentationEventDto;

/** <pre>
 * InstrumentationHandlerRAM.java
 * ==============================
 * (created by luiz, May 3, 2016)
 *
 * An instrumentation handler which keeps events in RAM between the start the finish of requests.
 *
 * @see InstrumentationHandlerProfilerLog
 * @version $Id$
 * @author luiz
 */

public abstract class InstrumentationHandlerRAM implements IInstrumentationHandler {
	
	private static ThreadLocal<ArrayList<InstrumentationEventDto>> threadEventsTL = new ThreadLocal<ArrayList<InstrumentationEventDto>>() {
		@Override
		protected ArrayList<InstrumentationEventDto> initialValue() {
			return new ArrayList<InstrumentationEventDto>(32);
		}
	};
	
	@Override
	public void onRequestStart(InstrumentationEventDto requestStartInstrumentationEvent) {
		ArrayList<InstrumentationEventDto> threadEvents = threadEventsTL.get();
		if (threadEvents.size() != 0) {
			onRequestFinish(new InstrumentationEventDto(System.currentTimeMillis(), Thread.currentThread(), UNFINISHED_REQUEST_EVENT));
		}
		threadEvents.add(requestStartInstrumentationEvent);
	}

	@Override
	public void onInstrumentationEvent(InstrumentationEventDto instrumentationEvent) {
		ArrayList<InstrumentationEventDto> threadEvents = threadEventsTL.get();
		threadEvents.add(instrumentationEvent);
	}

	@Override
	public void onRequestFinish(InstrumentationEventDto requestFinishInstrumentationEvent) {
		ArrayList<InstrumentationEventDto> threadEvents = threadEventsTL.get();
		threadEvents.add(requestFinishInstrumentationEvent);
		analyzeRequest(threadEvents);
		threadEvents.clear();
	}

	/** method called just after an application request finishes, passing along the list of instrumentation events raised during this processing.
	 *  overriddings of this method must not keep the 'requestEvents' object, since there is a weak reference to it (collected when the processing
	 *  thread dies) and it will be reused for the next request of the same thread.
	 *  Implementations should summarize the request in one line, giving meaningful names to the important events. */
	public abstract void analyzeRequest(ArrayList<InstrumentationEventDto> requestEvents);

}
