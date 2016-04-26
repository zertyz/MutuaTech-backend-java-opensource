package mutua.icc.instrumentation.handlers;

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

	public InstrumentationHandlerLogConsole(String applicationName, int minimumLogLevel) {
		super(applicationName, System.out, minimumLogLevel);
	}

}
