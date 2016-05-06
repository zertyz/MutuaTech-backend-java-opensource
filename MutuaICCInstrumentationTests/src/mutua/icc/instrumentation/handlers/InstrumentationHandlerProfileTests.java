package mutua.icc.instrumentation.handlers;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

import static mutua.icc.instrumentation.DefaultInstrumentationEvents.*;
import static mutua.icc.instrumentation.TestInstrumentationMethods.*;
import mutua.icc.instrumentation.InstrumentableProperty;
import mutua.icc.instrumentation.Instrumentation;
import mutua.icc.instrumentation.TestType;
import mutua.icc.instrumentation.InstrumentableEvent.ELogSeverity;

import org.junit.Test;

/** <pre>
 * InstrumentationHandlerProfileTests.java
 * =======================================
 * (created by luiz, May 3, 2016)
 *
 * Tests the custom profiling for the Instrumentation Handler architecture
 *
 * @see RelatedClass(es)
 * @version $Id$
 * @author luiz
 */

public class InstrumentationHandlerProfileTests {
	
	public InstrumentationHandlerProfileTests() throws UnsupportedEncodingException, FileNotFoundException {
		IInstrumentationHandler logInstrumentationHandler;
		IInstrumentationHandler profileInstrumentationHandler;
		logInstrumentationHandler     = new InstrumentationHandlerLogConsole  ("InstrumentationHandlerProfileTests", ELogSeverity.DEBUG);
		profileInstrumentationHandler = new InstrumentationHandlerProfilerLog(logInstrumentationHandler,
			REQUEST_START_EVENT, onePropEvent,         new InstrumentableProperty("oneProp enqueueing", Long.class),
			onePropEvent,        twoPropEvent,         new InstrumentableProperty("oneProp processing", Long.class),
			twoPropEvent,        REQUEST_FINISH_EVENT, new InstrumentableProperty("twoProp enqueueing", Long.class));
		
		Instrumentation.configureDefaultValuesForNewInstances(logInstrumentationHandler, logInstrumentationHandler, profileInstrumentationHandler);
	}

	@Test
	public void testSimpleProfiling() throws InterruptedException {
		startTestRequest("profiling attempt request");
		Thread.sleep(10);
		ONEPROP_EVENT("just enqueued");
		Thread.sleep(20);
		TWOPROP_EVENT("just processed", new TestType("from", "to"));
		Thread.sleep(30);
		finishTestRequest();
	}

}
