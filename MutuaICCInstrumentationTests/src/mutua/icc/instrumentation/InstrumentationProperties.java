package mutua.icc.instrumentation;

/** <pre>
 * InstrumentationProperties.java
 * ==============================
 * (created by luiz, Jan 21, 2015)
 *
 * Defines the available properties to participate on instrumentation events
 *
 * @see RelatedClass(es)
 * @version $Id$
 * @author luiz
 */

public class InstrumentationProperties {
	
	public static IInstrumentableProperty<String> DAY_OF_WEEK = new IInstrumentableProperty<String>("dayOfWeek", String.class);
	
	public static IInstrumentableProperty<TestType> MAIL = new IInstrumentableProperty<TestType>("mail", TestType.class) {

		@Override
		public void appendValueToLogLine(StringBuffer logLine, TestType value) {
			logLine.append("from='").append(value.from).append("',").
			append("to='").append(value.to).append("'");
		}
		
	};

}
