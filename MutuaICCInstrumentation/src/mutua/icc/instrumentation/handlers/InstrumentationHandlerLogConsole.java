package mutua.icc.instrumentation.handlers;

import mutua.icc.instrumentation.InstrumentableEvent.ELogSeverity;

/** <pre>
 * InstrumentationHandlerLogConsole.java
 * =====================================
 * (created by luiz, Apr 19, 2016)
 *
 * Specialization of {@link InstrumentationHandlerLogPrintStream} to log to 'System.out'.
 * 
 * @version $Id$
 * @author luiz
*/

public class InstrumentationHandlerLogConsole extends InstrumentationHandlerLogPrintStream {

	public InstrumentationHandlerLogConsole(String applicationName, ELogSeverity minimumLogSeverity) {
		super(applicationName, System.out, minimumLogSeverity);
	}

	@Override
	public void close() {
		// do noting -- that is, do not close the System.out stream
	}
	
	

}
