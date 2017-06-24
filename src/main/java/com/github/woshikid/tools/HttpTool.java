package com.github.woshikid.tools;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Http请求工具
 * @author kid
 *
 */
public class HttpTool extends Thread {
	private static String charset = Charset.defaultCharset().name();
	private static HttpURLConnection http;
	private static OutputStream out;
	
	public static void main(String... args) throws Exception {
		if (args.length < 1) {
			System.err.println("Usage: HttpTool url [charset] [headFile] [inFile] [outFile]");
			return;
		}
		
		String url = args[0];
		if (args.length > 1) charset = args[1];
		Properties head = new Properties();
		if (args.length > 2) head.load(new FileInputStream(args[2]));
		FileInputStream inFile = null;
		if (args.length > 3) inFile = new FileInputStream(args[3]);
		FileOutputStream outFile = null;
		if (args.length > 4) outFile = new FileOutputStream(args[4]);
		
		http = (HttpURLConnection) new URL(url).openConnection();
		http.setConnectTimeout(30000);
		http.setReadTimeout(60000);
		http.setUseCaches(false);
		http.setDoInput(true);
		http.setDoOutput(inFile != null);
		
		Enumeration<Object> keys = head.keys();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			http.setRequestProperty(key, head.getProperty(key));
		}
		
		http.connect();
		if (inFile != null) out = http.getOutputStream();
		System.out.println("Connected.");
		
		byte[] buffer = new byte[1024 * 1024];
		int length;
		
		if (inFile != null) {
			while ((length = inFile.read(buffer)) != -1) {
				out.write(buffer, 0, length);
				out.flush();
				System.out.print(new String(buffer, 0, length, charset));
			}
			
			out.close();
			System.out.println(System.lineSeparator());
			inFile.close();
		}
		
		try {
			for (int  i = 0; i < 100; i++) {
				String key = http.getHeaderFieldKey(i);
				String value = http.getHeaderField(i);
				if (key == null && value == null) break;
				
				String headField = (key == null) ? value : (key + ": " + value);
				if (outFile != null) {
					outFile.write(headField.getBytes(charset));
					outFile.write(System.lineSeparator().getBytes(charset));
					outFile.flush();
				}
				System.out.println(headField);
			}
			System.out.println();
			
			InputStream in = http.getInputStream();
			while ((length = in.read(buffer)) != -1) {
				if (outFile != null) {
					outFile.write(buffer, 0, length);
					outFile.flush();
				}
				System.out.print(new String(buffer, 0, length, charset));
			}
			
			System.out.println();
			System.out.println("Closed.");
		} catch (Exception e) {
			InputStream in = http.getErrorStream();
			while ((length = in.read(buffer)) != -1) {
				if (outFile != null) {
					outFile.write(buffer, 0, length);
					outFile.flush();
				}
				System.out.print(new String(buffer, 0, length, charset));
			}
			
			System.out.println();
			System.out.println("Reseted.");
		}
		
		if (outFile != null) outFile.close();
		System.exit(0);
	}
}
