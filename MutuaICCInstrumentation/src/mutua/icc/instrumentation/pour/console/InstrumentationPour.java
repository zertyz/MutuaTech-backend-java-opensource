package mutua.icc.instrumentation.pour.console;

import java.text.FieldPosition;
import java.text.SimpleDateFormat;

import mutua.icc.instrumentation.InstrumentableEvent;
import mutua.icc.instrumentation.IInstrumentableProperty;
import mutua.icc.instrumentation.dto.EventDto;
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

public class InstrumentationPour extends IInstrumentationPour {

		
	private ISerializationRule<EventDto> logEventDtoSerializationRule = new ISerializationRule<EventDto>() {

		private SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd|HH:mm:ss.SSS|zzz");
		private FieldPosition fp = new FieldPosition(0);

		@Override
		public Class<EventDto> getType() {
			return EventDto.class;
		}

		@Override
		public void appendSerializedValue(StringBuffer buffer, EventDto logEvent) {
			long currentTimeMillis                                  = logEvent.getCurrentTimeMillis();
			String applicationName                                  = logEvent.getApplicationName();
			String threadInfo                                       = logEvent.getThreadInfo();
			InstrumentableEvent instrumentableEvent                = logEvent.getEvent();
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
	public void reset() {}

	@Override
	public void storeInstrumentableEvent(EventDto event) {
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
	public EventDto getNextEvent(int descriptor) {
		return null;
	}

	@Override
	public void closeDescriptor(int descriptor) {}
	
}
