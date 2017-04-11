package org.jenkinsci.plugins.autocompleteparameter;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jenkinsci.plugins.scriptsecurity.sandbox.groovy.SecureGroovyScript;
import org.jenkinsci.plugins.scriptsecurity.scripts.ClasspathEntry;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import groovy.lang.Binding;
import hudson.Extension;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import net.sf.json.JSONSerializer;

public class GroovyDataProvider extends AutocompleteDataProvider {
	private String script;
	private boolean sandbox;
	private List<ClasspathEntry> classpath;
	
	@DataBoundConstructor
	public GroovyDataProvider(String script, boolean sandbox, List<ClasspathEntry> classpath) {
		this.script = script;
		this.sandbox = sandbox;
		this.classpath = classpath;
	}
	
	@Override
	public String getData() {
       return runScript(script, sandbox, classpath);
	}
	
	public String getScript() {
		return script;
	}

	public void setScript(String script) {
		this.script = script;
	}
	
	public List<ClasspathEntry> getClasspath() {
		return classpath;
	}
	
	public void setClasspath(List<ClasspathEntry> classpath) {
		this.classpath = classpath;
	}

	public boolean getSandbox() {
		return sandbox;
	}
	
	public void setSandbox(boolean sandbox) {
		this.sandbox = sandbox;
	} 

	@Extension
	public static final class DescriptorImpl extends Descriptor<AutocompleteDataProvider> {
		@Override
		public String getDisplayName() {
			return "Groovy script";
		}
		
        public FormValidation doTest(StaplerRequest req, @QueryParameter String script, @QueryParameter boolean sandbox, List<ClasspathEntry> classpath)
        {
            try
            {
            	return FormValidation.ok(runScript(script, sandbox, classpath));
            }
            catch(Exception e)
            {
                return FormValidation.error(e, "Failed to execute script");
            }
        }

	}
	
	private static String runScript(String script, boolean sandbox, List<ClasspathEntry> classpath) {
		if (classpath == null)
			classpath = Collections.<ClasspathEntry>emptyList();
		
		SecureGroovyScript groovyScript = new SecureGroovyScript(script, sandbox, classpath).configuringWithKeyItem();
		
		ClassLoader cl = Jenkins.getInstance().getPluginManager().uberClassLoader;

        if (cl == null) 
            cl = Thread.currentThread().getContextClassLoader();

        Binding binding = new Binding();
        for (Entry<String, String> envVar : GlobalVariableUtils.getGlobalVariables().entrySet()) 
			binding.setVariable(envVar.getKey(), envVar.getValue());
        
        Object out;
		try {
			out = groovyScript.evaluate(cl, binding);
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalStateException(e);
		}

        if(out == null)
        	return "";
        
        return JSONSerializer.toJSON(out).toString();
	}	
}
