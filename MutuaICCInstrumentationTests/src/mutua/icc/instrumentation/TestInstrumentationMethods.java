package mutua.icc.instrumentation;

import mutua.icc.instrumentation.InstrumentableEvent.ELogSeverity;

/** <pre>
 * TestInstrumentationMethods.java
 * ===============================
 * (created by luiz, Apr 22, 2016)
 *
 * Experimentational class for the new Instrumentation Architecture
 *
 * @version $Id$
 * @author luiz
*/

public class TestInstrumentationMethods {
	
	// log, report
	
	// dayOfWeekProperty   = new InstrumentationProperty("dayOfWeek", ABSOLUTE_VALUE)
	// mail                = new InstrumentationProperty("mail",      DELTA_VALUE)	// ou increment... melhor
	
	// noPropEvent                    = new InstrumentationEvent("NOPROP", LOG.DEBUG)
	// onePropEvent(dayOfWeek)        = new InstrumentationEvent("ONEPROP", LOG.INFO,     dayOfWeekProperty)
	// twoPropEvent(dayOfWeek, mail)  = new InstrumentationEvent("TWOPROP", LOG.CRITICAL, dayOfWeekProperty, mailProperty)
	
	// TODO continuar a reduzir o instrumentation tal qual sugerido aqui:
	// 1) Instrumentation conterá apenas os logAndCompute, logAndProfile, profileAndCompute, logProfileAndCompute, justLog, justCompute, justProfile methods
	// 2) Os eventos e propriedades passam a ser objetos criáveis com new (ao invés de via enum)
	// 3) Instrumentation recebe o ConfigurationPattern da mutua para sabermos que log e que report usar -- e que profiler
	// 4) Acaba-se com o startRequest e finishRequest? de repente...
//	// material do editor que nos trouxe até aqui:
//	instrumentation.registerEvents(IInstrumentableEvent events)
//
//	1) Cada classe que declara as InstrumentableEvents deve chamar Instrumentation.registerEvent() -- considerar não declará-los mais através de uma Enum, mas sim de public static final .. = new InstrumentableEvent(name, handlerclass1, handlerclass2, ...) -- o mesmo vale para as properties
//	2) em Instrumentation, um array desses events é mantido e recalculado quando se configura o instrumentation -- configurar o instrumentation gera uma nova versão de 'instrumentation'
//	3) Pq não podemos chamar EVENT1.with(prop1, val1, prop2, val2) -- ou EVENT1_INSTRUMENTATION(val1, val2), como um método. Isso implicaria que cada instrumentableEvent tivesse um método implementado, e documentado, para receber as alegadas properties, do tipo correto. Esta parece ser a forma mais interessante de fazer.
//	4) Se 3, eu preciso do instrumentation, instrumentable events, instrumentable properties... ? Ou apenas dos InstrumentationHandlers?
//
//	ChatInstrumentation {
//
//	// EVENT1_INSTRUMENTATION (nome: event1, handlers: {log, report}, properties: prop1, prop2)
//	// prop1 (nome: prop1, class: String, type: absolute)
//	// prop2 (nome: prop2, class: int, type: inc)
//
//	public static void EVENT1_INSTRUMENTATION(String val1, int val2) {
//		event = Instrumentation.getInstrumentationEventDto("event1", prop1, val1, prop2, val2);
//		log.onEvent(event)
//		report.onEvent(event)
//	}
//
//	}
	
	// 'InstrumentableEvent's
	/////////////////////////

	// ... for the log handler tests
	public static final InstrumentableEvent debugEvent    = new InstrumentableEvent("DEBUG_EVENT_NAME",    ELogSeverity.DEBUG);
	public static final InstrumentableEvent cryptEvent    = new InstrumentableEvent("CRYPT_EVENT_NAME",    ELogSeverity.CRYPT);
	public static final InstrumentableEvent infoEvent     = new InstrumentableEvent("INFO_EVENT_NAME",     ELogSeverity.INFO);
	public static final InstrumentableEvent criticalEvent = new InstrumentableEvent("CRITICAL_EVENT_NAME", ELogSeverity.CRITICAL);
	public static final InstrumentableEvent errorEvent    = new InstrumentableEvent("ERROR_EVENT_NAME",    ELogSeverity.ERROR);

	// ... for the instrumentation tests
	public static final InstrumentableEvent noPropEvent  = new InstrumentableEvent("NOPROP",  ELogSeverity.DEBUG); 
	public static final InstrumentableEvent onePropEvent = new InstrumentableEvent("ONEPROP", ELogSeverity.INFO); 
	public static final InstrumentableEvent twoPropEvent = new InstrumentableEvent("TWOPROP", ELogSeverity.CRITICAL);
	
	// 'InstrumentableProperty'ies
	//////////////////////////////
	
	// ... for the log handler tests
	public static final InstrumentableProperty messageProperty = new InstrumentableProperty("msg", String.class);

	// ... for the instrumentation tests
	public static final InstrumentableProperty requestProperty   = new InstrumentableProperty("testName", String.class);
	public static final InstrumentableProperty dayOfWeekProperty = new InstrumentableProperty("prop1",    String.class);
	public static final InstrumentableProperty mailProperty      = new InstrumentableProperty("prop2",    TestType.class);

	
	public static void startTestRequest(String testName) {
		Instrumentation.startRequest(requestProperty, testName);
	}
	
	public static void finishTestRequest() {
		Instrumentation.finishRequest();
	}
	
	public static void NOPROP_EVENT() {
		Instrumentation.logProfileAndCompute(noPropEvent);
	}
	
	public static void ONEPROP_EVENT(String dayOfWeek) {
		Instrumentation.logProfileAndCompute(onePropEvent, dayOfWeekProperty, dayOfWeek);
	}
	
	public static void TWOPROP_EVENT(String dayOfWeek, TestType mail) {
		Instrumentation.logAndProfile(twoPropEvent, dayOfWeekProperty, dayOfWeek, mailProperty, mail);
	}

}
