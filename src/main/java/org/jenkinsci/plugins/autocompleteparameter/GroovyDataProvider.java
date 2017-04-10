package org.jenkinsci.plugins.autocompleteparameter;

import java.util.Collections;
import java.util.List;

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
	private SecureGroovyScript script;
	
	@DataBoundConstructor
	public GroovyDataProvider(SecureGroovyScript script) {
		this.setScript(script.configuringWithKeyItem());
	}
	
	@Override
	public String getData() {
        ClassLoader cl = Jenkins.getInstance().getPluginManager().uberClassLoader;

        if (cl == null) 
            cl = Thread.currentThread().getContextClassLoader();

        Binding binding = new Binding();
        
        Object out;
		try {
			out = getScript().evaluate(cl, binding);
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalStateException(e);
		}

        if(out == null)
        	return "";
        
        return JSONSerializer.toJSON(out).toString();
	}
	
	private static String runScript(SecureGroovyScript script) {
		 ClassLoader cl = Jenkins.getInstance().getPluginManager().uberClassLoader;

	        if (cl == null) 
	            cl = Thread.currentThread().getContextClassLoader();

	        Binding binding = new Binding();
	        
	        binding.setVariable("jenkins", Jenkins.getInstance());
	        
	        Object out;
			try {
				out = script.evaluate(cl, binding);
			} catch (Exception e) {
				e.printStackTrace();
				throw new IllegalStateException(e);
			}

	        if(out == null)
	        	return "";
	        
	        return JSONSerializer.toJSON(out).toString();
	}

	public SecureGroovyScript getScript() {
		return script;
	}

	public void setScript(SecureGroovyScript script) {
		this.script = script;
	}

	@Extension
	public static final class DescriptorImpl extends Descriptor<AutocompleteDataProvider> {
		@Override
		public String getDisplayName() {
			return "Groovy script";
		}
		
        public FormValidation doTest(StaplerRequest req, @QueryParameter String script, @QueryParameter boolean sandbox)
        {
            try
            {
            	return FormValidation.ok(runScript(new SecureGroovyScript(script, sandbox, Collections.<ClasspathEntry>emptyList()).configuringWithKeyItem()));
            }
            catch(Exception e)
            {
                return FormValidation.error(e, "Failed to execute script");
            }
        }

	}
}
