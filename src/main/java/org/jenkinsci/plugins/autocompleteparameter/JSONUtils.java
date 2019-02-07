package org.jenkinsci.plugins.autocompleteparameter;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

public class JSONUtils {
	public static String toJSON(Object o) {
		return JSONSerializer.toJSON(o).toString();
	}

	public static Collection<?> toCanonicalCollection(String data) {
		if (StringUtils.isEmpty(data))
			return Collections.emptyList();
		
		JSON json = JSONSerializer.toJSON(data);
		if (json instanceof JSONArray) 
			return JSONArray.toCollection((JSONArray) json);
		
		JSONObject jsonObject = (JSONObject) json;
		
		LinkedList<Map<String, String>> list = new LinkedList<Map<String, String>>();
		
		for (Object key : jsonObject.keySet()) {
			LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
			map.put("key", key.toString());
			map.put("value", jsonObject.getString(key.toString()));
			list.add(map);
		}
		
		return list;
	}

	public static String traverseJson(String data, String xpath) {
		if (StringUtils.isEmpty(xpath)) {
			return data;
		}
		JSON json = JSONSerializer.toJSON(data);

		for (String part: xpath.split("/")) {
			if (json instanceof JSONObject) {
				json = JSONSerializer.toJSON(((JSONObject) json).get(part));
			} else if (json instanceof JSONArray) {
				json = JSONSerializer.toJSON(((JSONArray) json).get(Integer.parseInt(part)));
			}
		}
		return json.toString();
	}
}
