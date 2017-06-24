package com.github.woshikid.tools;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;

/**
 * 隧道代理工具
 * socks5代理
 * @author kid
 *
 */
public class Tunnel extends Thread {
	private static String newline = System.lineSeparator();
	private static String charset = Charset.defaultCharset().name();
	private static FileOutputStream logFile = null;
	private static ServerSocket server;
	private static int count = 0;
	private Socket inSocket;
	private Socket outSocket;
	private InputStream in;
	private OutputStream out;
	private int number;
	private boolean isSend;
	
	public Tunnel(Socket inSocket, Socket outSocket, int number, boolean isSend) throws Exception {
		this.inSocket = inSocket;
		this.outSocket = outSocket;
		this.in = inSocket.getInputStream();
		this.out = outSocket.getOutputStream();
		this.number = number;
		this.isSend = isSend;
	}
	
	private synchronized static void log(String log) {
		if (logFile != null) {
			try {
				logFile.write(log.getBytes());
				logFile.write(newline.getBytes());
				logFile.flush();
			} catch (Exception e) {}
		}
		
		System.out.println(log);
	}
	
	private synchronized static void log(byte bytes[], int offset, int length) {
		if (logFile != null) {
			try {
				logFile.write(bytes, offset, length);
				logFile.write(newline.getBytes());
				logFile.flush();
			} catch (Exception e) {}
		}
		
		try {
			System.out.println(new String(bytes, offset, length, charset));
		} catch (Exception e) {}
	}
	
	public void run() {
		byte[] buffer = new byte[1024 * 1024];
		int length;
		
		try {
			while ((length = in.read(buffer)) != -1) {
				out.write(buffer, 0, length);
				out.flush();
				
				synchronized (Tunnel.class) {
					log("Tunnel " + number + (isSend?" send:":" receive:"));
					log(buffer, 0, length);
				}
			}
			
			log("Tunnel " + number + " closed by " + (isSend?"local.":"remote."));
		} catch (Exception e) {
			log("Tunnel " + number + (isSend?" local":" remote") + " error.");
		} finally {
			try { out.close();		} catch (Exception e) {}
			try { outSocket.close();} catch (Exception e) {}
			try { in.close();		} catch (Exception e) {}
			try { inSocket.close();	} catch (Exception e) {}
		}
	}
	
	public static void main(String... args) throws Exception {
		if (args.length < 3) {
			System.err.println("Usage: Tunnel localPort remoteHost remotePort [charset] [logFile]");
			return;
		}
		
		int localPort = Integer.parseInt(args[0]);
		String remoteHost = args[1];
		int remotePort = Integer.parseInt(args[2]);
		if (args.length > 3) charset = args[3];
		if (args.length > 4) logFile = new FileOutputStream(args[4]);
		
		server = new ServerSocket(localPort);
		while (true) {
			Socket localSocket = server.accept();
			Socket remoteSocket = new Socket(remoteHost, remotePort);
			log("Tunnel " + (++count) + " connected.");
			
			new Tunnel(localSocket, remoteSocket, count, true).start();
			new Tunnel(remoteSocket, localSocket, count, false).start();
		}
	}
}
