package com.github.pony;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class Execution {
	public void exec(String projectName) {
		Process process;
		try {
			process = new ProcessBuilder("sh", "exec.sh", projectName).start();
			InputStream is = process.getInputStream();

			/*
			 * プロセス実行側での文字列等の出力によっては、 文字コードが一致しないと、受け取る際に文字化けを起こす
			 */
			InputStreamReader isr = new InputStreamReader(is, "Shift-JIS");
			// InputStreamReader isr = new InputStreamReader(is, "UTF-8");

			BufferedReader reader = new BufferedReader(isr);
			StringBuilder builder = new StringBuilder();
			int c;
			while ((c = reader.read()) != -1) {
				builder.append((char) c);
			}
			// コンソール出力される文字列の格納
			String text = builder.toString();
			// 終了コードの格納(0:正常終了 1:異常終了)
			int ret = process.waitFor();
			// System.out.println(text);
			if (ret != 0) {
				System.out.println("execスクリプトに関する異常が見つかりました．");
			}
			// System.out.println(ret);

		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void convert(List<String> command) {
		Process process;
		
		for(String s:command) {
			System.out.println("command " + s);
		}
		
		
		try {
			// process = new ProcessBuilder(command).start();
			process = new ProcessBuilder(command).start();
			InputStream is = process.getInputStream();

			/*
			 * プロセス実行側での文字列等の出力によっては、 文字コードが一致しないと、受け取る際に文字化けを起こす
			 */
			InputStreamReader isr = new InputStreamReader(is, "Shift-JIS");
			// InputStreamReader isr = new InputStreamReader(is, "UTF-8");

			BufferedReader reader = new BufferedReader(isr);
			StringBuilder builder = new StringBuilder();
			int c;
			while ((c = reader.read()) != -1) {
				builder.append((char) c);
			}
			// コンソール出力される文字列の格納
			String text = builder.toString();
			// 終了コードの格納(0:正常終了 1:異常終了)
			int ret = process.waitFor();
			System.out.println("---python出力コード（始）---");
			System.out.println(text);
			System.out.println("---python出力コード（終）---");
			if (ret != 0) {
				System.out.println("convertスクリプトに関する異常が見つかりました．");
			}
			// System.out.println(ret);

		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
