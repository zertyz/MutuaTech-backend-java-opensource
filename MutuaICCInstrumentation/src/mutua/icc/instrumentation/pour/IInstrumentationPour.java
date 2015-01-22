package mutua.icc.instrumentation.pour;

import mutua.icc.instrumentation.dto.EventDto;

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

public abstract class IInstrumentationPour {
	
	/** Resets the database, for testing purposes */
	public abstract void reset();
	
	/** Stores, for later reference, an instrumentable event without any property */
	public abstract void storeInstrumentableEvent(EventDto event);

	/** Returns a descriptor to start an instrumentation data traversal, since the beginning of history.
	 * -1 is returned if the limit of opened descriptors have been reached */
	public abstract int startTraversal();
	
	/** Returns a descriptor to start following the next instrumentation data appended to the instrumentation data.
	 * -1 is returned if the limit of opened descriptors have been reached */
	public abstract int startFollowing();
	
	/** Attempts to return the next instrumentation data for the given descriptor.
	 * Null is returned if no more data is available now, but later calls with the same parameters might
	 * provide results */
	public abstract EventDto getNextEvent(int descriptor);
	
	/** Avoids memory leaks by releasing resources related to the given descriptor */
	public abstract void closeDescriptor(int descriptor);
}
