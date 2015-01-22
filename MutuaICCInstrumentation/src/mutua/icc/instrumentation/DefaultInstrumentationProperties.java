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

public class DefaultInstrumentationProperties {

	public static IInstrumentableProperty<String>    DIP_MSG       = new IInstrumentableProperty<String>("msg", String.class);
	
	public static IInstrumentableProperty<Throwable> DIP_THROWABLE = new IInstrumentableProperty<Throwable>("stackTrace", Throwable.class) {
		@Override
		public void appendValueToLogLine(final StringBuffer logLine, Throwable value) {
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
			value.printStackTrace(pw);
			logLine.append('"');
		}
		
	};

}
