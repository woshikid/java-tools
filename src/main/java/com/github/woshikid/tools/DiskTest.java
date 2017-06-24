package com.github.woshikid.tools;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 磁盘读写速度测试
 * 磁盘读写正确性校验
 * @author kid
 *
 */
public class DiskTest extends Thread {
	private static boolean enable = true;
	private static boolean readMode;
	private static long data = 0;
	private static long error = 0;
	private static DataOutputStream out;
	private static DataInputStream in;

	public void run() {
		try {
			long lastData = data;
			while (enable) {
				Thread.sleep(1000);
				long size = data * 8 / 1024 / 1024;
				String speed = new DecimalFormat("#.##").format(Math.abs((double)(data - lastData)) * 8 / 1024 / 1024);
				String errorInfo = readMode?("   \t" + (error * 8) + "B"):"";
				System.out.println(size + "MB\t\t" + speed + "MB/s" + errorInfo);
				lastData = data;
			}
			
			System.out.println("Done.");
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	public static void main(String... args) throws Exception {
		if (args.length < 1) {
			System.err.println("Usage: DiskTest path/file");
			return;
		}
		
		File file = new File(args[0]);
		if (!file.exists()) {
			System.err.println("path/file not exists.");
			return;
		}
		
		if (file.isDirectory()) readMode = false;
		if (file.isFile()) readMode = true;
		
		new DiskTest().start();
		
		try {
			if (!readMode) {
				System.out.println("Write Mode:");
				String time = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
				out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(new File(args[0] + "/" + time + ".dat")), 256 * 1024));
				try {
					while (true) out.writeLong(data++);
				} catch (Exception e) {
					try { out.close(); } catch (Exception ee) {}
				}
				
				data = data - 256 * 1024 / 8 - 1;
				out = new DataOutputStream(new FileOutputStream(new File(args[0] + "/" + time + ".dat"), true));
				while (true) out.writeLong(data++);
			} else {
				System.out.println("Read Mode:");
				in = new DataInputStream(new BufferedInputStream(new FileInputStream(file), 256 * 1024));
				while (true) if (in.readLong() != data++) error++;
			}
		} catch (Exception e) {
			//e.printStackTrace();
		}
		
		Thread.sleep(1000);
		enable = false;
	}
}
