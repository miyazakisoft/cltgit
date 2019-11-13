package com.example.cltgit;

import java.net.URI;

import org.apache.http.client.methods.HttpRequestBase;

public class HttpPurge extends HttpRequestBase {

	public final static String METHOD_NAME = "PURGE";

	public HttpPurge() {
		super();
	}

	@Override
	public String getMethod() {
		return METHOD_NAME; // To change body of implemented methods use File | Settings | File Templates.
	}

	public HttpPurge(final String uri) {
		super();
		setURI(URI.create(uri));
	}

	public String getName() {
		return "PURGE";
	}
}
