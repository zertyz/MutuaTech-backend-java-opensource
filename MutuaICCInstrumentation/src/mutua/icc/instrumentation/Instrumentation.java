package mutua.icc.instrumentation;

import static mutua.icc.instrumentation.DefaultInstrumentationEvents.*;
import static mutua.icc.instrumentation.DefaultInstrumentationProperties.*;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.WeakHashMap;

import mutua.events.DirectEventLink;
import mutua.events.EventServer;
import mutua.events.IEventLink;
import mutua.icc.instrumentation.Instrumentation.EInstrumentationPropagableEvents;
import mutua.icc.instrumentation.dto.InstrumentationEventDto;
import mutua.icc.instrumentation.eventclients.InstrumentationPropagableEventsClient;
import mutua.icc.instrumentation.pour.IInstrumentationPour;
import mutua.icc.instrumentation.pour.PourFactory;
import mutua.icc.instrumentation.pour.PourFactory.EInstrumentationDataPours;
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
	
	// data structures
	//////////////////
	
	private IEventLink<EInstrumentationPropagableEvents> propagableEventsLink;

	private String APPLICATION_NAME;
	private REQUEST_PROPERTY_TYPE requestProperty;
	
	public InstrumentableEvent UNFINISHED_REQUEST_EVENT;
	public InstrumentableEvent REQUEST_START_EVENT;
	public InstrumentableEvent REQUEST_FINISH_EVENT;

	
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

	private InstrumentationEventDto getInstrumentationEvent(IInstrumentableEvent ievent,
		                                                    IInstrumentableProperty property1, Object value1,
		                                                    IInstrumentableProperty property2, Object value2,
		                                                    IInstrumentableProperty property3, Object value3) {
		long currentTimeMillis = System.currentTimeMillis();
		Thread thread          = Thread.currentThread();
		return new InstrumentationEventDto(currentTimeMillis, APPLICATION_NAME, thread, ievent.getInstrumentableEvent(),
		                                   property1, value1, property2, value2, property3, value3);
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

	/** needed to satisfy the java need/limitation that super can only receive a value that may be set to an instance variable if it comes as a constructor parameter */
	private Instrumentation(String applicationName, REQUEST_PROPERTY_TYPE requestProperty,
	                       IEventLink<EInstrumentationPropagableEvents> propagableEventsLink,
	                       EInstrumentationDataPours pourType, String descriptorReference,
	                       IInstrumentableEvent... instrumentableEvents) {

		super(propagableEventsLink);

		this.APPLICATION_NAME = applicationName;
		this.requestProperty  = requestProperty;
		
		// get & configure the poor
		pour = PourFactory.getInstrumentationPour(pourType, descriptorReference, new IInstrumentableProperty[] {});
		addInstrumentableEvents(instrumentableEvents);
		addInstrumentableEvents(DefaultInstrumentationEvents.values());

		// add the default instrumentation propagable events consumer (the instrumentation poor notifier)
		try {
			super.addClient(pour);
		} catch (IndirectMethodNotFoundException e) {
			String msg = "Exception while initializing the Instrumentation Propagable Events framework";
			InstrumentationEventDto event = getInstrumentationEvent(DIE_UNCOUGHT_EXCEPTION, DIP_MSG, msg, DIP_THROWABLE, e);
			try {
				pour.storeInstrumentableEvent(event);
			} catch (IOException e1) {
				e1.printStackTrace();
				System.out.println("Error documenting the log event "+event);
			}
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
				synchronized (ongoingRequests) {
					for (Thread t : ongoingRequests.keySet()) {
						reportUnfinishedRequest(t);
					}
				}
				reportInternalEvent(DIE_APP_SHUTDOWN);
			}
		});
	}
	
	/** report start of application, shutdown hook to report the end */
	public Instrumentation(String applicationName, REQUEST_PROPERTY_TYPE requestProperty,
	                       EInstrumentationDataPours pourType, String descriptorReference,
	                       IInstrumentableEvent... instrumentableEvents) {
		
		this(applicationName, requestProperty,
			 new DirectEventLink<EInstrumentationPropagableEvents>(EInstrumentationPropagableEvents.class),
			 pourType, descriptorReference,
			 instrumentableEvents);
		
	}
	
	/** Includes the provided 'instrumentableEvents' on the allowed events for this instrumentation pour
	 *  -- needed for serialization purposes */
	public void addInstrumentableEvents(IInstrumentableEvent... instrumentableEvents) {
		for (IInstrumentableEvent instrumentableEvent : instrumentableEvents) {
			IInstrumentableProperty[] instrumentableEventProperties = instrumentableEvent.getInstrumentableEvent().getProperties();
			pour.considerInstrumentableProperties(instrumentableEventProperties);
		}
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
	
	public void reportEvent(IInstrumentableEvent ievent,
		                    IInstrumentableProperty property1, Object value1,
		                    IInstrumentableProperty property2, Object value2,
		                    IInstrumentableProperty property3, Object value3) {
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
		return super.addClient(instrumentationPropagableEventsClient);
	}
}
