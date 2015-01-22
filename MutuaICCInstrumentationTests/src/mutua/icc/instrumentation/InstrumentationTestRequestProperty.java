package mutua.icc.instrumentation;

/** <pre>
 * InstrumentationRequestProperty.java
 * ===================================
 * (created by luiz, Jan 21, 2015)
 *
 * Class that can log requests on the instrumentation framework
 *
 * @see RelatedClass(es)
 * @version $Id$
 * @author luiz
 */

public class InstrumentationTestRequestProperty extends IInstrumentableProperty<String> {

	public InstrumentationTestRequestProperty(String name) {
		super(name, String.class);
	}

	@Override
	public void appendValueToLogLine(StringBuffer logLine, String value) {
		logLine.append(value);
	}

}
