package mutua.icc.instrumentation.handlers;

import java.io.FileOutputStream;
import java.io.PrintStream;

import mutua.icc.instrumentation.InstrumentableEvent.ELogSeverity;

import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZOutputStream;

/** <pre>
 * InstrumentationHandlerLogRotatoryCompressedFile.java
 * ====================================================
 * (created by luiz, Apr 20, 2016)
 *
 * Specialization of {@link InstrumentationHandlerLogRotatoryFile} to write compressed text files
 *
 * @see RelatedClass(es)
 * @version $Id$
 * @author luiz
*/

public class InstrumentationHandlerLogRotatoryCompressedFile extends InstrumentationHandlerLogRotatoryFile {

	public InstrumentationHandlerLogRotatoryCompressedFile(String applicationName, String fsPathPrefix, String fsPathSuffix, ELogSeverity minimumLogSeverity, int rotationFrequency) {
		super(applicationName, fsPathPrefix, fsPathSuffix, minimumLogSeverity, rotationFrequency);

//		// MOVO to Instrumentation on shutdown, close the stream -- this will compress & actually write any previously streamed bytes
//		Runtime.getRuntime().addShutdownHook(new Thread() {
//			public void run() {
//				out.close();
//				out = null;
//			}
//		});
	}

	@Override
	public void closeOldAndOpenNewPrintStream(String fsFilePath) {
		PrintStream newOut = null;
		try {
			FileOutputStream outFile = new FileOutputStream(fsFilePath, true);
			XZOutputStream outXZ = new XZOutputStream(outFile, new LZMA2Options());
			newOut = getPrintStream(outXZ);
		} catch (Throwable t) {
			out.println("Exception while attempting to rotate to a new log file '"+fsFilePath+"'. Aborting rotation.");
			t.printStackTrace(out);
		}
		if ((out != null) && (out != System.out)) {
			out.close();
		}
		out = newOut;
	}
}