package com.github.pony;

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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

public class Notification {

	public String postJson(String json, String path, String accessToken) {
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


	public String getBranchList(String ownerName, String repositoryName) throws IOException {
		String body = "";
		String url = "https://api.github.com/repos/" + ownerName + "/" + repositoryName + "/branches?access_token="
				+ DataBase.getDataBaseToken(ownerName);
		try {
			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpGet request = new HttpGet(url);
			request.addHeader("content-type", "application/json");
			HttpResponse result = httpClient.execute(request);
			body = EntityUtils.toString(result.getEntity(), "UTF-8");
		} catch (IOException ex) {
			System.out.println(ex.getStackTrace());
		}

		return body;
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

}
