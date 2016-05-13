package mutua.icc.instrumentation;

import java.lang.Thread.UncaughtExceptionHandler;

import static mutua.icc.instrumentation.DefaultInstrumentationEvents.*;

import mutua.icc.instrumentation.dto.InstrumentationEventDto;
import mutua.icc.instrumentation.handlers.IInstrumentationHandler;

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

public class Instrumentation {

	// data structures
	//////////////////
	
	// 'IInstrumentationHandler's
	private static IInstrumentationHandler  LOG_INSTRUMENTATION_HANDLER     = null;
	private static IInstrumentationHandler  REPORT_INSTRUMENTATION_HANDLER  = null;
	private static IInstrumentationHandler  PROFILE_INSTRUMENTATION_HANDLER = null;
	
	static {
		
		// hooks setting
		////////////////
		
		// set the VM default uncaught exception handler
		UncaughtExceptionHandler ueh = new UncaughtExceptionHandler() {		
			public void uncaughtException(Thread t, Throwable e) {
				justLog(UNCAUGHT_EXCEPTION_EVENT, MSG_PROPERTY, "Uncought Exception detected by Instrumentation facility", THROWABLE_PROPERTY, e);
			}
		};
		Thread.setDefaultUncaughtExceptionHandler(ueh);

		// shutdown hook to report application shutdown & flush, process, send and/or close streams
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				justLog(APP_SHUTDOWN_EVENT);
				if (REPORT_INSTRUMENTATION_HANDLER != null) {
					REPORT_INSTRUMENTATION_HANDLER.close();
				}
				if (PROFILE_INSTRUMENTATION_HANDLER != null) {
					PROFILE_INSTRUMENTATION_HANDLER.close();
				}
				if (LOG_INSTRUMENTATION_HANDLER != null) {
					LOG_INSTRUMENTATION_HANDLER.close();
				}
			}
		});

	}


	
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

		// close any instrumentation handlers before using new
		if (REPORT_INSTRUMENTATION_HANDLER != null) {
			REPORT_INSTRUMENTATION_HANDLER.close();
		}
		if (PROFILE_INSTRUMENTATION_HANDLER != null) {
			PROFILE_INSTRUMENTATION_HANDLER.close();
		}
		if (LOG_INSTRUMENTATION_HANDLER != null) {
			LOG_INSTRUMENTATION_HANDLER.close();
		}

		
		LOG_INSTRUMENTATION_HANDLER     = logInstrumentationHandler;
		REPORT_INSTRUMENTATION_HANDLER  = reportInstrumentationHandler;
		PROFILE_INSTRUMENTATION_HANDLER = profileInstrumentationHandler;
		
		// set & register internal events
		justLog(APP_START_EVENT);
		
	}
	
	public static void startRequest(InstrumentableProperty property1, Object property1Value) {
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
	
	public static void finishRequest() {
		InstrumentationEventDto requestStartInstrumentationEvent = new InstrumentationEventDto(System.currentTimeMillis(), Thread.currentThread(), REQUEST_FINISH_EVENT);
		if (LOG_INSTRUMENTATION_HANDLER != null) {
			LOG_INSTRUMENTATION_HANDLER.onRequestFinish(requestStartInstrumentationEvent);
		}
		if (REPORT_INSTRUMENTATION_HANDLER != null) {
			REPORT_INSTRUMENTATION_HANDLER.onRequestFinish(requestStartInstrumentationEvent);
		}
		if (PROFILE_INSTRUMENTATION_HANDLER != null) {
			PROFILE_INSTRUMENTATION_HANDLER.onRequestFinish(requestStartInstrumentationEvent);
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

	public static void justLog(InstrumentableEvent event,
		InstrumentableProperty property1, Object property1Value,
		InstrumentableProperty property2, Object property2Value,
		InstrumentableProperty property3, Object property3Value,
		InstrumentableProperty property4, Object property4Value) {
		LOG_INSTRUMENTATION_HANDLER.onInstrumentationEvent(new InstrumentationEventDto(System.currentTimeMillis(), Thread.currentThread(), event,
			new Object[] {
				property1, property1Value,
				property2, property2Value,
				property3, property3Value,
				property4, property4Value,
		}));
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
	
	public static void logAndCompute(InstrumentableEvent event,
		InstrumentableProperty property1, Object property1Value,
		InstrumentableProperty property2, Object property2Value,
		InstrumentableProperty property3, Object property3Value,
		InstrumentableProperty property4, Object property4Value) {
		InstrumentationEventDto instrumentationEvent = new InstrumentationEventDto(System.currentTimeMillis(), Thread.currentThread(), event, new Object[] {
			property1, property1Value,
			property2, property2Value,
			property3, property3Value,
			property4, property4Value});
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
	
	public static void logProfileAndCompute(InstrumentableEvent event,
		InstrumentableProperty property1, Object property1Value,
		InstrumentableProperty property2, Object property2Value,
		InstrumentableProperty property3, Object property3Value,
		InstrumentableProperty property4, Object property4Value) {
		InstrumentationEventDto instrumentationEvent = new InstrumentationEventDto(System.currentTimeMillis(), Thread.currentThread(), event, new Object[] {
			property1, property1Value,
			property2, property2Value,
			property3, property3Value,
			property4, property4Value});
		LOG_INSTRUMENTATION_HANDLER.onInstrumentationEvent(instrumentationEvent);
		PROFILE_INSTRUMENTATION_HANDLER.onInstrumentationEvent(instrumentationEvent);
		REPORT_INSTRUMENTATION_HANDLER.onInstrumentationEvent(instrumentationEvent);
	}


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
	
	public static void reportDebug(String msg) {
		justLog(DEBUG_EVENT, MSG_PROPERTY, msg);
	}
	
	public static void reportThrowable(Throwable t, String msg) {
		justLog(REPORTED_THROWABLE_EVENT, MSG_PROPERTY, msg, THROWABLE_PROPERTY, t);
	}
	
	public static void reportUncaughtThrowable(Throwable t, String msg) {
		justLog(UNCAUGHT_EXCEPTION_EVENT, MSG_PROPERTY, msg, THROWABLE_PROPERTY, t);
	}
	
}
