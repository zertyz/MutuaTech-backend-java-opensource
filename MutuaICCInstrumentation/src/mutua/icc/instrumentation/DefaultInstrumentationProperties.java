package mutua.icc.instrumentation;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;

/** <pre>
 * DefaultInstrumentationProperties.java
 * =====================================
 * (created by luiz, Jan 21, 2015)
 *
 * Defines common instrumentation properties that are available to participate on instrumentation events
 *
 * @see RelatedClass(es)
 * @version $Id$
 * @author luiz
 */

public enum DefaultInstrumentationProperties implements IInstrumentableProperty {

	
	DIP_MSG("msg", String.class),
	
	DIP_THROWABLE("stackTrace", Throwable.class) {
		@Override
		public void appendSerializedValue(final StringBuffer logLine, Object value) {
			Throwable t = (Throwable)value;
			PrintWriter pw = new PrintWriter(new Writer() {
				@Override
				public void write(char[] cbuf, int off, int len) throws IOException {
					logLine.append(new String(cbuf, off, len).replaceAll("\n", "\\\\n").replaceAll("\t", "\\\\t"));
				}
				@Override
				public void flush() throws IOException {}
				@Override
				public void close() throws IOException {}
				
			});
			logLine.append('"');
			t.printStackTrace(pw);
			logLine.append('"');
		}
	},
	
	
	;

	
	private String instrumentationPropertyName;
	private Class<?> instrumentationPropertyType;
	
	
	private DefaultInstrumentationProperties(String instrumentationPropertyName, Class<?> instrumentationPropertyType) {
		this.instrumentationPropertyName = instrumentationPropertyName;
		this.instrumentationPropertyType = instrumentationPropertyType;
	}

	
	// IInstrumentableProperty implementation
	/////////////////////////////////////////
	
	@Override
	public String getInstrumentationPropertyName() {
		return instrumentationPropertyName;
	}

	
	// ISerializationRule implementation
	////////////////////////////////////
	
	@Override
	public Class<?> getType() {
		return instrumentationPropertyType;
	}

	@Override
	public void appendSerializedValue(StringBuffer buffer, Object value) {
		throw new RuntimeException("Serialization Rule '" + this.getClass().getName() +
                                   "' didn't overrode 'appendSerializedValue' from " +
                                   "'ISerializationRule' for type '" + instrumentationPropertyType);
	}

}
