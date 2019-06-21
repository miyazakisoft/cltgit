package com.example.cltgit;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;

public class DownloadAndSave {
	

	public void perform(Integer violationCount, String setToBadgePath) throws IOException {
		
		String color = "brightgreen";
		
		if(violationCount>=100) {
			color = "red";
		}else if(violationCount>=50) {
			color = "orange";
		}else if(violationCount>=20) {
			color = "yellow";
		}else if(violationCount>=10) {
			color = "yellowgreen";
		}else if(violationCount>0) {
			color = "green";
		}
		
		
		URL url = new URL("https://img.shields.io/badge/9rules result-" + violationCount + "-" + color + ".svg"); // ダウンロードする URL
		URLConnection conn = url.openConnection();
		conn.setRequestProperty("User-agent","Mozilla/5.0");
		InputStream in = conn.getInputStream();

		File file = new File(setToBadgePath); // 保存先
		FileOutputStream out = new FileOutputStream(file, false);
		int b;
		while((b = in.read()) != -1){
		    out.write(b);
		}

		out.close();
		in.close();
		
	}
	

}
