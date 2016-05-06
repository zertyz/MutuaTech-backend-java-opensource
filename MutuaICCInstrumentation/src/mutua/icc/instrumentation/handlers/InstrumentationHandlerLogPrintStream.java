package mutua.icc.instrumentation.handlers;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;

import mutua.icc.instrumentation.InstrumentableEvent.ELogSeverity;
import mutua.icc.instrumentation.InstrumentableProperty;
import mutua.icc.instrumentation.dto.InstrumentationEventDto;
import mutua.serialization.SerializationRepository;

/** <pre>
 * InstrumentationHandlerLogPrintStream.java
 * =========================================
 * (created by luiz, Apr 19, 2016)
 *
 * The main log (textual) event serialization class, outputting to a PrintStream.
 *
 * @see InstrumentationHandlerLogConsole
 * @see InstrumentationHandlerLogPlainFile
 * @see InstrumentationHandlerLogRotatoryFile
 * @version $Id$
 * @author luiz
*/

public class InstrumentationHandlerLogPrintStream implements IInstrumentationHandler {
	
	protected       PrintStream  out;
	protected final StringBuffer logLine;
	private   final String       applicationName;
	private   final int          minimumLogSeverityOrdinal;

	public InstrumentationHandlerLogPrintStream(String applicationName, PrintStream out, ELogSeverity minimumLogSeverity) {
		this.applicationName           = applicationName;
		this.out                       = out;
		this.minimumLogSeverityOrdinal = minimumLogSeverity.ordinal();
		logLine                        = new StringBuffer(128);
	}
	
	public InstrumentationHandlerLogPrintStream(String applicationName, OutputStream out, ELogSeverity minimumLogSeverity) {
		this(applicationName, getPrintStream(out), minimumLogSeverity);
	}
	
	protected static PrintStream getPrintStream(OutputStream out) {
		try {
			return new PrintStream(out, false, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("For some reason, this happened: ", e);
		}
	}
	
	@Override
	public void onRequestStart(InstrumentationEventDto requestStartInstrumentationEvent) {
		onInstrumentationEvent(requestStartInstrumentationEvent);
	}

	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd|HH:mm:ss.SSS|zzz");
	private static final FieldPosition    fp  = new FieldPosition(0);
	@Override
	public void onInstrumentationEvent(InstrumentationEventDto instrumentationEvent) {
		
		// only log events for which the InstrumentableEvent is flagged with a level equal or greater than this instance's chosen 'minimumLogLevel'
		if (instrumentationEvent.instrumentableEvent.logSeverityOrdinal < minimumLogSeverityOrdinal) {
			return;
		}
		
		String l;
		synchronized (logLine) {
			try {
				logLine.setLength(0);
				
				// serialization algorithm
				sdf.format(instrumentationEvent.currentTimeMillis, logLine, fp);
				logLine.append('|').append(instrumentationEvent.currentTimeMillis).append(", ").
				append(applicationName).append('-').append(instrumentationEvent.threadInfo).append(": ").
				append(instrumentationEvent.instrumentableEvent.eventName);
				if (instrumentationEvent.propertiesAndValues.length > 0) {
					logLine.append(" {");
					for (int i=0; i<instrumentationEvent.propertiesAndValues.length; i+=2) {

						if (i > 0) {
							logLine.append(", ");
						}
						
						InstrumentableProperty instrumentableProperty = (InstrumentableProperty)instrumentationEvent.propertiesAndValues[i];
						logLine.append(instrumentableProperty.propertyName).append(" = ");
						Object logEventPropertyValue = instrumentationEvent.propertiesAndValues[i+1];
						
						Class<?> type = instrumentableProperty.propertyType;
						if ((type == Integer.class) || (type == Long.class) || (type == Double.class) || (type == Float.class) ||
						    (type == Short.class)   || (type == Byte.class) || (type == Boolean.class)) {
							logLine.append(logEventPropertyValue);
						} else if (type == String.class) {
							SerializationRepository.serialize(logLine.append('"'), (String)logEventPropertyValue).append('"');
						} else {
							Method propertySerializationMethod = instrumentableProperty.serializationMethod;
							SerializationRepository.invokeSerializationMethod(propertySerializationMethod, logLine.append('{'), logEventPropertyValue);
							logLine.append('}');
						}
					}
					logLine.append('}');
				}

			} catch (Throwable t) {
				t.printStackTrace(out);
				logLine.append("### Exception serializing 'instrumentationEvent' named '"+instrumentationEvent.instrumentableEvent.eventName+"': ").append(t.toString());
			}

			l = logLine.toString();
			out.println(l);
		}
	}
	
	@Override
	public void onRequestFinish(InstrumentationEventDto requestFinishInstrumentationEvent) {
		onInstrumentationEvent(requestFinishInstrumentationEvent);
	}

	@Override
	public void close() {
		if (out != null) {
			out.close();
			out = null;
		}
	}
}