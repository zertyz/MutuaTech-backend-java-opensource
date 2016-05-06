package mutua.icc.instrumentation.handlers;

import java.util.ArrayList;

import static mutua.icc.instrumentation.DefaultInstrumentationEvents.*;

import mutua.icc.instrumentation.InstrumentableEvent;
import mutua.icc.instrumentation.InstrumentableProperty;
import mutua.icc.instrumentation.dto.InstrumentationEventDto;

/** <pre>
 * InstrumentationHandlerProfilerLog.java
 * ======================================
 * (created by luiz, May 3, 2016)
 *
 * An instrumentation handler doing profiling computations which outputs to a log instrumentation handler 
 *
 * @version $Id$
 * @author luiz
 */

public class InstrumentationHandlerProfilerLog extends InstrumentationHandlerRAM {
	
	private final IInstrumentationHandler log;
	private final Object[]                eventTransitions;
	
	/** eventTransitions defines which events we want to measure times for, as well as a 'Long' 'InstrumentableProperty' with a meaningful name:
	 *  eventTransitions := {eventA, eventB, (InstrumentableProperty)measurementName, ...} */
	public InstrumentationHandlerProfilerLog(IInstrumentationHandler log, Object... eventTransitions) {
		this.log              = log;
		this.eventTransitions = eventTransitions;
	}

	@Override
	public void close() {
		log.close();
	}

	@Override
	public void analyzeRequest(ArrayList<InstrumentationEventDto> requestEvents) {
		int mt = 0;
		Object[] measuredTimes = new Object[(eventTransitions.length/3)*2];
		
		int evt = 0;
		InstrumentableEvent    interestingFirstInstrumentableEvent  = (InstrumentableEvent)    eventTransitions[evt];
		InstrumentableEvent    interestingSecondInstrumentableEvent = (InstrumentableEvent)    eventTransitions[evt+1];
		InstrumentableProperty measurementProperty                  = (InstrumentableProperty) eventTransitions[evt+2];
		
		for (int i = 0; i<requestEvents.size(); i++) {
			InstrumentationEventDto interestingFirstEventCandidate               = requestEvents.get(i);
			InstrumentableEvent     interestingFirstInstrumentableEventCandidate = interestingFirstEventCandidate.instrumentableEvent;
			
			if (interestingFirstInstrumentableEventCandidate == interestingFirstInstrumentableEvent) {
				InstrumentationEventDto interestingSecondEventCandidate               = requestEvents.get(i+1);
				InstrumentableEvent     interestingSecondInstrumentableEventCandidate = interestingSecondEventCandidate.instrumentableEvent;
				
				if (interestingSecondInstrumentableEventCandidate == interestingSecondInstrumentableEvent) {
					// we found a transition
					measuredTimes[mt++] = measurementProperty;
					measuredTimes[mt++] = interestingSecondEventCandidate.currentTimeMillis - interestingFirstEventCandidate.currentTimeMillis;
					// advance with evt
					evt += 3;
					if (evt >= eventTransitions.length) {
						break;
					} else {
						interestingFirstInstrumentableEvent  = (InstrumentableEvent)    eventTransitions[evt];
						interestingSecondInstrumentableEvent = (InstrumentableEvent)    eventTransitions[evt+1];
						measurementProperty                  = (InstrumentableProperty) eventTransitions[evt+2];
					}
				}
			}
		}
		
		log.onInstrumentationEvent(new InstrumentationEventDto(System.currentTimeMillis(), Thread.currentThread(), PROFILED_REQUEST_EVENT, measuredTimes));
	}

}
