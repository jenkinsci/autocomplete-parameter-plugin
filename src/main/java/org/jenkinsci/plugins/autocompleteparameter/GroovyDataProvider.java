package org.jenkinsci.plugins.autocompleteparameter;

import hudson.Extension;
import hudson.model.Descriptor;

public class GroovyDataProvider extends AutocompleteDataProvider {
	private String script;
	private boolean useSandbox;
	
	@Override
	public String getData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Extension
	public static final class DescriptorImpl extends Descriptor<AutocompleteDataProvider> {
		@Override
		public String getDisplayName() {
			return "Groovy script";
		}
	}
}
