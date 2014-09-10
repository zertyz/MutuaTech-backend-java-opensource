package adapters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Hashtable;

import org.junit.Test;

import servers.http.ISimpleHTTPServerHandler;
import servers.http.SimpleHTTPServer;
import adapters.dto.HTTPRequestDto;
import adapters.dto.HTTPResponseDto;
import adapters.model.URLInfo;

/**
 * HTTPClientAdapterTest.java
 * ==========================
 * (created by luiz, Dec 23, 2011)
 *
 * Validates the implementation of the 'HTTPClientAdapter' class
 *
 * @see HTTPClientAdapter
 * @version $Id$
 * @author luiz
 */

public class HTTPClientAdapterTest {
	

	/***********************
	** NORMAL USAGE TESTS **
	***********************/
	
	@Test
	public void testSimpleGet() throws IOException {
		String response = HTTPClientAdapter.requestGet("http://www.wagemobile.com.br", null, "UTF-8");
		boolean found = response.indexOf("inovadora") != -1;
		assertTrue("Wage Mobile's site seems to have been downloaded incorrectly", found);
	}
	
	@Test
	public void testSimplePost() throws IOException {
		HTTPRequestDto request_data = new HTTPRequestDto();
		request_data.addParameter("user",     "casanova");
		request_data.addParameter("password", "ninhoSMS");
		String response = HTTPClientAdapter.requestPost("http://apache.iw.us.to/Login/Authenticate", request_data, null, "UTF-8");
		boolean found = response.indexOf("CasaNova") != -1;
		assertTrue("Wage Mobile's extranet login form didn't receive a correct post request", found);
	}
	
	@Test
	public void testHeaders() throws IOException {
		// google currently only performs queries for known browsers 
		HTTPRequestDto request_data = new HTTPRequestDto();
		request_data.addParameter("q", "zertyz' blog");
		request_data.addHeader("User-Agent", "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.4) Gecko/2008102920 Firefox/3.0.4");
		String response = HTTPClientAdapter.requestGet("http://www.google.com.br/search", request_data, "UTF-8");
		boolean found = response.indexOf("http://sites.google.com/site/zertyzblog") != -1;
		assertTrue("Wage Mobile's site wasn't found among google search results", found);
	}
	
	@Test
	public void AuthenticationURLDetectionTest() {
		
		Object[][] validURLs = {	// := {{URL, URLInfo}, ... }
			{"http://user1:pass1@www.host.com",    new URLInfo("http://www.host.com",    "user1", "pass1")},
			{"http://www.host.com",                new URLInfo("http://www.host.com")},
			{"http://user2:pass2@172.21.0.29/",    new URLInfo("http://172.21.0.29/",    "user2", "pass2")},
			{"http://m.someplace:80/s?a=1",        new URLInfo("http://m.someplace:80/s?a=1")},
			{"http://user3:pass3@m.someplace:81/", new URLInfo("http://m.someplace:81/", "user3", "pass3")},
		};
		
		String[] invalidURLs = {
			"http://@192.168.0.1",
			"http://user:pass:host.com/",
			"http://onlyuserorpass@host.com/"
		};
		
		// test valid URLs
		for (int i=0; i<validURLs.length; i++) {
			String URL = (String)validURLs[i][0];
			URLInfo expectedURLInfo = (URLInfo)validURLs[i][1];
			URLInfo observedURLInfo = HTTPClientAdapter.parseURLInfo(URL);
			
			assertEquals("'connectableURL' does not match",        expectedURLInfo.getConnectableURL(), observedURLInfo.getConnectableURL());
			assertEquals("'hasAuthenticationData' does not match", expectedURLInfo.hasAuthenticationData(), observedURLInfo.hasAuthenticationData());
			assertEquals("'authUser' does not match", expectedURLInfo.getAuthUser(), observedURLInfo.getAuthUser());
			assertEquals("'authPass' does not match", expectedURLInfo.getAuthPass(), observedURLInfo.getAuthPass());
		}
		
		// test "invalid" URLs
		for (int i=0; i<invalidURLs.length; i++) {
			String URL = (String)invalidURLs[i];
			URLInfo observedURLInfo = HTTPClientAdapter.parseURLInfo(URL);
			
			assertEquals("'connectableURL' does not match",        URL,   observedURLInfo.getConnectableURL());
			assertFalse("'hasAuthenticationData' should be false", observedURLInfo.hasAuthenticationData());
		}
		
	}
	
	@Test
	public void localServerTest() throws IOException, InterruptedException {
		
		// setup the server
		SimpleHTTPServer.start(8080, new ISimpleHTTPServerHandler[] {
			ISimpleHTTPServerHandler.getStaticHandler("/test", new String[][] {{"myheader",  "myvalue1"}, {"myheader", "myvalue2"}}, "This is the response".getBytes()),
		});
		String url = "http://localhost:8080/test";
		
		//Thread.sleep(60000);
		
		// setup the client
		HTTPRequestDto request = new HTTPRequestDto();
		request.addHeader("h1", "h1v1");
		request.addHeader("h2", "h2v1");
		request.addHeader("h1", "h1v2");
		HTTPResponseDto response = HTTPClientAdapter.requestGet(url, request);
		
		System.out.println(request);
		System.out.println(response);
		
		// request log
		for (Object[] logEntry : SimpleHTTPServer.log) {
			String uri                                 = (String) logEntry[0];
			long millis                                = (Long) logEntry[1];
			Hashtable<String, String[]> requestHeaders = (Hashtable<String, String[]>) logEntry[2];
			String logLine = millis + " - Access to '"+uri+"' with headers: {";
			for (String headerKey : requestHeaders.keySet()) {
				logLine += "'"+headerKey+"': ";
				for (String headerValue : requestHeaders.get(headerKey)) {
					logLine += "'"+headerValue+"',";
				}
				logLine += "; ";
			}
			logLine += "}";
			System.out.println(logLine);
		}
		
		SimpleHTTPServer.stop();
	}
	

	/**************************
	** ERROR CONDITION TESTS **
	**************************/
	
	@Test(expected=SocketTimeoutException.class)
	public void testConnectionTimeout() throws IOException {
		HTTPClientAdapter.CONNECTION_TIMEOUT = 10;
		HTTPClientAdapter.requestGet("http://gizmodo.com/5041005/earths-most-distant-web-cam-pics-went-live-this-week", null);
	}
	
	@Test(expected=SocketTimeoutException.class)
	public void testReadTimeout() throws IOException {
		HTTPClientAdapter.READ_TIMEOUT = 10;
		HTTPClientAdapter.requestGet("http://mail.mundivox.com:25", null);
	}

}
