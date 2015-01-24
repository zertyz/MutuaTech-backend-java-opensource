package mutua.icc.instrumentation;

/** <pre>
 * InstrumentationTestRequestProperty.java
 * =======================================
 * (created by luiz, Jan 21, 2015)
 *
 * Class that can log requests on the instrumentation framework -- where a request is something
 * like an http request or a console main execution. On this case, the 'TestRequest' is a
 * representation of a JUnit run.
 *
 * @see RelatedClass(es)
 * @version $Id$
 * @author luiz
 */

public class InstrumentationTestRequestProperty implements IInstrumentableProperty {

	private String testName;
	
	public InstrumentationTestRequestProperty(String testName) {
		this.testName = testName;
	}

	@Override
	public String getInstrumentationPropertyName() {
		return "testName";
	}

	@Override
	public Class<?> getType() {
		return testName.getClass();
	}

	@Override
	public void appendSerializedValue(StringBuffer buffer, Object value) {
		throw new RuntimeException("This serialization rule should not have been called since 'type' is a string");
	}

}
