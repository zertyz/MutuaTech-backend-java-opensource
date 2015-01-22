package mutua.icc.instrumentation;

import static mutua.icc.instrumentation.InstrumentationEvents.*;
import static mutua.icc.instrumentation.InstrumentationProperties.*;

import org.junit.Test;

/** <pre>
 * InstrumentationTests.java
 * =========================
 * (created by luiz, Jan 21, 2015)
 *
 * Test the instrumentation facility
 *
 * @see RelatedClass(es)
 * @version $Id$
 * @author luiz
 */

public class InstrumentationTests {
	
	private static Instrumentation<InstrumentationTestRequestProperty, String> log = new Instrumentation<InstrumentationTestRequestProperty, String>("InstrumentationTests", new InstrumentationTestRequestProperty("testProcedure"));
	
	@Test
	public void simpleTest() {
		log.reportRequestStart("simpleTest");
		log.reportEvent(NOPROP_EVENT);
		log.reportEvent(ONEPROP_EVENT, DAY_OF_WEEK, "Que se foda");
		log.reportEvent(TWOPROP_EVENT, DAY_OF_WEEK, "nao me foda tanto", MAIL, new TestType("de", "para"));
		log.reportThrowable(new RuntimeException("Some thing happened"), "This is a test exception created on purpose");
		log.reportRequestFinish();
	}

	@Test
	public void unfinishedRequestProcessingTest() {
		log.reportRequestStart("unfinishedRequestProcessingTest");
		log.reportRequestStart("unfinishedRequestProcessingTest2");
		log.reportRequestFinish();
	}

	@Test
	public void unfinishedRequestProcessingBeforeShutdownTest() {
		log.reportRequestStart("unfinishedRequestProcessingBeforeShutdownTest");
	}
	
	@Test
	public void uncoughtExceptionTest() {
		new Thread() {
			@Override
			public void run() {
				log.reportRequestStart("uncoughtExceptionTest");
				throw new RuntimeException("there is no try/catch for this one");
			}
			
		}.start();
	}

}
