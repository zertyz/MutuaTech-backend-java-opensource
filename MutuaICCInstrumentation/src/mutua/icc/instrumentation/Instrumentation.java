package mutua.icc.instrumentation;

import static mutua.icc.instrumentation.DefaultInstrumentationEvents.*;
import static mutua.icc.instrumentation.DefaultInstrumentationProperties.*;

import java.io.PrintStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.WeakHashMap;

import mutua.icc.instrumentation.dto.EventDto;
import mutua.icc.instrumentation.pour.IInstrumentationPour;
import mutua.icc.instrumentation.pour.PourFactory;


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

public class Instrumentation<P extends IInstrumentableProperty<?>, R> {
	
	
	// data structures
	//////////////////
	
	private String APPLICATION_NAME;
	private P requestProperty;
	
	private IInstrumentableEvent UNFINISHED_REQUEST_EVENT;
	private IInstrumentableEvent REQUEST_START_EVENT;
	private IInstrumentableEvent REQUEST_FINISH_EVENT;

	
	private WeakHashMap<Thread, R> ongoingRequests = new WeakHashMap<Thread, R>();
	
	UncaughtExceptionHandler ueh = new UncaughtExceptionHandler() {		
		public void uncaughtException(Thread t, Throwable e) {
			reportEvent(DIE_UNCOUGHT_EXCEPTION, DIP_MSG, "Uncought Exception detected by Instrumentation facility", DIP_THROWABLE, e);
		}
	};
	
	private static IInstrumentationPour pour = PourFactory.getInstrumentationPour();
	
	
	// helper methods
	/////////////////
	
	private void reportUnfinishedRequest(Thread t) {
		R request = ongoingRequests.get(t);
		Throwable e = new Throwable("Unfinished request processing detected");
		e.setStackTrace(t.getStackTrace());
		reportEvent(UNFINISHED_REQUEST_EVENT, requestProperty, request, DIP_THROWABLE, e);
	}

	
	// report start of application, shutdown hook to report the end
	public Instrumentation(String applicationName, P requestProperty) {
		this.APPLICATION_NAME = applicationName;
		this.requestProperty  = requestProperty;
		UNFINISHED_REQUEST_EVENT = new IInstrumentableEvent("UNFINISHED_REQUEST", requestProperty);
	    REQUEST_START_EVENT      = new IInstrumentableEvent("REQUEST_START", requestProperty);
	    REQUEST_FINISH_EVENT     = new IInstrumentableEvent("REQUEST_FINISH");
		reportEvent(DIE_APP_START);
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				for (Thread t : ongoingRequests.keySet()) {
					reportUnfinishedRequest(t);
				}
				reportEvent(DIE_APP_SHUTDOWN);
			}
		});
	}
	
	public void reportRequestStart(R requestData) {
		
		// detect any unclosed transaction on the current thread
		Thread ct = Thread.currentThread();
		R currentlyOpennedRequest = ongoingRequests.get(ct);
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
		
		reportEvent(REQUEST_START_EVENT, requestProperty, requestData);
	}
	
	public void reportRequestFinish() {
		ongoingRequests.remove(Thread.currentThread());
		reportEvent(REQUEST_FINISH_EVENT);
	}
	
	public void reportEvent(IInstrumentableEvent event) {
		long currentTimeMillis = System.currentTimeMillis();
		Thread thread          = Thread.currentThread();
		pour.storeInstrumentableEvent(new EventDto(currentTimeMillis, APPLICATION_NAME, thread, event));
	}

	public void reportEvent(IInstrumentableEvent event, IInstrumentableProperty<?> property, Object value) {
		long currentTimeMillis = System.currentTimeMillis();
		Thread thread          = Thread.currentThread();
		pour.storeInstrumentableEvent(new EventDto(currentTimeMillis, APPLICATION_NAME, thread, event, property, value));
	}

	public void reportEvent(IInstrumentableEvent event,
	                               IInstrumentableProperty<?> property1, Object value1,
	                               IInstrumentableProperty<?> property2, Object value2) {
		long currentTimeMillis = System.currentTimeMillis();
		Thread thread          = Thread.currentThread();
		pour.storeInstrumentableEvent(new EventDto(currentTimeMillis, APPLICATION_NAME, thread, event,
		                              property1, value1, property2, value2));
	}

	public void reportThrowable(Throwable t, String msg) {
		reportEvent(DIE_REPORTED_THROWABLE, DIP_MSG, msg, DIP_THROWABLE, t);
	}
	
	public void reportUncoughtThrowable() {
		
	}
}
