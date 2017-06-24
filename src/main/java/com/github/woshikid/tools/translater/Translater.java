package com.github.woshikid.tools.translater;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.woshikid.utils.DBUtils;
import com.github.woshikid.utils.SQLUtils;
import com.github.woshikid.utils.StringUtils;

public class Translater {
	public static boolean enable = false;
	private static Map<String, Map<String, String>> translates = new HashMap<String, Map<String, String>>();
	
	public synchronized static void clear() {
		translates.clear();
	}
	
	public static String getText(String original, String lang) {
		if(StringUtils.isEmpty(original))return "";
		if(original.matches("^[\\.\\-0-9%]+$"))return original;
		
		if(!translates.containsKey(original)){
			synchronized(Translater.class) {
				if(!translates.containsKey(original)){
					translates.put(original, new HashMap<String, String>());
				}
			}
		}
		
		Map<String, String> translation = translates.get(original);
		if(translation.containsKey(lang))return StringUtils.defaultIfEmpty(translation.get(lang), original);
		
		try{
			synchronized(Translater.class) {
				if(!translation.containsKey(lang)){
					List<Map<String, String>> list = DBUtils.query("select t.text from TRANSLATION t where t.original = "+SQLUtils.sql(original)+" and t.lang = "+SQLUtils.sql(lang));
					if(list.size() > 0){
						String text = list.get(0).get("text").replace("<", "\u2039").replace(">", "\u203A").replace("\'", "\u2018").replace("\"", "\u201C");
						translation.put(lang, text);
					}else{
						translation.put(lang, "");
						if(DBUtils.count("select 1 from TRANSLATION_TODO t where t.original = "+SQLUtils.sql(original)+" and t.lang = "+SQLUtils.sql(lang)) == 0){
							DBUtils.execute("insert into translation_todo(original, lang) values("+SQLUtils.sql(original)+", "+SQLUtils.sql(lang)+")");
						}
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
		return StringUtils.defaultIfEmpty(translation.get(lang), original);
	}
	
	public static String translate(String content, String lang) {
		if(content.startsWith("\ufeff"))return "\ufeff" + translate(content.substring(1), lang);
		
		if(StringUtils.isEmpty(lang)){
			StringBuffer buffer = new StringBuffer(content);
			Matcher m = Pattern.compile("\\[t!([^\\]]*)\\]").matcher(content);
			while(m.find()){
				String original = m.group();
				String text = m.group(1);
				int start = buffer.indexOf(original);
				int end = start + original.length();
				buffer.replace(start, end, text);
			}
			content = buffer.toString();
		}else{
			StringBuffer buffer = new StringBuffer(content);
			Matcher m = Pattern.compile("\\\\u[0-9a-fA-F]{4}").matcher(content);
			while(m.find()){
				String unicode = m.group();
				String original = StringUtils.fromUnicode(unicode);
				int start = buffer.indexOf(unicode);
				int end = start + unicode.length();
				buffer.replace(start, end, original);
			}
			content = buffer.toString();
			
			m = Pattern.compile("\\[t!([^\\]]*)\\]").matcher(content);
			while(m.find()){
				String original = m.group();
				String text = getText(m.group(1), lang);
				int start = buffer.indexOf(original);
				int end = start + original.length();
				buffer.replace(start, end, text);
			}
			content = buffer.toString();
			
			m = Pattern.compile("\\[t:([^\\]]*)\\]").matcher(content);
			while(m.find()){
				String original = m.group();
				String text = StringUtils.toUnicode(m.group(1));
				int start = buffer.indexOf(original);
				int end = start + original.length();
				buffer.replace(start, end, "[t:" + text + "]");
			}
			content = buffer.toString();
			
			m = Pattern.compile("[\\.\\-0-9%\u4e00-\u9fa5\ufe30-\uffa0]+").matcher(content);
			while(m.find()){
				String original = m.group();
				String text = getText(original, lang);
				int start = buffer.indexOf(original);
				int end = start + original.length();
				buffer.replace(start, end, text);
			}
			content = buffer.toString();
			
			m = Pattern.compile("\\[t:([^\\]]*)\\]").matcher(content);
			while(m.find()){
				String original = m.group();
				String text = StringUtils.fromUnicode(m.group(1));
				int start = buffer.indexOf(original);
				int end = start + original.length();
				buffer.replace(start, end, text);
			}
			content = buffer.toString();
		}
		return content;
	}
}
