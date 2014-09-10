package servers.http;

import java.io.IOException;
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
	
	// log := { {(String) uri, (long) request_millis, (Hashtable<String, String[]>) requestHeaders}, }
	public static ArrayList<Object[]> log = null;
	
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
	protected static void logRequest(String uri, long millis, Headers requestHeaders) {
		// transform 'requestHeaders' into an array of arrays
		Hashtable<String, String[]> requestHeadersHashtable = new Hashtable<String, String[]>();
		for (String headerKey : requestHeaders.keySet()) {
			List<String> headerValues = requestHeaders.get(headerKey);
			requestHeadersHashtable.put(headerKey, headerValues.toArray(new String[headerValues.size()]));
		}
		log.add(new Object[] {uri, millis, requestHeadersHashtable});
	}
}
