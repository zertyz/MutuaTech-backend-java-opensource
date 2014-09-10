package adapters.dto;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Hashtable;

/** <pre>
 * HTTPResponseDto.java
 * ====================
 * (created by luiz, Sep 8, 2014)
 *
 * Represents the information retrieved by an HTTPClientAdapter request
 *
 * @see HTTPClientAdapter, HTTPRequestDto
 * @version $Id$
 * @author luiz
 */

public class HTTPResponseDto {
	
	private byte[] contents;
	private Hashtable<String, ArrayList<String>> headers;
	
	
	public HTTPResponseDto(byte[] contents) {
		this.contents = contents;
		headers = new Hashtable<String, ArrayList<String>>();
	}

	public void addHeader(String headerName, String headerValue) {
		ArrayList<String> headerValues;
		if (headers.containsKey(headerName)) {
			headerValues = headers.get(headerName);
		} else {
			headerValues = new ArrayList<String>();
			headers.put(headerName, headerValues);
		}
		headerValues.add(headerValue);
	}
	
	public Hashtable<String, ArrayList<String>> getHeaders() {
		return headers;
	}
	
	public byte[] getContents() {
		return contents;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append("@HTTPResponseDto{headers={");
		boolean showRootPreviousComma = false;
		for (String headerName : headers.keySet()) {
			if (showRootPreviousComma) {
				sb.append(",");
			} else {
				showRootPreviousComma = true;
			}
			sb.append(headerName);
			sb.append("={");
			boolean showHeadersPreviousComma = false;
			for (String headerValue : headers.get(headerName)) {
				if (showHeadersPreviousComma) {
					sb.append(",");
				} else {
					showHeadersPreviousComma = true;
				}
				sb.append("'");
				sb.append(headerValue);
				sb.append("'");
			}
			sb.append("}");
		}
		sb.append("}, contents=[[");
		String contents;
		try {
			contents = new String(getContents(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			contents = new String(getContents());
		}
		sb.append(contents.replaceAll("\n", "\\n").replaceAll("\r", "\\r"));
		sb.append("]]}");
		return sb.toString();
	}

}
