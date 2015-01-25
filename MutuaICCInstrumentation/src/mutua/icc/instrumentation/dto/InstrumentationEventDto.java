package mutua.icc.instrumentation.dto;

import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Hashtable;

import mutua.icc.instrumentation.InstrumentableEvent;
import mutua.icc.instrumentation.IInstrumentableProperty;

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
		return properties.get(property.getInstrumentationPropertyName());
	}

	
	/************************
	** LOG & SERIALIZATION **
	************************/
	
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd|HH:mm:ss.SSS|zzz");
	private static FieldPosition fp = new FieldPosition(0);
	
	private static String[][] stringEscapeSequences = {
		{"\n", "\\\\n"},
		{"\r", "\\\\r"},
		{"\t", "\\\\t"},
	};

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sdf.format(currentTimeMillis, sb, fp);
		sb.append("|").append(currentTimeMillis).append(", ").
		append(applicationName).append('-').append(threadInfo).append(": ").
		append(event.getName());
		IInstrumentableProperty[] eventProperties = event.getProperties();
		if (eventProperties.length > 0) {
			sb.append(" {");
			for (int i=0; i<eventProperties.length; i++) {
				IInstrumentableProperty property = eventProperties[i];
				sb.append(property.getInstrumentationPropertyName()).append(" = ");
				Object value = properties.get(property.getInstrumentationPropertyName());
				Class<?> type = property.getType();
				if (type == Integer.TYPE) {
					sb.append((Integer)value);
				} else if (type == Long.TYPE) {
					sb.append((Long)value);
				} else if (type == String.class) {
					String s = (String)value;
					for (int j=0; j<stringEscapeSequences.length; j++) {
						s = s.replaceAll(stringEscapeSequences[j][0], stringEscapeSequences[j][1]);
					}
					sb.append('"').append(s).append('"');
				} else {
					sb.append('{');
					property.appendSerializedValue(sb, value);
					sb.append('}');
				}
				if (i < (eventProperties.length-1)) {
					sb.append(", ");
				}
			}
			sb.append("}");
		}
		return sb.toString();
	}

}
