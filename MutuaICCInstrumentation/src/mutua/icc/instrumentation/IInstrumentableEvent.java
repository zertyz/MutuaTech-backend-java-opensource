package mutua.icc.instrumentation;


/** <pre>
 * IInstrumentableEvent.java
 * =========================
 * (created by luiz, Jan 21, 2015)
 *
 * Defines an instrumentable event
 *
 * @see RelatedClass(es)
 * @version $Id$
 * @author luiz
 */

public class IInstrumentableEvent {
	
	private final String name;
	private final IInstrumentableProperty[] properties;
	
	public IInstrumentableEvent(String name, IInstrumentableProperty property) {
		this.name       = name;
		this.properties = new IInstrumentableProperty[] {property};
	}

	public IInstrumentableEvent(String name, IInstrumentableProperty property1, IInstrumentableProperty property2) {
		this.name       = name;
		this.properties = new IInstrumentableProperty[] {property1, property2};
	}

	public IInstrumentableEvent(String name) {
		this.name       = name;
		this.properties = new IInstrumentableProperty[] {};
	}

	/** The name of the event */
	public String getName() {
		return name;
	}
	
	/** Properties that are related to this event */
	public IInstrumentableProperty[] getProperties() {
		return properties;
	}

	// some default events:
	// app start
	// app shutdown
	// uncouth exception
	// general error
	// general warning
	// debug information
	
}
