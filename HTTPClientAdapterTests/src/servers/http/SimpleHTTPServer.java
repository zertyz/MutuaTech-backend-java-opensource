package servers.http;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpServer;

/** <pre>
 * SimpleHTTPServer.java
 * =====================
 * (created by luiz, Sep 8, 2014)
 *
 * Creates an internal HTTP server for testing purposes
 *
 * @see ISimpleHTTPServerHandler
 * @version $Id$
 * @author luiz
 */

public class SimpleHTTPServer {
	
	private static HttpServer server;
	
	public static boolean doSynchronousLogPrinting = false;
	
	// log := { {(String) uri, (String) query, (long) request_millis, (Hashtable<String, String[]>) requestHeaders}, }
	protected static ArrayList<Object[]> log = null;
	
	public static void start(int port, ISimpleHTTPServerHandler[] handlers) throws IOException {
        
		server = HttpServer.create(new InetSocketAddress(port), 0);
        
        server.setExecutor(null); // creates a default executor

        // register the handlers
        for (ISimpleHTTPServerHandler handler : handlers) {
        	server.createContext(handler.getServedURI(), handler.getHttpHandler());
        }
        
        // restart the logs
        log = new ArrayList<Object[]>();
        
        server.start();
    }
	
	public static void stop() {
        server.stop(0);
        log = null;
	}

	// log requests handled by 'ISimpleHTTPServerHandler' instances
	public static void logRequest(String uri, String query, long millis, Headers requestHeaders) {
		Hashtable<String, String[]> requestHeadersHashtable = null;
		if (requestHeaders != null) {
			// transform 'requestHeaders' into an array of arrays
			requestHeadersHashtable = new Hashtable<String, String[]>();
			for (String headerKey : requestHeaders.keySet()) {
				List<String> headerValues = requestHeaders.get(headerKey);
				requestHeadersHashtable.put(headerKey, headerValues.toArray(new String[headerValues.size()]));
			}
		}
		log.add(new Object[] {uri, query, millis, requestHeadersHashtable});
		if (doSynchronousLogPrinting) {
			printLogs(System.out);
			log.clear();
		}
	}

	public static void printLogs(PrintStream out) {
		for (Object[] logEntry : SimpleHTTPServer.log) {
			String uri                                 = (String) logEntry[0];
			String query                               = (String) logEntry[1];
			long millis                                = (Long) logEntry[2];
			Hashtable<String, String[]> requestHeaders = (Hashtable<String, String[]>) logEntry[3];
			String logLine = millis + " - Access to '"+uri+"' with query '" + query + "' and headers: {";
			if (requestHeaders != null) {
				for (String headerKey : requestHeaders.keySet()) {
					logLine += "'"+headerKey+"': ";
					for (String headerValue : requestHeaders.get(headerKey)) {
						logLine += "'"+headerValue+"',";
					}
					logLine += "; ";
				}
			}
			logLine += "}";
			out.println(logLine);
		}
	}
	
}
