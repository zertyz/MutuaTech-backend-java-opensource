package mutua.icc.instrumentation.pour.rotatingfile;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import mutua.events.annotations.EventListener;
import mutua.icc.instrumentation.IInstrumentableProperty;
import mutua.icc.instrumentation.dto.InstrumentationEventDto;
import mutua.icc.instrumentation.pour.IInstrumentationPour;
import mutua.icc.instrumentation.pour.SerializationRules;
import mutua.serialization.ISerializationRule;
import mutua.serialization.SerializationRepository;

/** <pre>
 * InstrumentationPour.java
 * ========================
 * (created by luiz, Feb 4, 2015)
 *
 * Implements the ROTATING_FILE version of 'IInstrumentationData'
 *
 * @see RelatedClass(es)
 * @version $Id$
 * @author luiz
 */

public class InstrumentationPour implements IInstrumentationPour {

	private ISerializationRule<InstrumentationEventDto> logEventDtoSerializationRule;
	private SerializationRepository serializer;
	private FileOutputStream fout;

	public InstrumentationPour(IInstrumentableProperty[] instrumentationProperties, String logFileName) throws FileNotFoundException {
		serializer = new SerializationRepository(instrumentationProperties);
		logEventDtoSerializationRule = new SerializationRules(serializer);
		serializer.addSerializationRule(logEventDtoSerializationRule);
		fout = new FileOutputStream(logFileName, true);
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
	public void storeInstrumentableEvent(InstrumentationEventDto event) throws IOException {
		StringBuffer logLine = new StringBuffer();
		serializer.serialize(logLine, event);
		logLine.append('\n');
		fout.write(logLine.toString().getBytes());
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
