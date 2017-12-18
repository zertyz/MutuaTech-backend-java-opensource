package adapters;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Properties;

import adapters.dto.HTTPRequestDto;
import adapters.dto.HTTPResponseDto;

/**
 * HTTPClientAdapter.java
 * ======================
 * (created by luiz, Dec 9, 2008)
 *
 * Through the Adapter Pattern (http://c2.com/cgi/wiki?AdapterPattern), allows applications
 * to decouple from the somewhat complicated original HTTP API.
 * 
 * Features:
 * 	- get
 * 	- post
 * 	- get + post
 * 	- authentication (for URLs in the form http://user:pass@host...)
 * 	- connection & read timeouts
 * 
 * Follows the "Mutua Configurable Module" pattern.
 *
 * @see HTTPRequestDto
 * @version $Id$
 * @author luiz
 */

public class HTTPClientAdapter {
	
	// default values for new instances
	private static int      CONNECTION_TIMEOUT_MILLIS;
	private static int      READ_TIMEOUT_MILLIS;
	private static boolean  FOLLOW_REDIRECTS;
	private static String[] DEFAULT_HEADERS;
	
	// instance variables
	private final int      connectionTimeoutMillis;
	private final int      readTimeoutMillis;
	private final boolean  followRedirects;
	private final String[] defaultHeaders;
	private final String   baseUrl;
	
	/**************************
	** CONFIGURATION METHODS **
	**************************/
	
	/** method to be called when attempting to configure the default behavior of new instances of this class.<pre>
	 *  @param connectionTimeutMillis     the maximum time to wait for a connection to be established -- or -1 to not touch the value
	 *  @param readTimeoutMillis          the maximum time to wait for another by to arrive before dropping the connection -- pass -1 not to mess with this also
	 *  @param followRedirects            whether or not to follow (make a new request) the header "Location"
	 *  @param defaultHeaders             := {header1, value1, header2, value2, ...} -- the fixed headers to be prepended to each request */
	public static void configureDefaultValuesForNewInstances(int connectionTimeutMillis,
	                                                         int readTimeoutMillis,
	                                                         boolean followRedirects,
	                                                         String... defaultHeaders) {
		
		// assure a global jvm behavior
		Properties p = System.getProperties();
		p.setProperty("http.keepAlive",                           "true");
		p.setProperty("http.maxConnections",                      "30");
		p.setProperty("sun.net.http.errorstream.enableBuffering", "false");
		
		CONNECTION_TIMEOUT_MILLIS = connectionTimeutMillis != -1 ? connectionTimeutMillis : CONNECTION_TIMEOUT_MILLIS;
		READ_TIMEOUT_MILLIS       = readTimeoutMillis      != -1 ? readTimeoutMillis      : READ_TIMEOUT_MILLIS;
		FOLLOW_REDIRECTS          = followRedirects;
		DEFAULT_HEADERS           = defaultHeaders;
	}

	static {
		configureDefaultValuesForNewInstances(30000, 30000, false, "User-Agent", "MutuaTech.com HTTPClientAdapter");
	}
	
	/** this constructor should be used when repetitive requests to the same 'baseURL' are issued -- <protocol>://[auth@]<host>[:port]</path>
	 *  in this scenario, the instance method 'requestGet' & 'requestPost' should be used */ 
	public HTTPClientAdapter(String baseUrl) {
		
		this.connectionTimeoutMillis = CONNECTION_TIMEOUT_MILLIS;
		this.readTimeoutMillis       = READ_TIMEOUT_MILLIS;
		this.followRedirects         = FOLLOW_REDIRECTS;
		this.defaultHeaders          = DEFAULT_HEADERS;
		
//// NOT WORKING AS OF DEC, 2011
//		if (baseUrl != null) {
//			URLInfo urlInfo = parseURLInfo(baseURL);
//			url = new URL(urlInfo.getConnectableURL());
//		} else {
//			this.baseUrl = null;
//		}
		
		this.baseUrl = baseUrl;
	}
	
	/** for arbitrary URLs, which will be provided on each request */ 
	public HTTPClientAdapter() {
		this(null);
	}
	
	/** Apply the default headers to the connection, prior to making the request */
	private void applyHeaders(HttpURLConnection connection, String... headers) {
		for (int i=0; i<headers.length; i+=2) {
			String headerName  = headers[i];
			String headerValue = headers[i+1];
			connection.addRequestProperty(headerName, headerValue);
		}
	}
	
	/** Reads all data available and closes the stream 'is' afterwords */
	private static byte[] fullyReadAndCloseStream(InputStream is) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
        byte[] buffer = new byte[1024];
//        int totalBytes = 0;
        while (true) {
        	int bytesRead = is.read(buffer);
        	if (bytesRead == -1) {
        		break;
        	}
        	if (bytesRead > 0) {
	            baos.write(buffer, 0, bytesRead);
//	        	totalBytes += bytesRead;
        	}
        }
        is.close();
		return baos.toByteArray();
	}
	
	/** Build a connection object suitable for performing the desired HTTP action */
	private HttpURLConnection buildConnection(String serviceUrl, boolean doPost) throws IOException {
		URL url = new URL(serviceUrl);
        HttpURLConnection.setFollowRedirects(followRedirects);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
	
		try {
	        connection.setConnectTimeout(connectionTimeoutMillis);
	        connection.setReadTimeout(readTimeoutMillis);
	        
	        if (doPost) {
	        	connection.setDoOutput(true);
	        	connection.setRequestMethod("POST");
	        } else {
	        	connection.setDoOutput(false);
	        	connection.setRequestMethod("GET");
	        }
	        applyHeaders(connection, defaultHeaders);
	        connection.connect();
		} catch (IOException e) {
			// in case of an exception, assures the connection is left in a state suitable for persistent (Keep-Alive) reusage
			// and rethrows the exception
			if (connection != null) {
				try {
					fullyReadAndCloseStream(connection.getErrorStream());
				} catch (Throwable t) {}
			}
			throw e;
		}
		return connection;
	}

	/** URLEncode (in place) parameter values, where:<pre>
	 *  parameters := {parameterName1, parameterValue1, parameterName2, parameterValue2, ...} */
	public String[] encodeParameterValues(String... parameters) throws UnsupportedEncodingException {
		for (int i=1; i<parameters.length; i+=2) {
			parameters[i] = URLEncoder.encode(parameters[i], "UTF-8");
		}
		return parameters;
	}
	
	/** Builds the string of parameters names and values that can be sent either via GET or POST.<pre>
	 *  parameters := {parameterName1, parameterValue1, parameterName2, parameterValue2, ...} */
	private String buildParameterString(String... parameters) {
		StringBuffer parameterString = new StringBuffer(parameters.length*16);
		for (int i=0; i<parameters.length; i+=2) {
			String parameterName  = parameters[i];
			String parameterValue = parameters[i+1];
			if (i > 0) {
				parameterString.append('&');
			}
			parameterString.append(parameterName).append('=').append(parameterValue);
		}
		return parameterString.toString();
	}
	
	/** Retrieves a connection for a GET method -- which should not be closed to honor the Keep-Alive implementation (we must only close input & error streams) */
	private HttpURLConnection buildGetConnection(boolean shouldEncodeParameterValues, String... parameters) throws IOException {
		if ((parameters == null) || (parameters.length == 0)) {
			return buildConnection(baseUrl, false);
		} else {
			if (shouldEncodeParameterValues) {
				encodeParameterValues(parameters);
			}
			String queryString = buildParameterString(parameters);
			return buildConnection(baseUrl + "?" + queryString, false);
		}
	}
	
	// the following GET method requests honors the HTTP/1.1 Keep-Alive feature
	
	/** Performs an HTTP request to 'baseUrl' via the GET method, encoding all parameter values and building the 'ResponseDto' object.<pre>
	 *  @param parameters := {parameterName1, parameterValue1, parameterName2, parameterValue2, ...} */
	public HTTPResponseDto requestGetWithResponse(String... parameters) throws IOException {
		HttpURLConnection connection = buildGetConnection(true, parameters);
		return buildResponseDto(connection);
	}
	
	/** Similar to {@link #requestGetWithResponse(String[])}, but do not encode any parameter values */
	public HTTPResponseDto requestGetWithAlreadyEncodedValuesWithResponse(String... parameters) throws IOException {
		HttpURLConnection connection = buildGetConnection(false, parameters);
		return buildResponseDto(connection);
	}
	
	/** Performs an HTTP request to 'baseUrl' via the GET method, encoding all parameter values and returning the response as a String.<pre>
	 *  @param parameters := {parameterName1, parameterValue1, parameterName2, parameterValue2, ...} */
	public String requestGet(String... parameters) throws IOException {
		HttpURLConnection connection = buildGetConnection(true, parameters);
		return new String(fullyReadAndCloseStream(connection.getInputStream()), "UTF-8");
	}

	/** Similar to {@link #requestGet(String[])}, but do not encode any parameter values */
	public String requestGetWithAlreadyEncodedValues(String... parameters) throws IOException {
		HttpURLConnection connection = buildGetConnection(false, parameters);
		return new String(fullyReadAndCloseStream(connection.getInputStream()), "UTF-8");
	}
	
	/*********************
	** AUXILIAR METHODS **
	*********************/
	
	// build the so called "query string" from the request parameters
	protected static String buildParameterString(HTTPRequestDto requestData) throws UnsupportedEncodingException {

		if (requestData == null) {
			return "";
		}
		
		StringBuffer queryString = new StringBuffer();
		Enumeration<String> encodedParameters = requestData.getEncodedParametersEnumeration();		
		Enumeration<String> parameters = requestData.getParametersEnumeration();		

		// add already encoded parameters
		while (encodedParameters.hasMoreElements()) {
			String encodedParameter = encodedParameters.nextElement();
			String encodedValue     = requestData.getEncodedParameter(encodedParameter);
			queryString.append(encodedParameter).
			            append('=').
			            append(encodedValue);
			if (encodedParameters.hasMoreElements() || parameters.hasMoreElements()) {
				queryString.append('&');
			}
		}
		
		
		// add parameters that need to be url encoded
		while (parameters.hasMoreElements()) {
			String parameter = parameters.nextElement();
			String value     = requestData.getParameter(parameter);
			queryString.append(URLEncoder.encode(parameter, "UTF-8")).
			            append('=').
			            append(URLEncoder.encode(value, "UTF-8"));
			if (parameters.hasMoreElements()) {
				queryString.append('&');
			}
		}
		
		return queryString.toString();
	}
	
	// append the request headers to the http request
	protected static void applyHeaders(HTTPRequestDto requestData, HttpURLConnection connection) {
		if (requestData == null) {
			return;
		}
		Enumeration<String> headers = requestData.getHeadersEnumeration();
		while (headers.hasMoreElements()) {
			String header = headers.nextElement();
			String value = requestData.getHeader(header);
			connection.addRequestProperty(header, value);
		}
	}
	
// NOT WORKING AS OF DEC, 2011
//	private static String urlWithAuthInfoRegex    = "http://([^:@/]*):([^@]*)@(.*)";
//	private static Pattern pattern = Pattern.compile(urlWithAuthInfoRegex);
//	protected static URLInfo parseURLInfo(String url) {
//		Matcher matcher = pattern.matcher(url);
//		if (matcher.matches()) {
//			String authUser = matcher.group(1);
//			String authPass = matcher.group(2);
//			String uri      = matcher.group(3);
//			return new URLInfo("http://"+uri, authUser, authPass);
//		} else {
//			return new URLInfo(url);
//		}
//	}
	
	/** build a connection object suitable for performing the desired HTTP action */
	protected static HttpURLConnection buildConnection(String canonicalUrl, boolean doPost, HTTPRequestDto requestData) throws IOException {
//// NOT WORKING AS OF DEC, 2011
//		URLInfo urlInfo = parseURLInfo(canonicalUrl);
//        URL url = new URL(urlInfo.getConnectableURL());
		URL url = new URL(canonicalUrl);
        HttpURLConnection.setFollowRedirects(FOLLOW_REDIRECTS);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

// NOT WORKING AS OF DEC, 2011
//        
//        // process authentication data (if any)
//        if (urlInfo.hasAuthenticationData()) {
//	        // Set header "Authorization"
//	        String credentials = urlInfo.getAuthUser() + ":" + urlInfo.getAuthPass();
//	        throw new Error("Please correct the code above -- which used to work as of Dec, 2011");
////	        String encodedCredentials = new sun.misc.BASE64Encoder().encode(credentials.getBytes());
////	        connection.setRequestProperty("Authorization", "Basic " + encodedCredentials);
//        }
        
        connection.setConnectTimeout(CONNECTION_TIMEOUT_MILLIS);
        connection.setReadTimeout(READ_TIMEOUT_MILLIS);
        if (doPost) {
        	connection.setDoOutput(true);
        	connection.setRequestMethod("POST");
        } else {
        	connection.setDoOutput(false);
        	connection.setRequestMethod("GET");
        }
        applyHeaders(requestData, connection);
        connection.connect();
	    return connection;
	}

	private static HTTPResponseDto buildResponseDto(HttpURLConnection connection) throws IOException {
		byte[] contents = fullyReadAndCloseStream(connection.getInputStream());
		String responseMessage = connection.getResponseCode() + " " + connection.getResponseMessage();
		HTTPResponseDto response = new HTTPResponseDto(responseMessage, contents);
		// assemble headers
		int n = 1;
		while (true) {
			String headerKey = connection.getHeaderFieldKey(n);
			if (headerKey == null) {
				break;
			}
			String headerValue = connection.getHeaderField(n);
			response.addHeader(headerKey, headerValue);
			n++;
		}
		return response;
	}

	
	/*******************
	** PUBLIC METHODS **
	*******************/

	/**
	 *  perform the http request via the get method
	 * @throws IOException
	 */
	public static HTTPResponseDto requestGet(String url, HTTPRequestDto requestData) throws IOException {
		String queryString = buildParameterString(requestData);
		HttpURLConnection connection = buildConnection(url+(queryString.equals("") ? "": "?")+queryString, false, requestData);
        HTTPResponseDto response = buildResponseDto(connection);
		return response;
	}
	
	public static String requestGet(String url, HTTPRequestDto requestData, String charsetName) throws IOException {
		return new String(requestGet(url, requestData).getContents(), charsetName);
	}
	
	/**
	 * perform the http request via the post method
	 * if 'outputData' is provided, it will be copied to the output stream and any parameters on 'requestData' will be ignored
	 * if 'outputData' is null, the parameters contained in 'requestData' will be sent to the output stream
	 * @throws IOException
	 */
	public static HTTPResponseDto requestPost(String url, HTTPRequestDto requestData, byte[] outputData) throws IOException {
		if (outputData == null) {
			outputData = buildParameterString(requestData).getBytes();
		}
		HttpURLConnection connection = buildConnection(url, true, requestData);

        // post the queryString or 'outputData'
        OutputStream out = connection.getOutputStream();
        out.write(outputData);
        out.close();

        HTTPResponseDto response = buildResponseDto(connection);

        return response;
	}
	
	public static String requestPost(String url, HTTPRequestDto requestData, byte[] outputData, String charsetName) throws IOException {
		return new String(requestPost(url, requestData, outputData).getContents(), charsetName);
	}
	
	/**
	 * return the get URL
	 * @throws UnsupportedEncodingException
	 */
	public static String toString(String url, HTTPRequestDto requestData) throws UnsupportedEncodingException {
		String queryString = buildParameterString(requestData);
		return url+(queryString.equals("") ? "": "?")+queryString;
	}

}
