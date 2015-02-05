package mutua.icc.instrumentation;

import static mutua.icc.instrumentation.MutuaEventAdditionalEventLinksInstrumentationProperties.*;

/** <pre>
 * MutuaEventAdditionalEventLinksInstrumentationEvents.java
 * ========================================================
 * (created by luiz, Feb 1, 2015)
 *
 * Defines the available events that can participate on instrumentation logs
 *
 * @see MutuaEventAdditionalEventLinksInstrumentationProperties
 * @version $Id$
 * @author luiz
 */

public enum MutuaEventAdditionalEventLinksInstrumentationEvents implements IInstrumentableEvent {

	
	// queue management
	///////////////////

	
	// thread management
	////////////////////
	
	IE_INTERRUPTING                    ("Interrupting PostgreSQL Queue Manager...", IP_QUEUE_TABLE_NAME),
	IE_WAITING_CONSUMERS               ("There are consumers still processing events. Waiting..."),
	IE_FORCING_CONSUMERS_SHUTDOWN      ("Consumers are still processing events after the stop command. Forcing the interruption..."),
	IE_INTERRUPTION_COMPLETE           ("Interruption of PostgreSQL QueueManager complete", IP_QUEUE_TABLE_NAME),
	
	
	// errors
	/////////
	
	
	;
	
	
	private InstrumentableEvent instrumentableEvent;
	
	private MutuaEventAdditionalEventLinksInstrumentationEvents(String name, IInstrumentableProperty property) {
		instrumentableEvent = new InstrumentableEvent(name, property);
	}
	
	private MutuaEventAdditionalEventLinksInstrumentationEvents(String name, IInstrumentableProperty property1, IInstrumentableProperty property2) {
		instrumentableEvent = new InstrumentableEvent(name, property1, property2);
	}
	
	private MutuaEventAdditionalEventLinksInstrumentationEvents(String name) {
		instrumentableEvent = new InstrumentableEvent(name);
	}

	@Override
	public InstrumentableEvent getInstrumentableEvent() {
		return instrumentableEvent;
	}

}
