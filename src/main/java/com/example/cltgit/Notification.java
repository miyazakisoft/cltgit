package com.example.cltgit;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

public class Notification {

	public String postJson(String json, String path,String accessToken) {
		HttpURLConnection uc;
		try {
			URL url = new URL("https://api.github.com/" + path);
			uc = (HttpURLConnection) url.openConnection();
			uc.setRequestMethod("POST");
			uc.setUseCaches(false);
			uc.setDoOutput(true);
			uc.setRequestProperty("Content-Type", "application/json; charset=utf-8");
			uc.setRequestProperty("Authorization", "token " + accessToken);
			// uc.setRequestProperty("WWW-Authenticate", "Bearer error=\"invalid_token\"");
			// uc.setRequestProperty("Authorization", "Bearer");

			OutputStreamWriter out = new OutputStreamWriter(new BufferedOutputStream(uc.getOutputStream()));
			out.write(json);
			out.close();

			BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
			String line = in.readLine();
			String body = "";
			while (line != null) {
				body = body + line;
				line = in.readLine();
			}
			uc.disconnect();
			return body;
		} catch (IOException e) {
			e.printStackTrace();
			return "client - IOException : " + e.getMessage();
		}
	}

	public String getAccessToken(String code, String clientId, String clientSecret) {
		HttpURLConnection uc;
		try {
			URL url = new URL("https://github.com/login/oauth/access_token?" + "code=" + code + "&client_id=" + clientId
					+ "&client_secret=" + clientSecret);
			uc = (HttpURLConnection) url.openConnection();
			uc.setRequestMethod("GET");
			uc.setUseCaches(false);
			uc.setDoOutput(true);
			uc.setRequestProperty("Content-Type", "text/plain; charset=utf-8");

			OutputStreamWriter out = new OutputStreamWriter(new BufferedOutputStream(uc.getOutputStream()));
			out.close();

			BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
			String line = in.readLine();
			String body = "";
			while (line != null) {
				body = body + line;
				line = in.readLine();
			}
			uc.disconnect();
			return body.replace("access_token=", "").replace("&scope=&token_type=bearer", "");
		} catch (IOException e) {
			e.printStackTrace();
			return "client - IOException : " + e.getMessage();
		}
	}

	public void callVarnish(String url) {

		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpPurge httpPurge = new HttpPurge(url);
		Header header = new BasicHeader("Host", "camo.githubusercontent.com");
		httpPurge.setHeader(header);
		try {
			HttpResponse response = httpclient.execute(httpPurge);
			System.out.print("-------------------------------------");
			System.out.println(response.getStatusLine());
			System.out.print("-------------------------------------");
			HttpEntity entity = response.getEntity();
			// If the response does not enclose an entity, there is no need
			// to worry about connection release
			if (entity != null) {
				// do something useful with the response body
				// and ensure it is fully consumed
				EntityUtils.consume(entity);
			}
		} catch (IOException ex) {

			// In case of an IOException the connection will be released
			// back to the connection manager automatically
		} catch (RuntimeException ex) {

			// In case of an unexpected exception you may want to abort
			// the HTTP request in order to shut down the underlying
			// connection and release it back to the connection manager.
			httpPurge.abort();
		}
	}

}
