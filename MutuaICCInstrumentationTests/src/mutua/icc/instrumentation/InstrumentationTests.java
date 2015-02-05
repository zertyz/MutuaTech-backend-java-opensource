package mutua.icc.instrumentation;

import static mutua.icc.instrumentation.InstrumentationEvents.NOPROP_EVENT;
import static mutua.icc.instrumentation.InstrumentationEvents.ONEPROP_EVENT;
import static mutua.icc.instrumentation.InstrumentationEvents.TWOPROP_EVENT;
import static mutua.icc.instrumentation.InstrumentationProperties.DAY_OF_WEEK;
import static mutua.icc.instrumentation.InstrumentationProperties.MAIL;
import mutua.events.annotations.EventListener;
import mutua.icc.instrumentation.Instrumentation.EInstrumentationPropagableEvents;
import mutua.icc.instrumentation.dto.InstrumentationEventDto;
import mutua.icc.instrumentation.eventclients.InstrumentationPropagableEventsClient;
import mutua.icc.instrumentation.pour.PourFactory.EInstrumentationDataPours;
import mutua.imi.IndirectMethodNotFoundException;

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
	
	private static Instrumentation<InstrumentationTestRequestProperty, String> log = new Instrumentation<InstrumentationTestRequestProperty, String>(
			"InstrumentationTests", new InstrumentationTestRequestProperty(), EInstrumentationDataPours.CONSOLE, null, InstrumentationEvents.values());
	
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
	
	@Test
	public void customPropagableInstrumentationEventClientTest() throws IndirectMethodNotFoundException {
		log.addInstrumentationPropagableEventsClient(new InstrumentationPropagableEventsClient<EInstrumentationPropagableEvents>() {
			@EventListener({"INTERNAL_FRAMEWORK_INSTRUMENTATION_EVENT"})
			public void handleInternalFrameworkInstrumentationEventNotification(InstrumentationEventDto event) {
				InstrumentableEvent instrumentableEvent = event.getEvent();
				if (instrumentableEvent == log.REQUEST_START_EVENT) {
					System.out.println("PROFILE: request start");
				} else if (instrumentableEvent == log.REQUEST_FINISH_EVENT) {
					System.out.println("PROFILE: request finish");
				} else {
					System.out.println("PROFILE: unimportant internal event");
				}
			}
		});
		log.reportRequestStart("customPropagableInstrumentationEventClientTest");
		log.reportEvent(ONEPROP_EVENT, DAY_OF_WEEK, "QSF");
		log.reportRequestFinish();
	}

	@Test
	public void oneEventForEachLogInstance() {
		Instrumentation<InstrumentationTestRequestProperty, String> log1 = new Instrumentation<InstrumentationTestRequestProperty, String>(
				"LogInstance1", new InstrumentationTestRequestProperty(), EInstrumentationDataPours.CONSOLE, null, InstrumentationEvents.values());
		Instrumentation<InstrumentationTestRequestProperty, String> log2 = new Instrumentation<InstrumentationTestRequestProperty, String>(
				"LogInstance2", new InstrumentationTestRequestProperty(), EInstrumentationDataPours.CONSOLE, null, InstrumentationEvents.values());
		Instrumentation<InstrumentationTestRequestProperty, String> log3 = new Instrumentation<InstrumentationTestRequestProperty, String>(
				"LogInstance3", new InstrumentationTestRequestProperty(), EInstrumentationDataPours.CONSOLE, "differentKey", InstrumentationEvents.values());
		log1.reportDebug("This should go only to log1");
		log2.reportDebug("This should go only to log2");
		log3.reportDebug("This should go only to log3");
	}
}
