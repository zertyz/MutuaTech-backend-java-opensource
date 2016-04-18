package mutua.icc.instrumentation.pour;

import java.io.IOException;

import mutua.icc.instrumentation.IInstrumentableProperty;
import mutua.icc.instrumentation.Instrumentation.EInstrumentationPropagableEvents;
import mutua.icc.instrumentation.dto.InstrumentationEventDto;
import mutua.icc.instrumentation.eventclients.InstrumentationPropagableEventsClient;

/** <pre>
 * IInstrumentationPour.java
 * =========================
 * (created by luiz, Jan 21, 2015)
 *
 * Defines access methods for instrumentation data
 *
 * @see console.InstrumentationData, database.InstrumentationData, rolingfile.InstrumentationData
 * @see compressedrolingfile.InstrumentationData, network.InstrumentationData
 * @version $Id$
 * @author luiz
 */

public interface IInstrumentationPour extends InstrumentationPropagableEventsClient<EInstrumentationPropagableEvents> {
	
	/** Consider the additional 'IInstrumentableProperty's when serializing data
	 *  TODO as of the Instrumentation Refactorings of 15/04/2016, this method seems not be needed anymore. May be refactored. */
	public abstract void considerInstrumentableProperties(IInstrumentableProperty[] instrumentableProperties);
	
	/** Resets the database, for testing purposes */
	public abstract void reset();
	
	/** Stores, for later reference, an instrumentable event without any property */
	public abstract void storeInstrumentableEvent(InstrumentationEventDto event) throws IOException;

	/** Returns a descriptor to start an instrumentation data traversal, since the beginning of history.
	 * -1 is returned if the limit of opened descriptors have been reached */
	public abstract int startTraversal();
	
	/** Returns a descriptor to start following the next instrumentation data appended to the instrumentation data.
	 * -1 is returned if the limit of opened descriptors have been reached */
	public abstract int startFollowing();
	
	/** Attempts to return the next instrumentation data for the given descriptor.
	 * Null is returned if no more data is available now, but later calls with the same parameters might
	 * provide results */
	public abstract InstrumentationEventDto getNextEvent(int descriptor);
	
	/** Avoids memory leaks by releasing resources related to the given descriptor */
	public abstract void closeDescriptor(int descriptor);
}
