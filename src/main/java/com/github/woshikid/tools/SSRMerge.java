package com.github.woshikid.tools;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import java.util.Map;

import com.github.woshikid.utils.FileUtils;
import com.github.woshikid.utils.JSONUtils;

/**
 * gui-config.json合并工具
 * @author kid
 *
 */
public class SSRMerge {

	/**
	 * gui-config.json所在位置
	 */
	private static String filePath;
	
	/**
	 * 合并后的json内容
	 */
	private static Map<String, Object> totalJson;
	
	/**
	 * 合并文件内容到内存
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private static int merge() throws Exception {
		String content = FileUtils.readFile(new File(filePath), "UTF-8");
		Map<String, Object> json = JSONUtils.toMap(content);
		
		if (totalJson == null) {
			totalJson = json;
			return ((List<Object>)json.get("configs")).size();
		} else {
			List<Map<String, Object>> totalConfigs = (List<Map<String, Object>>)totalJson.get("configs");
			List<Map<String, Object>> configs = (List<Map<String, Object>>)json.get("configs");
			
			int found = 0;
			for (Map<String, Object> map : configs) {
				Object remarks = map.get("remarks");
				
				boolean exists = false;
				for (Map<String, Object> tMap : totalConfigs) {
					Object tRemarks = tMap.get("remarks");
					
					if (remarks.equals(tRemarks)) {
						exists = true;
						break;
					}
				}
				
				if (!exists) {
					totalConfigs.add(map);
					found++;
				}
			}
			
			return found;
		}
	}
	
	/**
	 * 将合并后内容写回文件
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private static int export() throws Exception {
		String json = JSONUtils.toJSON(totalJson, true);
		FileUtils.writeFile(new File(filePath), json.getBytes("UTF-8"), false);
		return ((List<Object>)totalJson.get("configs")).size();
	}
	
	public static void main(String... args) throws Exception {
		if (args.length < 2) {
			System.err.println("Usage: SSRMerge path file");
			return;
		} else {
			filePath = args[0] + args[1];
		}
		
		WatchService watcher = FileSystems.getDefault().newWatchService();
		Path path = Paths.get(args[0]);
		path.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY,
				StandardWatchEventKinds.ENTRY_DELETE);
		
		while (true) {
			WatchKey key = watcher.take();
			
			for (WatchEvent<?> event : key.pollEvents()) {
				Path changed = (Path)event.context();
				if (!changed.endsWith(args[1])) continue;
				
				if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
					//等待文件完全写入并关闭
					Thread.sleep(2000);
					int count = merge();
					System.out.println("found:\t" + count);
				} else {
					int count = export();
					System.out.println("\ntotal:\t" + count);
					return;
				}
			}
			
			if (!key.reset()) break;
		}
	}
}
