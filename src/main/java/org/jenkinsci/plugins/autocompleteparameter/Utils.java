package org.jenkinsci.plugins.autocompleteparameter;

public class Utils {
	public static String normalizeExpression(String e) {
		if (e == null) 
			return "\"{}\"";
		
		e = e.replace("\"", "\\\"");
		
		if (e.startsWith("{")) 
			return "\"" + e + "\"";
		return "\"{"+e+"}\"";
	}
}
