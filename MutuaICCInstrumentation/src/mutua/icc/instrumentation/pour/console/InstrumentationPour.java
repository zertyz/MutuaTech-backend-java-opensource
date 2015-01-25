package mutua.icc.instrumentation.pour.console;

import java.text.FieldPosition;
import java.text.SimpleDateFormat;

import mutua.events.annotations.EventListener;
import mutua.icc.instrumentation.InstrumentableEvent;
import mutua.icc.instrumentation.IInstrumentableProperty;
import mutua.icc.instrumentation.Instrumentation.EInstrumentationPropagableEvents;
import mutua.icc.instrumentation.dto.InstrumentationEventDto;
import mutua.icc.instrumentation.eventclients.InstrumentationPropagableEventsClient;
import mutua.icc.instrumentation.pour.IInstrumentationPour;
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

		
	private ISerializationRule<InstrumentationEventDto> logEventDtoSerializationRule = new ISerializationRule<InstrumentationEventDto>() {

		private SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd|HH:mm:ss.SSS|zzz");
		private FieldPosition fp = new FieldPosition(0);

		@Override
		public Class<InstrumentationEventDto> getType() {
			return InstrumentationEventDto.class;
		}

		@Override
		public void appendSerializedValue(StringBuffer buffer, InstrumentationEventDto logEvent) {
			long currentTimeMillis                                  = logEvent.getCurrentTimeMillis();
			String applicationName                                  = logEvent.getApplicationName();
			String threadInfo                                       = logEvent.getThreadInfo();
			InstrumentableEvent instrumentableEvent                 = logEvent.getEvent();
			IInstrumentableProperty[] instrumentableEventProperties = instrumentableEvent.getProperties();
			sdf.format(currentTimeMillis, buffer, fp);
			buffer.append("|").append(currentTimeMillis).append(", ").
			append(applicationName).append('-').append(threadInfo).append(": ").
			append(instrumentableEvent.getName());
			if (instrumentableEventProperties.length > 0) {
				buffer.append(" {");
				for (int i=0; i<instrumentableEventProperties.length; i++) {
					IInstrumentableProperty instrumentableProperty = instrumentableEventProperties[i];
					buffer.append(instrumentableProperty.getInstrumentationPropertyName()).append(" = ");
					Object logEventPropertyValue = logEvent.getValue(instrumentableProperty);
					Class<?> type = instrumentableProperty.getType();
					if ((type == Integer.TYPE) || (type == Long.TYPE)) {
						serializer.serialize(buffer, logEventPropertyValue);
					} else if (type == String.class) {
						buffer.append('"');
						serializer.serialize(buffer, logEventPropertyValue);
						buffer.append('"');
					} else {
						buffer.append('{');
						serializer.serialize(buffer, logEventPropertyValue);
						buffer.append('}');
					}
					if (i < (instrumentableEventProperties.length-1)) {
						buffer.append(", ");
					}
				}
				buffer.append("}");
			}
		}
		
	};

	private SerializationRepository serializer;
	
	public InstrumentationPour(IInstrumentableProperty[] instrumentationProperties) {
		serializer = new SerializationRepository(instrumentationProperties); 
		serializer.addSerializationRule(logEventDtoSerializationRule);
	}


	// IInstrumentationPour implementation
	//////////////////////////////////////

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
