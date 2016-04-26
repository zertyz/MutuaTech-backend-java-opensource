package mutua.icc.instrumentation.handlers;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;

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
 * @version $Id$
 * @author luiz
*/

public class InstrumentationHandlerLogPrintStream implements IInstrumentationHandler {
	
	// flags that may be used when creating an 'IInstrumentableEvent', which will be returned by 'IInstrumentableEvent.getInstrumentableEventFlags()'
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** {@link IInstrumentableEvent}s flagged with this property denotes log messages that present details which are only useful for developers to improve the software */
	public static final int DEBUG    = 1;
	/** {@link IInstrumentableEvent}s flagged with this property denotes log messages that are similar to {@link #DEBUG}, but which information could be exploited by
	 *  reverse engineersto overcome the license and/or author rights protections and, therefore,should be either presented criptically or not presented at all, on production */
	public static final int CRYPT    = 2;
	/** {@link IInstrumentableEvent}s flagged with this property denotes log data that present additional information that helps the reproduction of the execution scenarios -- either in normal or error circumstances */
	public static final int INFO     = 4;
	/** {@link IInstrumentableEvent}s flagged with this property denotes log data that present minimal information needed to reproduce any #ERROR scenarios */
	public static final int CRITICAL = 8;
	/** {@link IInstrumentableEvent}s flagged with this property denotes log data corresponding to any thrown 'Errors', 'Exceptions' and 'Throwable' */
	public static final int ERROR    = 16;

	protected       PrintStream  out;
	protected final StringBuffer logLine;
	private   final String       applicationName;
	private   final int          minimumLogLevel;

	public InstrumentationHandlerLogPrintStream(String applicationName, PrintStream out, int minimumLogLevel) {
		this.applicationName = applicationName;
		this.out             = out;
		this.minimumLogLevel = minimumLogLevel;
		logLine = new StringBuffer(128);
	}
	
	public InstrumentationHandlerLogPrintStream(String applicationName, OutputStream out, int minimumLogLevel) {
		this(applicationName, getPrintStream(out), minimumLogLevel);
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
		if (instrumentationEvent.instrumentableEvent.eventFlags < minimumLogLevel) {
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
						if ((type == Integer.TYPE) || (type == Long.TYPE) || (type == Double.TYPE) || (type == Float.TYPE) ||
						    (type == Short.TYPE)   || (type == Byte.TYPE) || (type == Boolean.TYPE)) {
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

}