package adapters.model;

/**
 * URLInfo.java
 * ============
 * (created by luiz, Dec 23, 2011)
 *
 * Represents the information associated to an URL, differentiating between a
 * "full URL" and a "connectable URL"
 *
 * @see RelatedClass(es)
 * @version $Id$
 * @author luiz
 */

public class URLInfo {
	
	private String connectableURL;
	private boolean hasAuthenticationData;
	private String authUser;
	private String authPass;

	/**
	 * Creates a representation of a standard and simple URL (no authentication data associated to it) 
	 */
	public URLInfo(String url) {
		this.connectableURL        = url;
		this.hasAuthenticationData = false;
	}
	
	/**
	 * Creates a representation of an URL which has authentication data associated to it (urls in the form http://user:pass@host/...)
	 */
	public URLInfo(String connectableURL, String authUser, String authPass) {
		this.connectableURL        = connectableURL;
		this.hasAuthenticationData = true;
		this.authUser              = authUser;
		this.authPass              = authPass;
	}

	public String getConnectableURL() {
		return connectableURL;
	}
	
	public boolean hasAuthenticationData() {
		return hasAuthenticationData;
	}

	public String getAuthUser() {
		return authUser;
	}

	public String getAuthPass() {
		return authPass;
	}

}
