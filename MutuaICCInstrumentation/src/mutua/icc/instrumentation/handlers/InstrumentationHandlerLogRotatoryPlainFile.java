package mutua.icc.instrumentation.handlers;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

/** <pre>
 * InstrumentationHandlerLogRotatoryPlainFile.java
 * ===============================================
 * (created by luiz, Apr 20, 2016)
 *
 * Specialization of {@link InstrumentationHandlerLogRotatoryFile} to write plain text files
 *
 * @version $Id$
 * @author luiz
*/

public class InstrumentationHandlerLogRotatoryPlainFile extends InstrumentationHandlerLogRotatoryFile {

	public InstrumentationHandlerLogRotatoryPlainFile(String applicationName, String fsPathPrefix, String fsPathSuffix, int minimumLogLevel, int rotationFrequency) {
		super(applicationName, fsPathPrefix, fsPathSuffix, minimumLogLevel, rotationFrequency);
	}

	@Override
	public void closeOldAndOpenNewPrintStream(String fsFilePath) {
		PrintStream newOut = null;
		try {
			newOut = getPrintStream(new FileOutputStream(fsFilePath, true));
		} catch (FileNotFoundException e) {
			out.println("Exception while attempting to rotate to a new log file '"+fsFilePath+"'. Aborting rotation.");
			e.printStackTrace(out);
		}
		if ((out != null) && (out != System.out)) {
			out.close();
		}
		out = newOut;
	}

}
