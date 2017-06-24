package com.github.woshikid.tools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * http扫描工具
 * 得到整个网站的链接地址
 * @author kid
 *
 */
@SuppressWarnings("unchecked")
public class HttpScan {
	private static String defaultCharset = "GBK";
	private static int M = 1024*1024;
	private static String HOST = "";
	private static String cookie = "";
	private static String session = "";
	private static List skipList = new ArrayList();
	private static List urlList = new ArrayList();
	private static List historyList = new ArrayList();
	private static FileWriter historyFile;
	private static Map registerMap = new HashMap();
	
	private static String n(Object input){
		return input == null?"":String.valueOf(input);
	}
	
	private static boolean e(Object input){
		return (input == null || input.equals(""))?true:false;
	}
	
	private static String trim(Object input){
		return n(input).replace("\r", "").replace("\n", "").replace("\t", "").trim();
	}
	
	private static void loadHistory() throws Exception{
		historyFile = new FileWriter(HOST + ".log", true);
		BufferedReader reader = new BufferedReader(new FileReader(HOST + ".log"));
		String line = reader.readLine();
		while(line != null){
			line = trim(line);
			historyList.add(line);
			register(line);
			line = reader.readLine();
		}
		reader.close();
	}
	
	public static void main(String args[]) throws Exception{
		if(args.length == 0){
			System.out.println("usage: java HttpScan [session:] [cookie:] [skip:] [host:] [test:] [get:] [http://url]");
			return;
		}
		
		for(int i=0;i<args.length;i++){
			if(args[i].toLowerCase().startsWith("session:")){
				session += ";" + args[i].substring(8);
			}else if(args[i].toLowerCase().startsWith("cookie:")){
				cookie += " " + args[i].substring(7);
			}else if(args[i].toLowerCase().startsWith("skip:")){
				skipList.add(args[i].substring(5));
			}else if(args[i].toLowerCase().startsWith("host:")){
				HOST = args[i].substring(5).toLowerCase();
			}else if(args[i].toLowerCase().startsWith("http://")){
				if(e(HOST)){
					URL url = new URL(args[i]);
					HOST = n(url.getHost()).toLowerCase().replaceAll(".*?(?=[^\\.]+\\.[^\\.]+$)", "");
				}
				addUrl(args[i], null);
			}else if(args[i].toLowerCase().startsWith("test:")){
				testUrl(args[i].substring(5));
			}else if(args[i].toLowerCase().startsWith("get:")){
				String urlString = args[i].substring(4);
				System.out.println(urlString);
				String html = n(getHtml(urlString).get("html"));
				System.out.println(html);
			}
		}
		
		if(e(HOST))return;
		loadHistory();
		
		scanUrls();
	}
	
	private static boolean inSkipList(String urlString){
		for(int i=0;i<skipList.size();i++)
			if(n(urlString).matches(n(skipList.get(i))))
				return true;
		
		return false;
	}
	
	private static void addUrl(String urlString, String charset){
		urlString = trim(urlString).replaceAll("#.*", "");
		if(e(urlString))return;
		URL url;
		try{
			url = new URL(urlString);
		}catch(Exception e){
			return;
		}
		
		if(!"http".equalsIgnoreCase(url.getProtocol()))return;
		String host = n(url.getHost()).toLowerCase();
		if(!host.endsWith(HOST))return;
		
		urlString = getUrlString(url, charset);
		if(inSkipList(urlString))return;
		if(urlList.contains(urlString))return;
		if(historyList.contains(urlString))return;
		if(register(urlString))urlList.add(urlString);
	}
	
	private static String getUrlString(URL url, String charset){
		int port = url.getPort();
		if(port == url.getDefaultPort())port = -1;
		String protocol = n(url.getProtocol()).toLowerCase();
		String host = n(url.getHost()).toLowerCase().replace(" ", "");
		String path = getCanonicalPath(n(url.getPath()).replace(" ", "%20"));
		String query = n(url.getQuery()).replace(" ", "%20");
		String urlString = protocol + "://" + host + (port==-1?"":":"+port) + path + (e(query)?"":"?"+query);
		
		try{
			if(e(charset))charset = defaultCharset;
			Matcher m = Pattern.compile("[^\\x00-\\xff]").matcher(urlString);
			while(m.find()){
				String character = m.group();
				urlString = urlString.replace(character, URLEncoder.encode(character, charset));
			}
		}catch(Exception e){}
		
		return urlString;
	}
	
	private static String getCanonicalPath(String path){
		path = n(path).replaceAll("//+", "/").replaceAll("/(\\./)+", "/");
		String newpath = path.replaceFirst("^/(\\.{2}/)+", "/").replaceFirst("/[^/]+/\\.{2}/", "/");
		while(!newpath.equals(path)){
			path = newpath;
			newpath = path.replaceFirst("^/(\\.{2}/)+", "/").replaceFirst("/[^/]+/\\.{2}/", "/");
		}
		return newpath;
	}
	
	private static String addSessionTag(String urlString){
		if(e(session) || e(urlString))return urlString;
		int q = urlString.indexOf("?");
		if(q == -1){
			return urlString + session;
		}else{
			return urlString.substring(0, q) + session + urlString.substring(q);
		}
	}
	
	private static boolean register(String urlString){
		URL url;
		try{
			url = new URL(urlString);
		}catch(Exception e){
			return false;
		}
		
		String path = n(url.getPath());
		Integer i = (Integer)registerMap.get(path);
		if(i == null){
			registerMap.put(path, 1);
			return true;
		}else{
			if(i.intValue() >= 100){
				return false;
			}else{
				registerMap.put(path, i.intValue()+1);
				return true;
			}
		}
	}
	
	private static String getHref(URL url, String href){
		href = trim(href);
		if(href.startsWith("#"))return "";
		if(href.matches("\\w*:.*"))return href;
		if(href.startsWith("//"))return "http:" + href;
		
		int port = url.getPort();
		if(port == url.getDefaultPort())port = -1;
		String protocol = n(url.getProtocol()).toLowerCase();
		String host = n(url.getHost()).toLowerCase();
		String path = n(url.getPath()).replaceAll("/[^/]*$", "") + "/";
		
		if(href.startsWith("/")){
			return protocol + "://" + host + (port==-1?"":":"+port) + href;
		}else{
			return protocol + "://" + host + (port==-1?"":":"+port) + path + href;
		}
	}
	
	private static String readStream(InputStream in, String charset) throws Exception{
		BufferedReader reader = new BufferedReader(new InputStreamReader(in, charset));
		StringBuffer buffer = new StringBuffer();
		String line = reader.readLine();
		while(line != null){
			if(buffer.length() > 10*M)break;
			buffer.append(line).append("\r\n");
			line = reader.readLine();
		}
		reader.close();
		return buffer.toString();
	}
	
	private static void testUrl(String urlString) throws Exception{
		System.out.println(urlString);
		URL url = new URL(addSessionTag(urlString));
		HttpURLConnection con = (HttpURLConnection)url.openConnection();
		con.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; Trident/4.0)");
		con.setRequestProperty("Accept", "*/*");
		if(!e(cookie))con.setRequestProperty("Cookie", cookie);
		con.setInstanceFollowRedirects(false);
		con.setConnectTimeout(30000);
		con.setReadTimeout(60000);
		con.setUseCaches(false);
		con.setDoOutput(false);
		con.setDoInput(true);
		con.connect();
		
		for(int i=0;i<100;i++){
			String key = n(con.getHeaderFieldKey(i));
			String value = n(con.getHeaderField(i));
			if(e(key) && e(value))break;
			System.out.println(key + ": " + value);
		}
		con.disconnect();
	}
	
	private static Map getHtml(String urlString){
		Map map = new HashMap();
		
		try{
			URL url = new URL(addSessionTag(urlString));
			HttpURLConnection con = (HttpURLConnection)url.openConnection();
			con.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; Trident/4.0)");
			con.setRequestProperty("Accept", "*/*");
			if(!e(cookie))con.setRequestProperty("Cookie", cookie);
			con.setConnectTimeout(30000);
			con.setReadTimeout(60000);
			con.setUseCaches(false);
			con.setDoOutput(false);
			con.setDoInput(true);
			try{
				con.connect();
			}catch(Exception e){
				con.connect();
			}
			
			try{
				String contentType = trim(con.getContentType());
				String charset = contentType.replaceAll("(?i).*charset\\s*=", "").trim();
				try{
					if(!Charset.isSupported(charset))charset = defaultCharset;
				}catch(Exception e){
					charset = defaultCharset;
				}
				
				map.put("charset", charset);
				map.put("html", readStream(con.getInputStream(), charset));
			}catch(Exception e){
				map.put("html", readStream(con.getErrorStream(), defaultCharset));
			}
			con.disconnect();
		}catch(Exception e){}
		
		return map;
	}
	
	private static void scanUrls() throws Exception{
		while(urlList.size() > 0){
			String urlString = n(urlList.get(urlList.size()-1));
			if(historyList.contains(urlString)){
				urlList.remove(urlList.size()-1);
				historyFile.write(urlString + "\r\n");
				historyFile.flush();
			}else{
				System.out.print(historyList.size() + "\t" + urlString);
				
				analysisHtml(urlString);
				historyList.add(urlString);
				
				System.out.println("\t" + urlList.size());
			}
		}
		
		historyFile.flush();
		historyFile.close();
	}
	
	private static void analysisHtml(String urlString){
		URL url;
		try{
			url = new URL(urlString);
		}catch(Exception e){
			return;
		}
		
		Map map = getHtml(urlString);
		String charset = n(map.get("charset"));
		String html = trim(map.get("html"));
		if(e(html))return;
		
		Matcher m = Pattern.compile("( href| src| action)\\s*=\\s*(['\"])([^'\"<>]*?)\\2", Pattern.CASE_INSENSITIVE).matcher(html);
		while(m.find()){
			addUrl(getHref(url, m.group(3)), charset);
		}
		
		m = Pattern.compile("( href| src| action)\\s*=\\s*([^'\"<>]*?)[ >]", Pattern.CASE_INSENSITIVE).matcher(html);
		while(m.find()){
			addUrl(getHref(url, m.group(2)), charset);
		}
		
		m = Pattern.compile("(location|location\\.href|\\.src|\\.action)\\s*=\\s*(['\"])([^'\"<>]*?)\\2", Pattern.CASE_INSENSITIVE).matcher(html);
		while(m.find()){
			addUrl(getHref(url, m.group(3)), charset);
		}
		
		m = Pattern.compile("(replace|navigate|open|showModalDialog|showModelessDialog)\\s*\\(\\s*(['\"])([^'\"<>]*?)\\2", Pattern.CASE_INSENSITIVE).matcher(html);
		while(m.find()){
			addUrl(getHref(url, m.group(3)), charset);
		}
	}
}
