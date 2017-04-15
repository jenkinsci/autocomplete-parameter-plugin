package org.jenkinsci.plugins.autocompleteparameter;

import jenkins.model.Jenkins;

public class SafeJenkins {
	public static Jenkins getInstanceOrCry() {
        Jenkins instance = Jenkins.getInstance();
        if (instance == null)
        	throw new IllegalStateException("Jenkins not ready");
        return instance;
	}
}
