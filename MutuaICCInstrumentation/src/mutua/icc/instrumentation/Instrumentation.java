package mutua.icc.instrumentation;

import static mutua.icc.instrumentation.DefaultInstrumentationEvents.*;
import static mutua.icc.instrumentation.DefaultInstrumentationProperties.*;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.WeakHashMap;

import adapters.PostgreSQLAdapter;
import mutua.events.DirectEventLink;
import mutua.events.EventServer;
import mutua.events.IEventLink;
import mutua.events.PostgreSQLQueueEventLink;
import mutua.events.postgresql.QueuesPostgreSQLAdapter;
import mutua.icc.instrumentation.dto.InstrumentationEventDto;
import mutua.icc.instrumentation.eventclients.InstrumentationPropagableEventsClient;
import mutua.icc.instrumentation.handlers.IInstrumentationHandler;
import mutua.icc.instrumentation.handlers.InstrumentationHandlerLogPrintStream;
import mutua.imi.IndirectMethodNotFoundException;
import mutua.tests.MutuaEventsAdditionalEventLinksTestsConfiguration;


/** <pre>
 * Instrumentation.java
 * ====================
 * (created by luiz, Jan 21, 2015)
 *
 * Main class used by applications to report 
 *
 * @see RelatedClass(es)
 * @version $Id$
 * @author luiz
 */

public class Instrumentation<REQUEST_PROPERTY_TYPE extends InstrumentableProperty, REQUEST_TYPE> {

	// data structures
	//////////////////
	
	private String                          APPLICATION_NAME;
	private REQUEST_PROPERTY_TYPE           requestProperty;
	
	// 'IInstrumentationHandler's
	private static IInstrumentationHandler  LOG_INSTRUMENTATION_HANDLER;
	private static IInstrumentationHandler  REPORT_INSTRUMENTATION_HANDLER;
	private static IInstrumentationHandler  PROFILE_INSTRUMENTATION_HANDLER;
	
	public InstrumentableEvent UNFINISHED_REQUEST_EVENT;
	public InstrumentableEvent REQUEST_START_EVENT;
	public InstrumentableEvent REQUEST_FINISH_EVENT;

	private static final IInstrumentationHandler log    = null; //new InstrumentationHandlerLogConsole();
	private static final IInstrumentationHandler report = null;
	
	/**************************
	** CONFIGURATION METHODS **
	**************************/
	
	/** method to be called when attempting to configure the default behavior of 'Instrumentation' module.
	 *  Receives the list of {@link IInstrumentationHandler}s available to process instrumentation events:<pre>
	 *  @param logInstrumentationHandler
	 *  @param reportInstrumentationHandler
	 *  @param profileInstrumentationHandler */
	public static void configureDefaultValuesForNewInstances(
		IInstrumentationHandler logInstrumentationHandler,
		IInstrumentationHandler reportInstrumentationHandler,
		IInstrumentationHandler profileInstrumentationHandler) {
		
		LOG_INSTRUMENTATION_HANDLER     = logInstrumentationHandler;
		REPORT_INSTRUMENTATION_HANDLER  = reportInstrumentationHandler;
		PROFILE_INSTRUMENTATION_HANDLER = profileInstrumentationHandler;
	}
	
	private static final InstrumentableEvent REQUEST_START_EVENT  = new InstrumentableEvent("REQUEST START", CRITICAL, ALL HANDLERS);
	private static final InstrumentableEvent REQUEST_FINISH_EVENT = new InstrumentableEvent("REQUEST FINISH", CRITICAL, ALL HANDLERS);

	public static void requestStart(InstrumentableProperty property1, Object property1Value) {
		InstrumentationEventDto requestStartInstrumentationEvent = new InstrumentationEventDto(System.currentTimeMillis(), Thread.currentThread(), REQUEST_START_EVENT, property1, property1Value);
		if (LOG_INSTRUMENTATION_HANDLER != null) {
			LOG_INSTRUMENTATION_HANDLER.onRequestStart(requestStartInstrumentationEvent);
		}
		if (REPORT_INSTRUMENTATION_HANDLER != null) {
			REPORT_INSTRUMENTATION_HANDLER.onRequestStart(requestStartInstrumentationEvent);
		}
		if (PROFILE_INSTRUMENTATION_HANDLER != null) {
			PROFILE_INSTRUMENTATION_HANDLER.onRequestStart(requestStartInstrumentationEvent);
		}
	}
	
	public static void requestFinish() {
		InstrumentationEventDto requestStartInstrumentationEvent = new InstrumentationEventDto(System.currentTimeMillis(), Thread.currentThread(), REQUEST_FINISH_EVENT);
		if (LOG_INSTRUMENTATION_HANDLER != null) {
			LOG_INSTRUMENTATION_HANDLER.onRequestStart(requestStartInstrumentationEvent);
		}
		if (REPORT_INSTRUMENTATION_HANDLER != null) {
			REPORT_INSTRUMENTATION_HANDLER.onRequestStart(requestStartInstrumentationEvent);
		}
		if (PROFILE_INSTRUMENTATION_HANDLER != null) {
			PROFILE_INSTRUMENTATION_HANDLER.onRequestStart(requestStartInstrumentationEvent);
		}
	}

	public static void justLog(InstrumentableEvent event) {
		LOG_INSTRUMENTATION_HANDLER.onInstrumentationEvent(new InstrumentationEventDto(System.currentTimeMillis(), Thread.currentThread(), event));
	}

	public static void justLog(InstrumentableEvent event, InstrumentableProperty property1, Object property1Value) {
		LOG_INSTRUMENTATION_HANDLER.onInstrumentationEvent(new InstrumentationEventDto(System.currentTimeMillis(), Thread.currentThread(), event, property1, property1Value));
	}

	public static void justLog(InstrumentableEvent event, InstrumentableProperty property1, Object property1Value, InstrumentableProperty property2, Object property2Value) {
		LOG_INSTRUMENTATION_HANDLER.onInstrumentationEvent(new InstrumentationEventDto(System.currentTimeMillis(), Thread.currentThread(), event, property1, property1Value, property2, property2Value));
	}

	public static void justLog(InstrumentableEvent event, InstrumentableProperty property1, Object property1Value, InstrumentableProperty property2, Object property2Value, InstrumentableProperty property3, Object property3Value) {
		LOG_INSTRUMENTATION_HANDLER.onInstrumentationEvent(new InstrumentationEventDto(System.currentTimeMillis(), Thread.currentThread(), event, property1, property1Value, property2, property2Value, property3, property3Value));
	}

	public static void justCompute(InstrumentableEvent event) {
		REPORT_INSTRUMENTATION_HANDLER.onInstrumentationEvent(new InstrumentationEventDto(System.currentTimeMillis(), Thread.currentThread(), event));
	}

	public static void justCompute(InstrumentableEvent event, InstrumentableProperty property1, Object property1Value) {
		REPORT_INSTRUMENTATION_HANDLER.onInstrumentationEvent(new InstrumentationEventDto(System.currentTimeMillis(), Thread.currentThread(), event, property1, property1Value));
	}

	public static void justCompute(InstrumentableEvent event, InstrumentableProperty property1, Object property1Value, InstrumentableProperty property2, Object property2Value) {
		REPORT_INSTRUMENTATION_HANDLER.onInstrumentationEvent(new InstrumentationEventDto(System.currentTimeMillis(), Thread.currentThread(), event, property1, property1Value, property2, property2Value));
	}

	public static void justCompute(InstrumentableEvent event, InstrumentableProperty property1, Object property1Value, InstrumentableProperty property2, Object property2Value, InstrumentableProperty property3, Object property3Value) {
		REPORT_INSTRUMENTATION_HANDLER.onInstrumentationEvent(new InstrumentationEventDto(System.currentTimeMillis(), Thread.currentThread(), event, property1, property1Value, property2, property2Value, property3, property3Value));
	}
	
	public static void justProfile(InstrumentableEvent event) {
		PROFILE_INSTRUMENTATION_HANDLER.onInstrumentationEvent(new InstrumentationEventDto(System.currentTimeMillis(), Thread.currentThread(), event));
	}

	public static void justProfile(InstrumentableEvent event, InstrumentableProperty property1, Object property1Value) {
		PROFILE_INSTRUMENTATION_HANDLER.onInstrumentationEvent(new InstrumentationEventDto(System.currentTimeMillis(), Thread.currentThread(), event, property1, property1Value));
	}

	public static void justProfile(InstrumentableEvent event, InstrumentableProperty property1, Object property1Value, InstrumentableProperty property2, Object property2Value) {
		PROFILE_INSTRUMENTATION_HANDLER.onInstrumentationEvent(new InstrumentationEventDto(System.currentTimeMillis(), Thread.currentThread(), event, property1, property1Value, property2, property2Value));
	}

	public static void justProfile(InstrumentableEvent event, InstrumentableProperty property1, Object property1Value, InstrumentableProperty property2, Object property2Value, InstrumentableProperty property3, Object property3Value) {
		PROFILE_INSTRUMENTATION_HANDLER.onInstrumentationEvent(new InstrumentationEventDto(System.currentTimeMillis(), Thread.currentThread(), event, property1, property1Value, property2, property2Value, property3, property3Value));
	}
	
	public static void logAndCompute(InstrumentableEvent event) {
		InstrumentationEventDto instrumentationEvent = new InstrumentationEventDto(System.currentTimeMillis(), Thread.currentThread(), event);
		LOG_INSTRUMENTATION_HANDLER.onInstrumentationEvent(instrumentationEvent);
		REPORT_INSTRUMENTATION_HANDLER.onInstrumentationEvent(instrumentationEvent);
	}

	public static void logAndCompute(InstrumentableEvent event, InstrumentableProperty property1, Object property1Value) {
		InstrumentationEventDto instrumentationEvent = new InstrumentationEventDto(System.currentTimeMillis(), Thread.currentThread(), event, property1, property1Value);
		LOG_INSTRUMENTATION_HANDLER.onInstrumentationEvent(instrumentationEvent);
		REPORT_INSTRUMENTATION_HANDLER.onInstrumentationEvent(instrumentationEvent);
	}

	public static void logAndCompute(InstrumentableEvent event, InstrumentableProperty property1, Object property1Value, InstrumentableProperty property2, Object property2Value) {
		InstrumentationEventDto instrumentationEvent = new InstrumentationEventDto(System.currentTimeMillis(), Thread.currentThread(), event, property1, property1Value, property2, property2Value);
		LOG_INSTRUMENTATION_HANDLER.onInstrumentationEvent(instrumentationEvent);
		REPORT_INSTRUMENTATION_HANDLER.onInstrumentationEvent(instrumentationEvent);
	}

	public static void logAndCompute(InstrumentableEvent event, InstrumentableProperty property1, Object property1Value, InstrumentableProperty property2, Object property2Value, InstrumentableProperty property3, Object property3Value) {
		InstrumentationEventDto instrumentationEvent = new InstrumentationEventDto(System.currentTimeMillis(), Thread.currentThread(), event, property1, property1Value, property2, property2Value, property3, property3Value);
		LOG_INSTRUMENTATION_HANDLER.onInstrumentationEvent(instrumentationEvent);
		REPORT_INSTRUMENTATION_HANDLER.onInstrumentationEvent(instrumentationEvent);
	}
	
	public static void logAndProfile(InstrumentableEvent event) {
		InstrumentationEventDto instrumentationEvent = new InstrumentationEventDto(System.currentTimeMillis(), Thread.currentThread(), event);
		LOG_INSTRUMENTATION_HANDLER.onInstrumentationEvent(instrumentationEvent);
		PROFILE_INSTRUMENTATION_HANDLER.onInstrumentationEvent(instrumentationEvent);
	}

	public static void logAndProfile(InstrumentableEvent event, InstrumentableProperty property1, Object property1Value) {
		InstrumentationEventDto instrumentationEvent = new InstrumentationEventDto(System.currentTimeMillis(), Thread.currentThread(), event, property1, property1Value);
		LOG_INSTRUMENTATION_HANDLER.onInstrumentationEvent(instrumentationEvent);
		PROFILE_INSTRUMENTATION_HANDLER.onInstrumentationEvent(instrumentationEvent);
	}

	public static void logAndProfile(InstrumentableEvent event, InstrumentableProperty property1, Object property1Value, InstrumentableProperty property2, Object property2Value) {
		InstrumentationEventDto instrumentationEvent = new InstrumentationEventDto(System.currentTimeMillis(), Thread.currentThread(), event, property1, property1Value, property2, property2Value);
		LOG_INSTRUMENTATION_HANDLER.onInstrumentationEvent(instrumentationEvent);
		PROFILE_INSTRUMENTATION_HANDLER.onInstrumentationEvent(instrumentationEvent);
	}

	public static void logAndProfile(InstrumentableEvent event, InstrumentableProperty property1, Object property1Value, InstrumentableProperty property2, Object property2Value, InstrumentableProperty property3, Object property3Value) {
		InstrumentationEventDto instrumentationEvent = new InstrumentationEventDto(System.currentTimeMillis(), Thread.currentThread(), event, property1, property1Value, property2, property2Value, property3, property3Value);
		LOG_INSTRUMENTATION_HANDLER.onInstrumentationEvent(instrumentationEvent);
		PROFILE_INSTRUMENTATION_HANDLER.onInstrumentationEvent(instrumentationEvent);
	}

	public static void logProfileAndCompute(InstrumentableEvent event) {
		InstrumentationEventDto instrumentationEvent = new InstrumentationEventDto(System.currentTimeMillis(), Thread.currentThread(), event);
		LOG_INSTRUMENTATION_HANDLER.onInstrumentationEvent(instrumentationEvent);
		PROFILE_INSTRUMENTATION_HANDLER.onInstrumentationEvent(instrumentationEvent);
		REPORT_INSTRUMENTATION_HANDLER.onInstrumentationEvent(instrumentationEvent);
	}

	public static void logProfileAndCompute(InstrumentableEvent event, InstrumentableProperty property1, Object property1Value) {
		InstrumentationEventDto instrumentationEvent = new InstrumentationEventDto(System.currentTimeMillis(), Thread.currentThread(), event, property1, property1Value);
		LOG_INSTRUMENTATION_HANDLER.onInstrumentationEvent(instrumentationEvent);
		PROFILE_INSTRUMENTATION_HANDLER.onInstrumentationEvent(instrumentationEvent);
		REPORT_INSTRUMENTATION_HANDLER.onInstrumentationEvent(instrumentationEvent);
	}

	public static void logProfileAndCompute(InstrumentableEvent event, InstrumentableProperty property1, Object property1Value, InstrumentableProperty property2, Object property2Value) {
		InstrumentationEventDto instrumentationEvent = new InstrumentationEventDto(System.currentTimeMillis(), Thread.currentThread(), event, property1, property1Value, property2, property2Value);
		LOG_INSTRUMENTATION_HANDLER.onInstrumentationEvent(instrumentationEvent);
		PROFILE_INSTRUMENTATION_HANDLER.onInstrumentationEvent(instrumentationEvent);
		REPORT_INSTRUMENTATION_HANDLER.onInstrumentationEvent(instrumentationEvent);
	}

	public static void logProfileAndCompute(InstrumentableEvent event, InstrumentableProperty property1, Object property1Value, InstrumentableProperty property2, Object property2Value, InstrumentableProperty property3, Object property3Value) {
		InstrumentationEventDto instrumentationEvent = new InstrumentationEventDto(System.currentTimeMillis(), Thread.currentThread(), event, property1, property1Value, property2, property2Value, property3, property3Value);
		LOG_INSTRUMENTATION_HANDLER.onInstrumentationEvent(instrumentationEvent);
		PROFILE_INSTRUMENTATION_HANDLER.onInstrumentationEvent(instrumentationEvent);
		REPORT_INSTRUMENTATION_HANDLER.onInstrumentationEvent(instrumentationEvent);
	}

	private WeakHashMap<Thread, REQUEST_TYPE> ongoingRequests = new WeakHashMap<Thread, REQUEST_TYPE>();
	
	UncaughtExceptionHandler ueh = new UncaughtExceptionHandler() {		
		public void uncaughtException(Thread t, Throwable e) {
			reportInternalEvent(t, DIE_UNCOUGHT_EXCEPTION, DIP_MSG, "Uncought Exception detected by Instrumentation facility", DIP_THROWABLE, e);
		}
	};
	
// TODO 18/04/2016
//	myLog = new Log("/....");
//	myReports = new Reports(TimeResolution, ...);
//	myProfile = new Profile(IP1, IP2, IP3, ... ou seria: param1 -- onde param1 representa o tick do time?)
//	myProfileTransaction = new ProfileTransaction(IP1, IP2, IP3, ...)
//	myProfile = new Profile(new ProfileTransactionHandler(handler, log, myProfileTransaction))
//
//	new Instrumentation(myLOG, myReports, myWhatever...)
//	Instrumentation(IInstrumentationHandler... instrumentationHandlers);	// which are listeners
//
//	log(ThatHappened);
//
//	ThatHappened = IInstrumentableProperty {
//		handlers = LOG_DEBUG, LOG_INFO, LOG_CRITICAL, LOG_ERROR, REPORTS, PROFILE, myHandler
//		name = "XXX";
//		param1 = {
//			name = "xxx"
//			type = absolute value | delta
//
//	-- ao se construir o log, as IInstrumentableProperties têm seus handlers setados para as respectivas instâncias, de modo que Instrumentation.reportEvent varre o array da IP e chama todos os métodos registrados -- isso significa que somente uma instância de instrumentation pode ser criada. É isso que queremos? NÃO! Faremos a busca nos 2 arrays a um custo O(n.m) -- que é melhor que um Hashtable	
	
	// helper methods
	/////////////////
	
	private void reportUnfinishedRequest(Thread t) {
		REQUEST_TYPE request = ongoingRequests.get(t);
		Throwable e = new Throwable("Unfinished request processing detected");
		e.setStackTrace(t.getStackTrace());
		reportInternalEvent(UNFINISHED_REQUEST_EVENT, requestProperty, request, DIP_THROWABLE, e);
	}
	
	private InstrumentationEventDto getInstrumentationEvent(Thread thread, IInstrumentableEvent ievent,
	                                                        InstrumentableProperty property1, Object value1,
	                                                        InstrumentableProperty property2, Object value2) {
		long currentTimeMillis = System.currentTimeMillis();
		return new InstrumentationEventDto(currentTimeMillis, thread, ievent,
		                                   property1, value1, property2, value2);
	}

	private InstrumentationEventDto getInstrumentationEvent(IInstrumentableEvent ievent,
	                                                        InstrumentableProperty property1, Object value1,
	                                                        InstrumentableProperty property2, Object value2) {
		long currentTimeMillis = System.currentTimeMillis();
		Thread thread          = Thread.currentThread();
		return new InstrumentationEventDto(currentTimeMillis, thread, ievent,
		                                   property1, value1, property2, value2);
	}

	private InstrumentationEventDto getInstrumentationEvent(IInstrumentableEvent ievent,
		                                                    InstrumentableProperty property1, Object value1,
		                                                    InstrumentableProperty property2, Object value2,
		                                                    InstrumentableProperty property3, Object value3) {
		long currentTimeMillis = System.currentTimeMillis();
		Thread thread          = Thread.currentThread();
		return new InstrumentationEventDto(currentTimeMillis, thread, ievent,
		                                   property1, value1, property2, value2, property3, value3);
	}

	private InstrumentationEventDto getInstrumentationEvent(IInstrumentableEvent ievent, InstrumentableProperty property, Object value) {
		long currentTimeMillis = System.currentTimeMillis();
		Thread thread          = Thread.currentThread();
		return new InstrumentationEventDto(currentTimeMillis, thread, ievent, property, value);
	}

	private InstrumentationEventDto getInstrumentationEvent(IInstrumentableEvent ievent) {
		long currentTimeMillis = System.currentTimeMillis();
		Thread thread          = Thread.currentThread();
		return new InstrumentationEventDto(currentTimeMillis, thread, ievent);
	}

	/** needed to satisfy the java need/limitation that super can only receive a value that may be set to an instance variable if it comes as a constructor parameter */
	private Instrumentation(String applicationName, REQUEST_PROPERTY_TYPE requestProperty,
	                        IInstrumentationHandler... instrumentationHandlers) {

		this.APPLICATION_NAME        = applicationName;
		this.requestProperty         = requestProperty;
		this.availableInstrumentationHandlers = instrumentationHandlers;
		
		// set & register internal events
		UNFINISHED_REQUEST_EVENT = new InstrumentableEvent("UNFINISHED_REQUEST", requestProperty);
	    REQUEST_START_EVENT      = new InstrumentableEvent("REQUEST_START",      requestProperty);
	    REQUEST_FINISH_EVENT     = new InstrumentableEvent("REQUEST_FINISH");
		reportInternalEvent(DIE_APP_START);
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				synchronized (ongoingRequests) {
					for (Thread t : ongoingRequests.keySet()) {
						reportUnfinishedRequest(t);
					}
				}
				reportInternalEvent(DIE_APP_SHUTDOWN);
			}
		});
	}
	
	public void reportRequestStart(REQUEST_TYPE requestData) {
		
		Thread ct = Thread.currentThread();

		synchronized (ongoingRequests) {

			// detect any unclosed transaction on the current thread
			REQUEST_TYPE currentlyOpennedRequest = ongoingRequests.get(ct);
			if (currentlyOpennedRequest != null) {
				reportUnfinishedRequest(ct);
			}		

			ongoingRequests.put(Thread.currentThread(), requestData);
			
			// detect any unclosed transaction on finished threads
			ArrayList<Thread> waitingToBeRemovedThreads = new ArrayList<Thread>();
			for (Thread t : ongoingRequests.keySet()) {
				if (!t.isAlive()) {
					reportUnfinishedRequest(t);
					waitingToBeRemovedThreads.add(t);
				}
			}
			// remove the dead threads
			for (int i=0; i<waitingToBeRemovedThreads.size(); i++) {
				ongoingRequests.remove(waitingToBeRemovedThreads.get(i));
			}
		}
		
		// set default exception handling
		ct.setUncaughtExceptionHandler(ueh);
		
		reportInternalEvent(REQUEST_START_EVENT, requestProperty, requestData);
	}
	
	public void reportRequestFinish() {
		synchronized (ongoingRequests) {
			ongoingRequests.remove(Thread.currentThread());
		}
		reportInternalEvent(REQUEST_FINISH_EVENT);
	}
	
	
	// instrumentation events reporting
	///////////////////////////////////
	
	public void reportEvent(IInstrumentableEvent ievent) {
		InstrumentationEventDto instrumentationEvent = getInstrumentationEvent(ievent);
		// please, make the following more efficient:
		Class<? extends IInstrumentationHandler>[] handleableBy = ievent.getTargetHandlers();
		for (int a=0; a<availableInstrumentationHandlers.length; a++) {
			IInstrumentationHandler availableHandler = availableInstrumentationHandlers[a];
			for (int h=0; h<handleableBy.length; h++) {
				if (handleableBy[h].isInstance(availableHandler)) {
					availableHandler.onInstrumentationEvent(instrumentationEvent);
				}
			}
		}
// O problema da falta de eficiência é que, a cada chamada, temos que calcular quem deve ser chamado.
// A melhor forma de resolver este problema seria ter uma lista de instrumentation handlers a ser chamado em um array,
// que deve ser único para cada ievent, e dependente dos availableHandlers passados no construtor.
// isso implica que, ao chamar reportEvent, a lista correspondente ao 'ievent' passado deve ser resgatada, para que possa ser
// percorrida. Se isso for feita através de um HashMap, ok... porém o ideal seria que 'iEvent' fornecesse sua própria lista.
// Surge, porém, outro problema: iEvent é um valor estático. Ele poderia ser alterado, porém poderia trazer problemas ao se criar
// mais de uma instância de Instrumentation por máquina virtual. Instrumentation, neste caso, seria um singleton -- isso pode, até, ser bom,
// pois evitaria a gente ter que ficar passando uma instância de instrumentation pra cá e pra lá -- neste caso, ela poderia ser incorporada através do
// import static e configurada no início da aplicação. SOlo. Existe alguma razão para eu querer mais de um instrumentation? Repare que eu, ainda assim,
// posso logar para diversos arquivos... algo a se pensar...
//		Events = {
//				Ev1 = 0, LOG;
//				Ev2 = 1, LOG, REPORT;
//				Ev3 = 2, REPORT;
//
//			}
//
//			log = new Instrumentation(LOG, REPORT, Events); {
//				myEvents = {
//					Ev1.callList(LOG.callback);
//					Ev2.callList(LOG.callback, REPORT.callback);
//					Ev3.callList(REPORT.callback);
//				}
//			}
//			log.reportEvent(Ev1, 1234) {
//				myEvents.Ev1.call(1234);
//			}
	}

	public void reportEvent(IInstrumentableEvent ievent, InstrumentableProperty property, Object value) {
		InstrumentationEventDto instrumentationEvent = getInstrumentationEvent(ievent, property, value);
		dispatchListenableEvent(EInstrumentationPropagableEvents.APPLICATION_INSTRUMENTATION_EVENT, instrumentationEvent);
	}

	public void reportEvent(IInstrumentableEvent ievent,
	                        InstrumentableProperty property1, Object value1,
	                        InstrumentableProperty property2, Object value2) {
		InstrumentationEventDto instrumentationEvent = getInstrumentationEvent(ievent, property1, value1, property2, value2);
		dispatchListenableEvent(EInstrumentationPropagableEvents.APPLICATION_INSTRUMENTATION_EVENT, instrumentationEvent);
	}
	
	public void reportEvent(IInstrumentableEvent ievent,
		                    InstrumentableProperty property1, Object value1,
		                    InstrumentableProperty property2, Object value2,
		                    InstrumentableProperty property3, Object value3) {
		InstrumentationEventDto instrumentationEvent = getInstrumentationEvent(ievent, property1, value1, property2, value2, property3, value3);
		dispatchListenableEvent(EInstrumentationPropagableEvents.APPLICATION_INSTRUMENTATION_EVENT, instrumentationEvent);
	}

	public void reportDebug(String msg) {
		reportEvent(DIE_DEBUG, DIP_MSG, msg);
	}
	
	public void reportThrowable(Throwable t, String msg) {
		reportEvent(DIE_REPORTED_THROWABLE, DIP_MSG, msg, DIP_THROWABLE, t);
	}
	
	public void reportUncoughtThrowable(Throwable t, String msg) {
		reportEvent(DIE_UNCOUGHT_EXCEPTION, DIP_MSG, msg, DIP_THROWABLE, t);
	}
	
	public boolean addInstrumentationPropagableEventsClient(InstrumentationPropagableEventsClient<EInstrumentationPropagableEvents> instrumentationPropagableEventsClient) throws IndirectMethodNotFoundException {
		return super.addListener(instrumentationPropagableEventsClient);
	}
}
