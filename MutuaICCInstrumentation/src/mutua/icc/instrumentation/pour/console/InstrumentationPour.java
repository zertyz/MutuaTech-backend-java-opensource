package mutua.icc.instrumentation.pour.console;

import java.text.FieldPosition;
import java.text.SimpleDateFormat;

import mutua.icc.instrumentation.IInstrumentableEvent;
import mutua.icc.instrumentation.IInstrumentableProperty;
import mutua.icc.instrumentation.dto.EventDto;
import mutua.icc.instrumentation.pour.IInstrumentationPour;

/** <pre>
 * InstrumentationPour.java
 * ========================
 * (created by luiz, Jan 21, 2015)
 *
 * Implements the CONSOLE version of 'IInstrumentationData'
 *
 * @see IInstrumentationPour
 * @version $Id$
 * @author luiz
 */

public class InstrumentationPour extends IInstrumentationPour {

	
	
	
	/*******************
	** HELPER METHODS **
	*******************/
	
	
	
	/****************************************
	** IInstrumentationData IMPLEMENTATION **
	****************************************/
	
	@Override
	public void reset() {}

	@Override
	public void storeInstrumentableEvent(EventDto event) {
		System.out.println(event.toString());
	}

	@Override
	public int startTraversal() {
		return -1;
	}

	@Override
	public int startFollowing() {
		return -1;
	}

	@Override
	public EventDto getNextEvent(int descriptor) {
		return null;
	}

	@Override
	public void closeDescriptor(int descriptor) {}
	
}
