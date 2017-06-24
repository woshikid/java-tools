package com.github.woshikid.tools;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Scanner;

/**
 * tcp调试工具
 * @author kid
 *
 */
public class TCP extends Thread {
	private static Scanner input = new Scanner(System.in);
	private static String charset = Charset.defaultCharset().name();
	private static Socket socket;
	private static OutputStream out;
	
	public void run() {
		while (true) {
			String line = input.nextLine();
			if (line.endsWith("`")) {
				line = line.substring(0, line.length() - 1);
			} else {
				line = line + "\r\n";
			}
			
			try {
				out.write(line.getBytes(charset));
				out.flush();
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(0);
			}
		}
	}
	
	public static void main(String... args) throws Exception {
		if (args.length < 2) {
			System.err.println("Usage: TCP host port [charset] [inFile] [outFile]");
			return;
		}
		
		String host = args[0];
		int port = Integer.parseInt(args[1]);
		if (args.length > 2) charset = args[2];
		FileInputStream inFile = null;
		if (args.length > 3) inFile = new FileInputStream(args[3]);
		FileOutputStream outFile = null;
		if (args.length > 4) outFile = new FileOutputStream(args[4]);
		
		socket = new Socket(host, port);
		out = socket.getOutputStream();
		System.out.println("Connected.");
		
		byte[] buffer = new byte[1024 * 1024];
		int length;
		
		if (inFile != null) {
			while ((length = inFile.read(buffer)) != -1) {
				out.write(buffer, 0, length);
				out.flush();
				System.out.print(new String(buffer, 0, length, charset));
			}
			inFile.close();
		}
		
		new TCP().start();
		
		try {
			InputStream in = socket.getInputStream();
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
			System.out.println();
			System.out.println("Reseted.");
		}
		
		if (outFile != null) outFile.close();
		System.exit(0);
	}
}
