package mutua.icc.instrumentation;

import static mutua.icc.instrumentation.DefaultInstrumentationEvents.DIE_APP_SHUTDOWN;
import static mutua.icc.instrumentation.DefaultInstrumentationEvents.DIE_APP_START;
import static mutua.icc.instrumentation.DefaultInstrumentationEvents.DIE_REPORTED_THROWABLE;
import static mutua.icc.instrumentation.DefaultInstrumentationEvents.DIE_UNCOUGHT_EXCEPTION;
import static mutua.icc.instrumentation.DefaultInstrumentationProperties.DIP_MSG;
import static mutua.icc.instrumentation.DefaultInstrumentationProperties.DIP_THROWABLE;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.WeakHashMap;

import mutua.events.DirectEventLink;
import mutua.events.EventServer;
import mutua.events.IEventLink;
import mutua.icc.instrumentation.Instrumentation.EInstrumentationPropagableEvents;
import mutua.icc.instrumentation.dto.InstrumentationEventDto;
import mutua.icc.instrumentation.pour.IInstrumentationPour;
import mutua.icc.instrumentation.pour.PourFactory;
import mutua.imi.IndirectMethodNotFoundException;


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

public class Instrumentation<REQUEST_PROPERTY_TYPE extends IInstrumentableProperty, REQUEST_TYPE> extends EventServer<EInstrumentationPropagableEvents> {
	
	
	// MutuaEvent framework
	///////////////////////
	// for the event propagation among different clients (the
	// one that generates logs, the profiling, reporting, etc)
	
	public enum EInstrumentationPropagableEvents {
		INTERNAL_FRAMEWORK_INSTRUMENTATION_EVENT,
		APPLICATION_INSTRUMENTATION_EVENT,
	};
	private static IEventLink<EInstrumentationPropagableEvents> propagableEventsLink = new DirectEventLink<EInstrumentationPropagableEvents>(EInstrumentationPropagableEvents.class);
	
	// data structures
	//////////////////
	
	private String APPLICATION_NAME;
	private REQUEST_PROPERTY_TYPE requestProperty;
	
	private InstrumentableEvent UNFINISHED_REQUEST_EVENT;
	private InstrumentableEvent REQUEST_START_EVENT;
	private InstrumentableEvent REQUEST_FINISH_EVENT;

	
	private WeakHashMap<Thread, REQUEST_TYPE> ongoingRequests = new WeakHashMap<Thread, REQUEST_TYPE>();
	
	UncaughtExceptionHandler ueh = new UncaughtExceptionHandler() {		
		public void uncaughtException(Thread t, Throwable e) {
			reportInternalEvent(t, DIE_UNCOUGHT_EXCEPTION, DIP_MSG, "Uncought Exception detected by Instrumentation facility", DIP_THROWABLE, e);
		}
	};
	
	private final IInstrumentationPour pour;
	
	
	// helper methods
	/////////////////
	
	private void reportUnfinishedRequest(Thread t) {
		REQUEST_TYPE request = ongoingRequests.get(t);
		Throwable e = new Throwable("Unfinished request processing detected");
		e.setStackTrace(t.getStackTrace());
		reportInternalEvent(UNFINISHED_REQUEST_EVENT, requestProperty, request, DIP_THROWABLE, e);
	}
	
	private void addInstrumentableProperties(ArrayList<IInstrumentableProperty> instrumentableProperties, IInstrumentableEvent... instrumentableEvents) {
		for (IInstrumentableEvent instrumentableEvent : instrumentableEvents) {
			IInstrumentableProperty[] instrumentableEventProperties = instrumentableEvent.getInstrumentableEvent().getProperties();
			for (IInstrumentableProperty instrumentableEventProperty : instrumentableEventProperties) {
				instrumentableProperties.add(instrumentableEventProperty);
			}
		}
	}

	private InstrumentationEventDto getInstrumentationEvent(Thread thread, IInstrumentableEvent ievent,
	                                                        IInstrumentableProperty property1, Object value1,
	                                                        IInstrumentableProperty property2, Object value2) {
		long currentTimeMillis = System.currentTimeMillis();
		return new InstrumentationEventDto(currentTimeMillis, APPLICATION_NAME, thread, ievent.getInstrumentableEvent(),
		                                   property1, value1, property2, value2);
	}

	private InstrumentationEventDto getInstrumentationEvent(IInstrumentableEvent ievent,
	                                                        IInstrumentableProperty property1, Object value1,
	                                                        IInstrumentableProperty property2, Object value2) {
		long currentTimeMillis = System.currentTimeMillis();
		Thread thread          = Thread.currentThread();
		return new InstrumentationEventDto(currentTimeMillis, APPLICATION_NAME, thread, ievent.getInstrumentableEvent(),
		                                   property1, value1, property2, value2);
	}

	private InstrumentationEventDto getInstrumentationEvent(IInstrumentableEvent ievent, IInstrumentableProperty property, Object value) {
		long currentTimeMillis = System.currentTimeMillis();
		Thread thread          = Thread.currentThread();
		return new InstrumentationEventDto(currentTimeMillis, APPLICATION_NAME, thread, ievent.getInstrumentableEvent(), property, value);
	}

	private InstrumentationEventDto getInstrumentationEvent(IInstrumentableEvent ievent) {
		long currentTimeMillis = System.currentTimeMillis();
		Thread thread          = Thread.currentThread();
		return new InstrumentationEventDto(currentTimeMillis, APPLICATION_NAME, thread, ievent.getInstrumentableEvent());
	}

	
	// report start of application, shutdown hook to report the end
	public Instrumentation(String applicationName, REQUEST_PROPERTY_TYPE requestProperty, IInstrumentableEvent... instrumentableEvents) {
		
		super(propagableEventsLink);
		
		this.APPLICATION_NAME = applicationName;
		this.requestProperty  = requestProperty;
		
		// get the poor
		ArrayList<IInstrumentableProperty> instrumentableProperties = new ArrayList<IInstrumentableProperty>();
		addInstrumentableProperties(instrumentableProperties, instrumentableEvents);
		addInstrumentableProperties(instrumentableProperties, DefaultInstrumentationEvents.values());
		IInstrumentableProperty[] instrumentablePropertiesArray = instrumentableProperties.toArray(new IInstrumentableProperty[instrumentableProperties.size()]);
		pour = PourFactory.getInstrumentationPour(instrumentablePropertiesArray);

		// add the default instrumentation propagable events consumer (the instrumentation poor notifier)
		try {
			super.addClient(pour);
		} catch (IndirectMethodNotFoundException e) {
			String msg = "Exception while initializing the Instrumentation Propagable Events framework";
			InstrumentationEventDto event = getInstrumentationEvent(DIE_UNCOUGHT_EXCEPTION, DIP_MSG, msg, DIP_THROWABLE, e);
			pour.storeInstrumentableEvent(event);
			throw new RuntimeException(msg, e);
		}
		
		// set & register internal events
		UNFINISHED_REQUEST_EVENT = new InstrumentableEvent("UNFINISHED_REQUEST", requestProperty);
	    REQUEST_START_EVENT      = new InstrumentableEvent("REQUEST_START",      requestProperty);
	    REQUEST_FINISH_EVENT     = new InstrumentableEvent("REQUEST_FINISH");
		reportInternalEvent(DIE_APP_START);
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				for (Thread t : ongoingRequests.keySet()) {
					reportUnfinishedRequest(t);
				}
				reportInternalEvent(DIE_APP_SHUTDOWN);
			}
		});
		
	}
	
	public void reportRequestStart(REQUEST_TYPE requestData) {
		
		// detect any unclosed transaction on the current thread
		Thread ct = Thread.currentThread();
		REQUEST_TYPE currentlyOpennedRequest = ongoingRequests.get(ct);
		if (currentlyOpennedRequest != null) {
			reportUnfinishedRequest(ct);
		}
		ongoingRequests.put(Thread.currentThread(), requestData);
		
		// detect any unclosed transaction on finished threads
		synchronized (ongoingRequests) {
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
		ongoingRequests.remove(Thread.currentThread());
		reportInternalEvent(REQUEST_FINISH_EVENT);
	}
	
	
	// internal instrumentation events reports
	//////////////////////////////////////////
	
	private void reportInternalEvent(IInstrumentableEvent ievent) {
		InstrumentationEventDto instrumentationEvent = getInstrumentationEvent(ievent);
		dispatchListenableEvent(EInstrumentationPropagableEvents.INTERNAL_FRAMEWORK_INSTRUMENTATION_EVENT, instrumentationEvent);
	}
	
	private void reportInternalEvent(IInstrumentableEvent ievent, IInstrumentableProperty property, Object value) {
		InstrumentationEventDto instrumentationEvent = getInstrumentationEvent(ievent, property, value);
		dispatchListenableEvent(EInstrumentationPropagableEvents.INTERNAL_FRAMEWORK_INSTRUMENTATION_EVENT, instrumentationEvent);
	}
	
	private void reportInternalEvent(Thread thread, IInstrumentableEvent ievent,
	                                 IInstrumentableProperty property1, Object value1,
	                                 IInstrumentableProperty property2, Object value2) {
		InstrumentationEventDto instrumentationEvent = getInstrumentationEvent(thread, ievent, property1, value1, property2, value2);
		dispatchListenableEvent(EInstrumentationPropagableEvents.INTERNAL_FRAMEWORK_INSTRUMENTATION_EVENT, instrumentationEvent);
	}

	private void reportInternalEvent(IInstrumentableEvent ievent,
	                                 IInstrumentableProperty property1, Object value1,
	                                 IInstrumentableProperty property2, Object value2) {
		InstrumentationEventDto instrumentationEvent = getInstrumentationEvent(ievent, property1, value1, property2, value2);
		dispatchListenableEvent(EInstrumentationPropagableEvents.INTERNAL_FRAMEWORK_INSTRUMENTATION_EVENT, instrumentationEvent);
	}

	
	// application instrumentation events reports
	/////////////////////////////////////////////
	
	public void reportEvent(IInstrumentableEvent ievent) {
		InstrumentationEventDto instrumentationEvent = getInstrumentationEvent(ievent);
		dispatchListenableEvent(EInstrumentationPropagableEvents.APPLICATION_INSTRUMENTATION_EVENT, instrumentationEvent);
	}

	public void reportEvent(IInstrumentableEvent ievent, IInstrumentableProperty property, Object value) {
		InstrumentationEventDto instrumentationEvent = getInstrumentationEvent(ievent, property, value);
		dispatchListenableEvent(EInstrumentationPropagableEvents.APPLICATION_INSTRUMENTATION_EVENT, instrumentationEvent);
	}

	public void reportEvent(IInstrumentableEvent ievent,
	                        IInstrumentableProperty property1, Object value1,
	                        IInstrumentableProperty property2, Object value2) {
		InstrumentationEventDto instrumentationEvent = getInstrumentationEvent(ievent, property1, value1, property2, value2);
		dispatchListenableEvent(EInstrumentationPropagableEvents.APPLICATION_INSTRUMENTATION_EVENT, instrumentationEvent);
	}
	
	public void reportThrowable(Throwable t, String msg) {
		reportEvent(DIE_REPORTED_THROWABLE, DIP_MSG, msg, DIP_THROWABLE, t);
	}
	
	public void reportUncoughtThrowable(Throwable t, String msg) {
		reportEvent(DIE_UNCOUGHT_EXCEPTION, DIP_MSG, msg, DIP_THROWABLE, t);
	}
}
