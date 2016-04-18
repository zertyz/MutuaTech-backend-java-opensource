package mutua.icc.instrumentation.dto;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Hashtable;
import mutua.icc.instrumentation.IInstrumentableProperty;
import mutua.icc.instrumentation.InstrumentableEvent;
import mutua.serialization.SerializationRepository;
import mutua.serialization.SerializationRepository.EfficientTextualSerializationMethod;

/** <pre>
 * EventDto.java
 * =============
 * (created by luiz, Jan 21, 2015)
 *
 * Represents an instrumentable event
 *
 * @see RelatedClass(es)
 * @version $Id$
 * @author luiz
 */

// TODO check properties against the ones registered on IInstrumentableEvent

public class InstrumentationEventDto {
	
	private long currentTimeMillis;
	private String applicationName;
	private String threadInfo;
	private InstrumentableEvent event;
	private Hashtable<String, Object> properties;
	
	public InstrumentationEventDto(long currentTimeMillis, String applicationName, Thread thread, InstrumentableEvent event) {
		this.currentTimeMillis = currentTimeMillis;
		this.applicationName   = applicationName;
		this.threadInfo        = thread.toString();;
		this.event             = event;
	}

	public InstrumentationEventDto(long currentTimeMillis, String applicationName, Thread thread, InstrumentableEvent event,
		                           IInstrumentableProperty property, Object value) {
		this(currentTimeMillis, applicationName, thread, event);
		properties = new Hashtable<String, Object>();
		if (value != null) {
			properties.put(property.getInstrumentationPropertyName(), value);
		}
	}

	public InstrumentationEventDto(long currentTimeMillis, String applicationName, Thread thread, InstrumentableEvent event,
		                           IInstrumentableProperty property1, Object value1,
		                           IInstrumentableProperty property2, Object value2) {
		this(currentTimeMillis, applicationName, thread, event, property1, value1);
		properties.put(property2.getInstrumentationPropertyName(), value2);
	}

	public InstrumentationEventDto(long currentTimeMillis, String applicationName, Thread thread, InstrumentableEvent event,
		                           IInstrumentableProperty property1, Object value1,
		                           IInstrumentableProperty property2, Object value2,
		                           IInstrumentableProperty property3, Object value3) {
		this(currentTimeMillis, applicationName, thread, event, property1, value1, property2, value2);
		properties.put(property3.getInstrumentationPropertyName(), value3);
	}

	public long getCurrentTimeMillis() {
		return currentTimeMillis;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public String getThreadInfo() {
		return threadInfo;
	}

	public InstrumentableEvent getEvent() {
		return event;
	}
	
	public IInstrumentableProperty[] getLogEventProperties() {
		return properties.keySet().toArray(new IInstrumentableProperty[properties.size()]);
	}

	public Object getValue(IInstrumentableProperty property) {
		try {
			return properties.get(property.getInstrumentationPropertyName());
		} catch (NullPointerException e) {
			throw new RuntimeException("Missing property value for property '"+property.getInstrumentationPropertyName()+"' on an instrumentation log line for event '"+getEvent().getName()+"'");
		}
	}
	
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd|HH:mm:ss.SSS|zzz");
	private static final FieldPosition    fp  = new FieldPosition(0);
	@EfficientTextualSerializationMethod
	public void toString(StringBuffer buffer) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		IInstrumentableProperty[] instrumentableEventProperties = event.getProperties();
		sdf.format(currentTimeMillis, buffer, fp);
		buffer.append("|").append(currentTimeMillis).append(", ").
		append(applicationName).append('-').append(threadInfo).append(": ").
		append(event.getName());
		if (instrumentableEventProperties.length > 0) {
			buffer.append(" {");
			for (int i=0; i<instrumentableEventProperties.length; i++) {

				if (i > 0) {
					buffer.append(", ");
				}
				
				IInstrumentableProperty instrumentableProperty = instrumentableEventProperties[i];
				buffer.append(instrumentableProperty.getInstrumentationPropertyName()).append(" = ");
				Object logEventPropertyValue = getValue(instrumentableProperty);
				
				Class<?> type = instrumentableProperty.getInstrumentationPropertyType();
				if ((type == Integer.TYPE) || (type == Long.TYPE) || (type == Double.TYPE) || (type == Float.TYPE) ||
				    (type == Short.TYPE)   || (type == Byte.TYPE) || (type == Boolean.TYPE)) {
					buffer.append(logEventPropertyValue);
				} else if (type == String.class) {
					SerializationRepository.serialize(buffer.append('"'), (String)logEventPropertyValue).append('"');
				} else {
					Method propertySerializationMethod = instrumentableProperty.getTextualSerializationMethod();
					SerializationRepository.invokeSerializationMethod(propertySerializationMethod, buffer.append('{'), logEventPropertyValue);
					buffer.append('}');
				}
			}
			buffer.append("}");
		}
	}

	
}