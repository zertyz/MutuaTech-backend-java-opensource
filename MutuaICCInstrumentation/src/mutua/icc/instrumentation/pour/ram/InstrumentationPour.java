package mutua.icc.instrumentation.pour.ram;

import java.util.Hashtable;

import mutua.icc.instrumentation.dto.InstrumentationEventDto;
import mutua.icc.instrumentation.pour.IInstrumentationPour;

/** <pre>
 * InstrumentationPour.java
 * ========================
 * (created by luiz, Jan 21, 2015)
 *
 * Implements the RAM version of 'IInstrumentationData'
 *
 * @see IInstrumentationPour
 * @version $Id$
 * @author luiz
 */

public class InstrumentationPour implements IInstrumentationPour {
	

	// configurable constants
	/////////////////////////
	

	/** Maximum number of the last log lines to keep on memory */
	public static int MAX_LINES = 99;

	/** Maximum number of simultaneous traversal descriptors used to read log entries */
	private static final int MAX_DESCRIPTORS = 1;
	
	
	// variables
	////////////
	
	// data := { [index1] = eventData1, [index2] = eventData2, ... }
	private static Hashtable<Integer, InstrumentationEventDto> data = new Hashtable<Integer, InstrumentationEventDto>(MAX_LINES+1);
	private static int dataIndex = 0;
	// free descriptors have the value -1
	private static int[] descriptorLastIndexes = new int[MAX_DESCRIPTORS];
	
	static {
		for (int i=0; i<descriptorLastIndexes.length; i++) {
			descriptorLastIndexes[i] = -1;
		}
	}

	
	/*******************
	** HELPER METHODS **
	*******************/
	
	private void holdEntry(InstrumentationEventDto event) {
		data.put(dataIndex++, event);
		while (data.size() > MAX_LINES) {
			data.remove(dataIndex - MAX_LINES - 1);
		}
	}
	
	private int findFreeDescriptor() {
		for (int i=0; i<descriptorLastIndexes.length; i++) {
			if (descriptorLastIndexes[i] == -1) {
				return i;
			}
		}
		return -1;
	}

	
	/****************************************
	** IInstrumentationData IMPLEMENTATION **
	****************************************/
	
	@Override
	public void reset() {
		data.clear();
		dataIndex = 0;
	}

	@Override
	public void storeInstrumentableEvent(InstrumentationEventDto event) {
		holdEntry(event);
	}


	@Override
	public int startTraversal() {
		int descriptor = findFreeDescriptor();
		if (descriptor != -1) {
			descriptorLastIndexes[descriptor] = 0;
		}
		return descriptor;
	}


	@Override
	public int startFollowing() {
		int descriptor = findFreeDescriptor();
		if (descriptor != -1) {
			descriptorLastIndexes[descriptor] = dataIndex;
		}
		return descriptor;
	}


	@Override
	public InstrumentationEventDto getNextEvent(int descriptor) {
		return data.get(descriptorLastIndexes[descriptor]++);
	}


	@Override
	public void closeDescriptor(int descriptor) {
		descriptorLastIndexes[descriptor] = -1;
	}
}
