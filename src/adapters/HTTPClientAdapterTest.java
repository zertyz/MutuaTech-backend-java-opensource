package adapters;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.util.List;

import javax.swing.text.StyleConstants.CharacterConstants;

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
		String response = HTTPClientAdapter.requestGet("http://instantvas.com", null, "UTF-8");
		boolean found = response.indexOf("Instant VAS") != -1;
		assertTrue("Instant VAS' site seems to have been downloaded incorrectly", found);
	}
	
	@Test
	public void testSimplePost() throws IOException {
		HTTPRequestDto request_data = new HTTPRequestDto();
		request_data.addParameter("log",         "zertyz");
		request_data.addParameter("pwd",         "PicaPau1");
		request_data.addParameter("testcookie",  "1");
		request_data.addParameter("redirect_to", "http://dominandoriscos.com.br/wp-admin/");
		String response = HTTPClientAdapter.requestPost("http://dominandoriscos.com.br/wp-login.php", request_data, null, "UTF-8");
		boolean found = response.indexOf("Cookies are blocked or not supported by your browser.") != -1;
		assertTrue("Word Press' login form didn't receive a correct post request", found);
	}
	
	@Test
	public void testHeaders() throws IOException {
		// google currently only performs queries for known browsers 
		HTTPRequestDto request_data = new HTTPRequestDto();
		request_data.addParameter("q", "zertyz' blog");
		request_data.addHeader("User-Agent", "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.4) Gecko/2008102920 Firefox/3.0.4");
		String response = HTTPClientAdapter.requestGet("http://www.google.com.br/search", request_data, "UTF-8");
		boolean found = response.indexOf("https://sites.google.com/site/zertyzblog") != -1;
		assertTrue("zertyz' blog wasn't found among google search results", found);
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
		SimpleHTTPServer.start(8888, new ISimpleHTTPServerHandler[] {
			ISimpleHTTPServerHandler.getStaticHandler("/test", new String[][] {{"myheader",  "myvalue1"}, {"myheader", "myvalue2"}}, "This is the response".getBytes()),
		});
		String url = "http://localhost:8888/test";
		
		//Thread.sleep(60000);
		
		// setup the client
		HTTPRequestDto request = new HTTPRequestDto();
		request.addHeader("h1", "h1v1");
		request.addHeader("h2", "h2v1");
		request.addHeader("h1", "h1v2");

		// TODO it is not possible to encode binary data due to urlencode also reencoding it with our fixed utf-8 format. how to fix this shit?
		//for (String charsetName : Charset.availableCharsets().keySet()) {
		//	System.out.println(charsetName);
		//}
		//request.addParameter("bytes", new String(new byte[] {1, 2, 3, 4, 5, 126, 127, (byte)128, /*(byte)128, (byte)129, (byte)251, (byte)252, (byte)253, (byte)254, (byte)255*/}, "ISO-8859-1"));
		//request.addParameter("bytes", new String(new char[] {1, 2, 3, 4, 5, 126, 127, Char, /*(byte)128, (byte)129, (byte)251, (byte)252, (byte)253, (byte)254, (byte)255*/}));
		request.addEncodedParameter("bytes", "%00%01%02%7F%80%81%FD%FE%FF");
		
		HTTPResponseDto response = HTTPClientAdapter.requestGet(url, request);
		
		System.out.println(request);
		System.out.println(response);
		
		// request log
		SimpleHTTPServer.printLogs(System.out);
		
		SimpleHTTPServer.stop();
	}
	
	@Test(expected=FileNotFoundException.class)
	public void testNotFoundResponse() throws IOException {
		// try to fetch a not to be found item
		String notToBeFoundURL = "http://google.com/nonexistentresource";
		HTTPResponseDto notFoundResponse = HTTPClientAdapter.requestGet(notToBeFoundURL, null);
		
		System.out.println(notFoundResponse);

	}
	

	@Test
	public void testRedirectionResponse() throws IOException {
		// try to fetch a URL that will respond as a redirection for another URL
		boolean originalFollowRedirects = HTTPClientAdapter.FOLLOW_REDIRECTS;
		try {
			HTTPClientAdapter.FOLLOW_REDIRECTS = false;
			String toBeRedirectedURL = "http://google.com";	// will be redirected to http://google.com.br/ throw HTTP 302 Found response
			String expectedRedirectionPattern = "http://www.google.com.br/.*";
			HTTPResponseDto toBeRedirectedResponse = HTTPClientAdapter.requestGet(toBeRedirectedURL, null);
			List<String> observedRedirections = toBeRedirectedResponse.getHeaders().get("Location");
			assertNotNull("No redirection detected", observedRedirections);
			String observedRedirection = observedRedirections.get(0);
			assertTrue("expected redirection pattern '"+expectedRedirectionPattern+"' is not contained into observed redirection string '"+observedRedirection+"'", "matched".equals(observedRedirection.replaceAll(expectedRedirectionPattern, "matched")));
			
			System.out.println(toBeRedirectedURL + " - " + toBeRedirectedResponse);
		} finally {
			HTTPClientAdapter.FOLLOW_REDIRECTS = originalFollowRedirects;
		}

	}

	/**************************
	** ERROR CONDITION TESTS **
	**************************/
	
	@Test(expected=SocketTimeoutException.class)
	public void testConnectionTimeout() throws IOException {
		int originalTimeout = HTTPClientAdapter.CONNECTION_TIMEOUT;
		try {
			HTTPClientAdapter.CONNECTION_TIMEOUT = 10;
			HTTPClientAdapter.requestGet("http://gizmodo.com/5041005/earths-most-distant-web-cam-pics-went-live-this-week", null);
		} finally {
			HTTPClientAdapter.CONNECTION_TIMEOUT = originalTimeout;
		}
	}
	
	@Test(expected=SocketTimeoutException.class)
	public void testReadTimeout() throws IOException {
		int originalTimeout = HTTPClientAdapter.READ_TIMEOUT;
		try {
			HTTPClientAdapter.READ_TIMEOUT = 10;
			HTTPClientAdapter.requestGet("http://gizmodo.com/5041005/earths-most-distant-web-cam-pics-went-live-this-week", null);
		} finally {
			HTTPClientAdapter.READ_TIMEOUT = originalTimeout;
		}
	}

}
