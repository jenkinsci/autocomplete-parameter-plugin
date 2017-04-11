package org.jenkinsci.plugins.autocompleteparameter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import hudson.Util;
import hudson.slaves.EnvironmentVariablesNodeProperty;
import jenkins.model.Jenkins;

public class GlobalVariableUtils {
	public static String resolveVariables(String str) {
		Map<String, String> vars = getGlobalVariables();
		return Util.replaceMacro(str, vars);
	}

	public static Map<String, String> getGlobalVariables() {
		Map<String, String> vars = new LinkedHashMap<String, String>();
		List<EnvironmentVariablesNodeProperty> all = Jenkins.getInstance().getGlobalNodeProperties().getAll(EnvironmentVariablesNodeProperty.class);
		for (EnvironmentVariablesNodeProperty environmentVariablesNodeProperty : all) 
			vars.putAll(environmentVariablesNodeProperty.getEnvVars());
		
		vars.putAll(System.getenv());
		return vars;
	}
}
