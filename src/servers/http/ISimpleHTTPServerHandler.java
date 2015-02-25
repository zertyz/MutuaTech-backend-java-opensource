package servers.http;

import java.io.IOException;
import java.io.OutputStream;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/** <pre>
 * ISimpleHTTPServerHandler.java
 * =============================
 * (created by luiz, Sep 9, 2014)
 *
 * Contract to be used by instances serving SimpleHTTPServer contexts
 *
 * @see SimpleHTTPServer
 * @version $Id$
 * @author luiz
 */

public abstract class ISimpleHTTPServerHandler {

	
	/**********************
	** INTERFACE METHODS **
	**********************/
	

	/**
	 * Returns the path that will be recognized and served by this SimpleHTTPServerHandler
	 * @return the served URI (served path), in the form "/dir1/dir2/resource"
	 */
	public abstract String getServedURI();
	
	
	/**
	 * Returns the headers to send along this SimpleHTTPServerHandler response
	 * @return := { {header_name, header_value}, ... }
	 */
	public abstract String[][] getHeaders();
	

	/**
	 * Returns the contents to send along this SimpleHTTPServerHandler response
	 * @return the byte array corresponding to the contents to be downloaded by the client
	 */
	public abstract byte[] getResponse();
	
	
	
	/*************************
	** SIMPLE USAGE HELPERS **
	*************************/

	/**
	 * Creates and returns an ordinary SimpleHTTPServerHandler, whose intention is to serve static content
	 * @param servedURI see getServedURI()
	 * @param headers	see getHeaders()
	 * @param response	see getResponse()
	 * @return
	 */
	public static ISimpleHTTPServerHandler getStaticHandler(final String servedURI, final String[][] headers, final byte[] response) {
		return new ISimpleHTTPServerHandler() {
			@Override
			public String getServedURI() {
				return servedURI;
			}
			@Override
			public String[][] getHeaders() {
				return headers;
			}
			@Override
			public byte[] getResponse() {
				return response;
			}
		};
	}
	
	
	/************************************
	** SimpleHTTPServer HELPER METHODS **
	************************************/
	
	/**
	 * This handler is implemented according to 'HttpServer'.createContext and should not be overridden
	 * @return
	 */
	public HttpHandler getHttpHandler() {
		return new HttpHandler() {
	        public void handle(HttpExchange t) throws IOException {
	        	SimpleHTTPServer.logRequest(t.getHttpContext().getPath(), t.getRequestURI().getRawQuery(), System.currentTimeMillis(), t.getRequestHeaders());
	        	String[][] headers = getHeaders();
	        	if (headers != null) {
		        	for (String[] header : getHeaders()) {
		        		String headerKey   = header[0];
		        		String headerValue = header[1];
			            t.getResponseHeaders().add(headerKey, headerValue);
		        	}
	        	}
	        	byte[] response = getResponse();
	            t.sendResponseHeaders(200, response.length);
	            OutputStream os = t.getResponseBody();
	            os.write(response);
	            os.close();
	        }
		};
	}


}
