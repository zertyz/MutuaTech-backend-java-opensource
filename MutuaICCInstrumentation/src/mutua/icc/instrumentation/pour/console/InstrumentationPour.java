package mutua.icc.instrumentation.pour.console;

import mutua.events.annotations.EventListener;
import mutua.icc.instrumentation.IInstrumentableProperty;
import mutua.icc.instrumentation.Instrumentation.EInstrumentationPropagableEvents;
import mutua.icc.instrumentation.dto.InstrumentationEventDto;
import mutua.icc.instrumentation.eventclients.InstrumentationPropagableEventsClient;
import mutua.icc.instrumentation.pour.IInstrumentationPour;
import mutua.icc.instrumentation.pour.SerializationRules;
import mutua.serialization.ISerializationRule;
import mutua.serialization.SerializationRepository;

/** <pre>
 * InstrumentationPour.java
 * ========================
 * (created by luiz, Jan 21, 2015)
 *
 * Implements the CONSOLE version of 'IInstrumentationData'
 *
 * @see IInstrumentationPour
 * @version $Id$
 * @author luiz
 */

public class InstrumentationPour implements IInstrumentationPour, InstrumentationPropagableEventsClient<EInstrumentationPropagableEvents> {


	private ISerializationRule<InstrumentationEventDto> logEventDtoSerializationRule;
	private SerializationRepository serializer;

	public InstrumentationPour(IInstrumentableProperty[] instrumentationProperties) {
		serializer = new SerializationRepository(instrumentationProperties);
		logEventDtoSerializationRule = new SerializationRules(serializer);
		serializer.addSerializationRule(logEventDtoSerializationRule);
	}

	@Override
	public void considerInstrumentableProperties(IInstrumentableProperty[] instrumentableProperties) {
		serializer.addSerializationRules(instrumentableProperties);
	}

	@Override
	public void reset() {}

	@Override
	@EventListener({"INTERNAL_FRAMEWORK_INSTRUMENTATION_EVENT",
	                "APPLICATION_INSTRUMENTATION_EVENT"})
	public void storeInstrumentableEvent(InstrumentationEventDto event) {
		StringBuffer logLine = new StringBuffer();
		serializer.serialize(logLine, event);
		System.out.println(logLine);
	}

	@Override
	public int startTraversal() {
		return -1;
	}

	@Override
	public int startFollowing() {
		return -1;
	}

	@Override
	public InstrumentationEventDto getNextEvent(int descriptor) {
		return null;
	}

	@Override
	public void closeDescriptor(int descriptor) {}
	
}
