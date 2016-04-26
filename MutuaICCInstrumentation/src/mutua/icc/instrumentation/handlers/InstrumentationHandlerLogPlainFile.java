package mutua.icc.instrumentation.handlers;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;

/** <pre>
 * InstrumentationHandlerLogPlainFile.java
 * =======================================
 * (created by luiz, Apr 20, 2016)
 *
 * Specialization of {@link InstrumentationHandlerLogPrintStream} to log to a normal, plain, fs file.
 *
 * @version $Id$
 * @author luiz
*/

public class InstrumentationHandlerLogPlainFile extends InstrumentationHandlerLogPrintStream {

	public InstrumentationHandlerLogPlainFile(String applicationName, String fsFilePath, int minimumLogLevel) throws UnsupportedEncodingException, FileNotFoundException {
		super(applicationName, new FileOutputStream(fsFilePath, true), minimumLogLevel);
	}

}
