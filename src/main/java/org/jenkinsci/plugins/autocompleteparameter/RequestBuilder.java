package org.jenkinsci.plugins.autocompleteparameter;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

public class RequestBuilder {
	private HttpURLConnection conn;
	private String body;
	private String url;
	private int lowerAcceptableStatus = 200;
	private int upperAcceptableStatus = 399;
	

	public static RequestBuilder url(String url) {
		return new RequestBuilder(url);
	}
	
	public RequestBuilder() {}

	public RequestBuilder(String url) {
		this.url = url;
		try {
			this.conn = (HttpURLConnection) new URL(url).openConnection();
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	
	public RequestBuilder header(String key, String value) {
		conn.addRequestProperty(key, value);
		return this;
	}

	public RequestBuilder credentials(String credentialsId) {
		CredentialsUtils.addAuth(url, credentialsId, conn);
		return this;
	}
	
	public RequestBuilder body(Object body) {
		if (body instanceof String)
			this.body = body.toString();
		else
			this.body = JSONUtils.toJSON(body);
		return this;
	}
	
	public RequestResponse post() {
		return doRequest("POST");
	}
	
	public RequestResponse put() {
		return doRequest("PUT");
	}
	
	public RequestResponse patch() {
		return doRequest("PATCH");
	}
	
	public RequestResponse get() {
		return doRequest("GET");
	}
	
	public RequestResponse delete() {
		return doRequest("DELETE");
	}
	
	public void acceptableRange(int from, int to) {
		this.lowerAcceptableStatus = from;
		this.upperAcceptableStatus = to;
	}

	private RequestResponse doRequest(String method) {
		OutputStreamWriter writer = null;
		try {
			conn.setRequestMethod(method);
			
			if (!StringUtils.isEmpty(body)) {
				conn.setDoOutput(true);
				writer = new OutputStreamWriter(conn.getOutputStream());
				writer.write(body);
				writer.flush();
			}
			
			int responseCode = conn.getResponseCode();
			String content;
			
			try {
				content = IOUtils.toString(conn.getInputStream());
			}catch(IOException e) {
				content = e.getMessage();
			}
			
			if (responseCode < lowerAcceptableStatus || responseCode > upperAcceptableStatus)
				throw new IllegalStateException("Failed : HTTP error code : " + responseCode);
			
			return new RequestResponse(responseCode, content, conn.getHeaderFields());
			
		} catch (Exception e) {
			throw new IllegalStateException(e);
		} finally {
			try {
				if (writer != null)
					writer.close();
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}
	}
}
