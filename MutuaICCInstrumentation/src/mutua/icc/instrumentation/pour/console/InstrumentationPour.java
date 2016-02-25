package mutua.icc.instrumentation.pour.console;

import mutua.icc.instrumentation.IInstrumentableProperty;
import mutua.icc.instrumentation.Instrumentation.EInstrumentationPropagableEvents;
import mutua.icc.instrumentation.Instrumentation.InstrumentationPropagableEvent;
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

	private StringBuffer logLine = new StringBuffer();
	
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
	@InstrumentationPropagableEvent({EInstrumentationPropagableEvents.INTERNAL_FRAMEWORK_INSTRUMENTATION_EVENT,
	                                 EInstrumentationPropagableEvents.APPLICATION_INSTRUMENTATION_EVENT})
	public void storeInstrumentableEvent(InstrumentationEventDto event) {
		String l;
		synchronized (logLine) {
			logLine.setLength(0);
			serializer.serialize(logLine, event);
			l = logLine.toString();
		}
		System.out.println(l);
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
