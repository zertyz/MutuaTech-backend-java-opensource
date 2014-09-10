package adapters.dto;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import adapters.HTTPClientAdapter;

/** <pre>
 * HTTPRequestDto.java
 * ===================
 * (created by luiz, Dec 9, 2008)
 *
 * Represents the information to be sent by an HTTPClientAdapter request
 *
 * @see HTTPClientAdapter, HTTPResponseDto
 * @version $Id$
 * @author luiz
 */

public class HTTPRequestDto {
	
	private Hashtable<String, String> parameters;
	private Hashtable<String, String> headers;
	
	
	public HTTPRequestDto() {
		parameters = new Hashtable<String, String>();
		headers    = new Hashtable<String, String>();
	}
	
	public void addParameter(String name, String value) {
		if (value != null) {
			parameters.put(name, value);
		}
	}
	
	public void addHeader(String name, String value) {
		if (value != null) {
			headers.put(name, value);
		}
	}
	
	public Enumeration<String> getParametersEnumeration() {
		return parameters.keys();
	}
	
	public Enumeration<String> getHeadersEnumeration() {
		return headers.keys();
	}
	
	public String getParameter(String name) {
		return parameters.get(name);
	}
	
	public String getHeader(String name) {
		return headers.get(name);
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		boolean showPreviousComma;

		sb.append("@HTTPRequestDto{headers={");
		showPreviousComma = false;
		for (String headerName : headers.keySet()) {
			if (showPreviousComma) {
				sb.append(",");
			} else {
				showPreviousComma = true;
			}
			sb.append(headerName);
			sb.append("='");
			sb.append(headers.get(headerName));
			sb.append("'");
		}
		sb.deleteCharAt(sb.length()-1);
		sb.append("}, parameters={");
		showPreviousComma = false;
		for (String parameterName : parameters.keySet()) {
			if (showPreviousComma) {
				sb.append(",");
			} else {
				showPreviousComma = true;
			}
			sb.append(parameterName);
			sb.append("='");
			sb.append(parameters.get(parameterName));
			sb.append("'");
		}
		sb.append("}}");
		return sb.toString();
	}
	
	

}
