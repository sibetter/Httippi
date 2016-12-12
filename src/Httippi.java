/**
MIT License

Copyright (c) 2016 sibetter

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

*/

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

public class Httippi {
	
	// Version code
	public static final String VERSION = "0.2";
	// Mime types
	public static final String MIMETYPE_JSON = "application/json";
	public static final String MIMETYPE_TEXT_PLAIN = "text/plain";
	public static final String MIMETYPE_HTML = "text/html";
		
	// response
	private Map<String, List<String>> responseHeaders;	
	private int responseCode;
	private String responseMessage;
	
	private URL endpoint;
	private HttpURLConnection connection;
	private String userAgent = Httippi.class.getSimpleName()+"/"+VERSION;
	
	public Httippi() {
		
	}	
	
	public Httippi url(URL endpoint) throws IOException {
		this.endpoint = endpoint;
		return this;
	}	
	
	private void open() throws IOException {
		URLConnection conn = this.endpoint.openConnection();
		if (!(conn instanceof HttpsURLConnection) && !(conn instanceof HttpURLConnection))
			throw new IllegalArgumentException("Only http/s connection is allowed");
		
		this.connection = (HttpURLConnection) conn;
	}

	public String get() throws IOException {
		try {
			this.open();
			this.connection.setRequestMethod("GET");
		} catch (ProtocolException e) {
		}
		return doConnection(null, null);
	}
	
	public String delete() throws IOException {
		try {
			this.open();
			this.connection.setRequestMethod("DELETE");
		} catch (ProtocolException e) {
		}
		return doConnection(null, null);
	}
	
	public String getBytes() throws IOException {
		throw new IOException("Not implemented yet");
	}
	
	public String post(String body) throws IOException {
		return this.post(body, MIMETYPE_TEXT_PLAIN);
	}
	
	public String post(String body, String contentType) throws IOException {
		try {
			this.open();
			this.connection.setRequestMethod("POST");
		} catch (ProtocolException e) {
		}
		return doConnection(body, contentType);
	}
	
	public String put(String body) throws IOException {
		return this.put(body, MIMETYPE_TEXT_PLAIN);
	}
	
	public String put(String body, String contentType) throws IOException {
		try {
			this.open();
			this.connection.setRequestMethod("PUT");
		} catch (ProtocolException e) {
		}
		return doConnection(body, contentType);
	}

	public String getResponseHeader(String key) {
		return this.responseHeaders.get(key).get(0);
	}	

	public int getResponseCode() {
		return responseCode;
	}

	public String getResponseMessage() {
		return responseMessage;
	}	

	public String getContentType() {
		return getResponseHeader("Content-type");
	}
	
	public Httippi setHttpBasicAutentication(String userName, String password) {
		this.connection.setRequestProperty("Authorization",
										   "Basic " + Base64.getEncoder().encodeToString((userName + ":" + password).getBytes()));
		return this;
	}
	
	public Httippi setUserAgent(String userAgent) {
		this.userAgent = userAgent;
		return this;
	}

	public Httippi setSSLSocketFactory(SSLSocketFactory sf) {
		if (!isHTTPS())
			throw new IllegalArgumentException("This isn't an https connection");

		((HttpsURLConnection) this.connection).setSSLSocketFactory(sf);

		return this;

	}
	
	private boolean isHTTPS() {
		return (this.connection instanceof HttpsURLConnection);
	}

	private String doConnection(String body, String contentType) throws IOException {
		
		this.connection.setRequestProperty("User-Agent", this.userAgent);
		
		if (body != null) {
			if (contentType != null && !contentType.trim().equals("")) {
				this.connection.setRequestProperty("Content-type", contentType);
			}			
			this.connection.setDoOutput(true);			
			this.connection.getOutputStream().write(body.getBytes());
			this.connection.getOutputStream().flush();	
			this.connection.getOutputStream().close();
		}
				
		this.connection.connect();		

		//get response code
		this.responseCode = this.connection.getResponseCode();
		this.responseMessage = this.connection.getResponseMessage();		
		
		InputStream input = null;
		if (this.connection.getErrorStream() != null) {
			input = this.connection.getErrorStream();
		} else {
			input = this.connection.getInputStream();			
		}
		// get response headers
		this.responseHeaders = this.connection.getHeaderFields();		
		
		//read response
		StringBuffer sb = new StringBuffer();
		BufferedReader br = null;

		String line;
		br = new BufferedReader(new InputStreamReader(input));
		while ((line = br.readLine()) != null) {
			sb.append(line);
		}		
		
		// close input stream
		input.close();		
		
		return sb.toString();
	}	

}
