package mutua.icc.instrumentation;

import static mutua.icc.instrumentation.TestInstrumentationMethods.*;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

import mutua.icc.instrumentation.InstrumentableEvent.ELogSeverity;
import mutua.icc.instrumentation.handlers.IInstrumentationHandler;
import mutua.icc.instrumentation.handlers.InstrumentationHandlerLogPlainFile;

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
	
	public InstrumentationTests() throws UnsupportedEncodingException, FileNotFoundException {
		IInstrumentationHandler logInstrumentationHandler;
		//logInstrumentationHandler = new InstrumentationHandlerLogConsole  ("InstrumentationTests",                       InstrumentationHandlerLogPrintStream.DEBUG);
		logInstrumentationHandler = new InstrumentationHandlerLogPlainFile("InstrumentationTests", "/tmp/permanent.log", ELogSeverity.DEBUG);
		Instrumentation.configureDefaultValuesForNewInstances(logInstrumentationHandler, logInstrumentationHandler, logInstrumentationHandler);
	}
	
	@Test
	public void simpleTest() {
		startTestRequest("simpleTest");
		NOPROP_EVENT();
		ONEPROP_EVENT("Que se foda");
		TWOPROP_EVENT("nao me foda tanto", new TestType("de", "para"));
		Instrumentation.reportThrowable(new RuntimeException("Some thing happened"), "This is a test exception created on purpose");
		finishTestRequest();
	}

	@Test
	public void unfinishedRequestProcessingTest() {
		startTestRequest("unfinishedRequestProcessingTest");
		startTestRequest("unfinishedRequestProcessingTest2");
		finishTestRequest();
	}

	@Test
	public void unfinishedRequestProcessingBeforeShutdownTest() {
		startTestRequest("unfinishedRequestProcessingBeforeShutdownTest");
	}
	
	@Test
	public void uncaughtExceptionTest() {
		new Thread() {
			@Override
			public void run() {
				startTestRequest("uncaughtExceptionTest");
				throw new RuntimeException("there is no explicit try/catch for this one. It should be caught by the instrumentation facility though");
			}
			
		}.start();
	}
	
//	@Test
//	public void customPropagableInstrumentationEventClientTest() throws IndirectMethodNotFoundException {
//		log.addInstrumentationPropagableEventsClient(new InstrumentationPropagableEventsClient<EInstrumentationPropagableEvents>() {
//			@InstrumentationPropagableEvent(EInstrumentationPropagableEvents.INTERNAL_FRAMEWORK_INSTRUMENTATION_EVENT)
//			public void handleInternalFrameworkInstrumentationEventNotification(InstrumentationEventDto event) {
//				InstrumentableEvent instrumentableEvent = event.getInstrumentableEvent();
//				if (instrumentableEvent == log.REQUEST_START_EVENT) {
//					System.out.println("PROFILE: request start");
//				} else if (instrumentableEvent == log.REQUEST_FINISH_EVENT) {
//					System.out.println("PROFILE: request finish");
//				} else {
//					System.out.println("PROFILE: unimportant internal event");
//				}
//			}
//		});
//		log.reportRequestStart("customPropagableInstrumentationEventClientTest");
//		log.reportEvent(ONEPROP_EVENT, DAY_OF_WEEK, "QSF");
//		log.reportRequestFinish();
//	}

//	@Test
//	public void oneEventForEachLogInstance() {
//		Instrumentation<InstrumentationTestRequestProperty, String> log1 = new Instrumentation<InstrumentationTestRequestProperty, String>(
//				"LogInstance1", new InstrumentationTestRequestProperty(), EInstrumentationDataPours.CONSOLE, null, InstrumentationEvents.values());
//		Instrumentation<InstrumentationTestRequestProperty, String> log2 = new Instrumentation<InstrumentationTestRequestProperty, String>(
//				"LogInstance2", new InstrumentationTestRequestProperty(), EInstrumentationDataPours.CONSOLE, null, InstrumentationEvents.values());
//		Instrumentation<InstrumentationTestRequestProperty, String> log3 = new Instrumentation<InstrumentationTestRequestProperty, String>(
//				"LogInstance3", new InstrumentationTestRequestProperty(), EInstrumentationDataPours.CONSOLE, "differentKey", InstrumentationEvents.values());
//		log1.reportDebug("This should go only to log1");
//		log2.reportDebug("This should go only to log2");
//		log3.reportDebug("This should go only to log3");
//	}
}
