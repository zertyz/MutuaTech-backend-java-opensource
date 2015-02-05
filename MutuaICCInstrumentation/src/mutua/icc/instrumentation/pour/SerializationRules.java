package mutua.icc.instrumentation.pour;

import java.text.FieldPosition;
import java.text.SimpleDateFormat;

import mutua.icc.instrumentation.IInstrumentableProperty;
import mutua.icc.instrumentation.InstrumentableEvent;
import mutua.icc.instrumentation.dto.InstrumentationEventDto;
import mutua.serialization.ISerializationRule;
import mutua.serialization.SerializationRepository;

/** <pre>
 * SerializationRules.java
 * =======================
 * (created by luiz, Feb 4, 2015)
 *
 * Implements the serialization rules for the Instrumentation framework
 *
 * @see RelatedClass(es)
 * @version $Id$
 * @author luiz
 */

public class SerializationRules implements ISerializationRule<InstrumentationEventDto> {
	

	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd|HH:mm:ss.SSS|zzz");
	private static FieldPosition fp = new FieldPosition(0);
	private SerializationRepository serializer;

	
	public SerializationRules(SerializationRepository serializer) {
		this.serializer = serializer;
	}

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

}
