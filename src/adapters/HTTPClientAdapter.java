package adapters;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import adapters.dto.HTTPRequestDto;
import adapters.model.URLInfo;

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
 * @see HTTPRequestDto
 * @version $Id$
 * @author luiz
 */

public class HTTPClientAdapter {
	
	/******************
	** CONFIGURATION **
	******************/
	
	protected static int CONNECTION_TIMEOUT = 30000;
	protected static int READ_TIMEOUT       = 30000;
	
	
	/*********************
	** AUXILIAR METHODS **
	*********************/
	
	// build the so called "query string" from the request parameters
	protected static String buildParameterString(HTTPRequestDto requestData) throws UnsupportedEncodingException {

		if (requestData == null) {
			return "";
		}
		
		String queryString = "";		
		Enumeration<String> parameters = requestData.getParametersEnumeration();
		
		while (parameters.hasMoreElements()) {
			String parameter = parameters.nextElement();
			String value = requestData.getParameter(parameter);
			queryString += URLEncoder.encode(parameter, "UTF-8") + "=" + URLEncoder.encode(value, "UTF-8");
			if (parameters.hasMoreElements()) {
				queryString += "&";
			}
		}
		
		return queryString;
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
	
	private static String urlWithAuthInfoRegex    = "http://([^:@/]*):([^@]*)@(.*)";
	private static Pattern pattern = Pattern.compile(urlWithAuthInfoRegex);
	protected static URLInfo parseURLInfo(String url) {
		Matcher matcher = pattern.matcher(url);
		if (matcher.matches()) {
			String authUser = matcher.group(1);
			String authPass = matcher.group(2);
			String uri      = matcher.group(3);
			return new URLInfo("http://"+uri, authUser, authPass);
		} else {
			return new URLInfo(url);
		}
	}
	
	// build a connection object suitable for performing the desired http action
	protected static HttpURLConnection buildConnection(String canonicalUrl, boolean doPost, HTTPRequestDto requestData) throws IOException {
		URLInfo urlInfo = parseURLInfo(canonicalUrl);
        URL url = new URL(urlInfo.getConnectableURL());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        // process authentication data (if any)
        if (urlInfo.hasAuthenticationData()) {
	        // Set header "Authorization"
	        String credentials = urlInfo.getAuthUser() + ":" + urlInfo.getAuthPass();
	        throw new Error("Please correct the code above -- which used to work as of Dec, 2011");
//	        String encodedCredentials = new sun.misc.BASE64Encoder().encode(credentials.getBytes());
//	        connection.setRequestProperty("Authorization", "Basic " + encodedCredentials);
        }
        
        connection.setConnectTimeout(CONNECTION_TIMEOUT);
        connection.setReadTimeout(READ_TIMEOUT);
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

	// read the answer (contents) for the request
	private static byte[] getContents(HttpURLConnection connection) throws IOException {
		InputStream in = connection.getInputStream();
		ArrayList<byte[]> chunks = new ArrayList<byte[]>();
        byte[] buffer = new byte[1024];
        int totalBytes = 0;
        while (true) {
        	int bytesRead = in.read(buffer);
        	if (bytesRead == -1) {
        		break;
        	}
        	if (bytesRead > 0) {
	            byte[] chunk = new byte[bytesRead];
	            System.arraycopy(buffer, 0, chunk, 0, bytesRead);
	        	chunks.add(chunk);
	        	totalBytes += bytesRead;
        	}
        }
        in.close();
        byte[] contentBytes = new byte[totalBytes];
        int copiedBytes = 0;
        for (int i=0; i<chunks.size(); i++) {
        	byte[] chunk = chunks.get(i);
        	int chunkLength = chunk.length;
        	System.arraycopy(chunk, 0, contentBytes, copiedBytes, chunkLength);
        	copiedBytes += chunkLength;
        }
        return contentBytes;
	}

	
	/*******************
	** PUBLIC METHODS **
	*******************/

	/**
	 *  perform the http request via the get method
	 * @throws IOException
	 */
	public static byte[] requestGet(String url, HTTPRequestDto requestData) throws IOException {
		String queryString = buildParameterString(requestData);
		HttpURLConnection connection = buildConnection(url+(queryString.equals("") ? "": "?")+queryString, false, requestData);
        byte[] contents = getContents(connection);
		return contents;
	}
	
	public static String requestGet(String url, HTTPRequestDto requestData, String charsetName) throws IOException {
		return new String(requestGet(url, requestData), charsetName);
	}
	
	/**
	 * perform the http request via the post method
	 * if 'outputData' is provided, it will be copied to the output stream and any parameters on 'requestData' will be ignored
	 * if 'outputData' is null, the parameters contained in 'requestData' will be sent to the output stream
	 * @throws IOException
	 */
	public static byte[] requestPost(String url, HTTPRequestDto requestData, byte[] outputData) throws IOException {
		if (outputData == null) {
			outputData = buildParameterString(requestData).getBytes();
		}
		HttpURLConnection connection = buildConnection(url, true, requestData);

        // post the queryString
        OutputStream out = connection.getOutputStream();
        out.write(outputData);
        out.close();
        
        byte[] contents = getContents(connection);
        return contents;
	}
	
	public static String requestPost(String url, HTTPRequestDto requestData, byte[] outputData, String charsetName) throws IOException {
		return new String(requestPost(url, requestData, outputData), charsetName);
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
