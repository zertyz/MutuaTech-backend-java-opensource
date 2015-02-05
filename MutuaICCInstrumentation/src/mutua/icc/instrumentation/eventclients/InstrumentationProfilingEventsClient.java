package mutua.icc.instrumentation.eventclients;

import java.io.IOException;
import java.util.Hashtable;

import mutua.events.annotations.EventListener;
import mutua.icc.instrumentation.IInstrumentableEvent;
import mutua.icc.instrumentation.IInstrumentableProperty;
import mutua.icc.instrumentation.InstrumentableEvent;
import mutua.icc.instrumentation.Instrumentation;
import mutua.icc.instrumentation.Instrumentation.EInstrumentationPropagableEvents;
import mutua.icc.instrumentation.dto.InstrumentationEventDto;
import mutua.icc.instrumentation.pour.IInstrumentationPour;
import mutua.icc.instrumentation.pour.PourFactory;
import mutua.icc.instrumentation.pour.PourFactory.EInstrumentationDataPours;

/** <pre>
 * InstrumentationProfilingEventsClient.java
 * =========================================
 * (created by luiz, Jan 24, 2015)
 *
 * Implements the profiling instrumentation
 *
 * @see RelatedClass(es)
 * @version $Id$
 * @author luiz
 */

public class InstrumentationProfilingEventsClient implements InstrumentationPropagableEventsClient<EInstrumentationPropagableEvents> {

	
	private final Instrumentation<?, ?> log;
	private final IInstrumentationPour pour;
	
	//private final WeakHashMap<String, Long> requestStartTimes;
	private final Hashtable<String, Long> requestStartTimes;
	
	
	public InstrumentationProfilingEventsClient(Instrumentation<?, ?> log, EInstrumentationDataPours pourType, String descriptorReference) {
		this.log  = log;
		this.pour = PourFactory.getInstrumentationPour(pourType, descriptorReference, EProfileInstrumentationProperties.values());
		//requestStartTimes = new WeakHashMap<String, Long>();
		requestStartTimes = new Hashtable<String, Long>();
	}

	public InstrumentationProfilingEventsClient(Instrumentation<?, ?> log, EInstrumentationDataPours pourType) {
		this(log, pourType, null);
		if ((pourType != EInstrumentationDataPours.CONSOLE) &&
			(pourType != EInstrumentationDataPours.RAM)) {
			throw new RuntimeException("When using a '"+pourType.name()+"' pour type, please provide a 'descriptorReference' (available on the other constructor)");
		}
	}

	@EventListener({"INTERNAL_FRAMEWORK_INSTRUMENTATION_EVENT"})
	public void handleInternalFrameworkInstrumentationEventNotification(InstrumentationEventDto applicationEvent) throws IOException {
		InstrumentableEvent instrumentableEvent = applicationEvent.getEvent();
		String applicationName = applicationEvent.getApplicationName();
		String threadInfo = applicationEvent.getThreadInfo();
		long eventTime = applicationEvent.getCurrentTimeMillis();
		
		if (instrumentableEvent == log.REQUEST_START_EVENT) {
			requestStartTimes.put(threadInfo, eventTime);
		} else if (instrumentableEvent == log.REQUEST_FINISH_EVENT) {
			long requestStartTime = requestStartTimes.get(threadInfo);
			requestStartTimes.remove(threadInfo);
			long elapsedTime = eventTime - requestStartTime;
			Thread currentThread = Thread.currentThread();
			InstrumentationEventDto profileEvent = new InstrumentationEventDto(System.currentTimeMillis(), applicationName, currentThread,
			                                                                   EProfileInstrumentationEvents.PROFILE_RESULT.getInstrumentableEvent(),
			                                                                   EProfileInstrumentationProperties.ELAPSED_MILLIS,   elapsedTime,
			                                                                   EProfileInstrumentationProperties.MEASUREMENT_NAME, "requestDuration");
			pour.storeInstrumentableEvent(profileEvent);
		} else {
//			System.out.println("PROFILE: unimportant internal event");
		}
	}
	
}


//instrumentation events & properties
//////////////////////////////////////

enum EProfileInstrumentationProperties implements IInstrumentableProperty {

	
	ELAPSED_MILLIS   ("elapsedMillis",   Long.TYPE),
	MEASUREMENT_NAME ("measurementName", String.class),
	
	
	;

	
	private String instrumentationPropertyName;
	private Class<?> instrumentationPropertyType;
	
	
	private EProfileInstrumentationProperties(String instrumentationPropertyName, Class<?> instrumentationPropertyType) {
		this.instrumentationPropertyName = instrumentationPropertyName;
		this.instrumentationPropertyType = instrumentationPropertyType;
	}

	
	// IInstrumentableProperty implementation
	/////////////////////////////////////////
	
	@Override
	public String getInstrumentationPropertyName() {
		return instrumentationPropertyName;
	}

	
	// ISerializationRule implementation
	////////////////////////////////////
	
	@Override
	public Class<?> getType() {
		return instrumentationPropertyType;
	}

	@Override
	public void appendSerializedValue(StringBuffer buffer, Object value) {
		throw new RuntimeException("Serialization Rule '" + this.getClass().getName() +
                                   "' didn't overrode 'appendSerializedValue' from " +
                                   "'ISerializationRule' for type '" + instrumentationPropertyType);
	}

}

enum EProfileInstrumentationEvents implements IInstrumentableEvent {

	
	PROFILE_RESULT("PROFILER RESULT", EProfileInstrumentationProperties.MEASUREMENT_NAME, EProfileInstrumentationProperties.ELAPSED_MILLIS),
	
	
	;
	
	
	private InstrumentableEvent instrumentableEvent;
	
	private EProfileInstrumentationEvents(String name, IInstrumentableProperty property) {
		instrumentableEvent = new InstrumentableEvent(name, property);
	}
	
	private EProfileInstrumentationEvents(String name, IInstrumentableProperty property1, IInstrumentableProperty property2) {
		instrumentableEvent = new InstrumentableEvent(name, property1, property2);
	}
	
	private EProfileInstrumentationEvents(String name) {
		instrumentableEvent = new InstrumentableEvent(name);
	}

	@Override
	public InstrumentableEvent getInstrumentableEvent() {
		return instrumentableEvent;
	}
}
