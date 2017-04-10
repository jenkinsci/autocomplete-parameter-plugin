package org.jenkinsci.plugins.autocompleteparameter;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.model.Descriptor;

public class InlineJsonDataProvider extends AutocompleteDataProvider {

	private String autoCompleteData;

	@DataBoundConstructor
	public InlineJsonDataProvider(String autoCompleteData) {
		this.autoCompleteData = autoCompleteData;
	}

	@Override
	public String getData() {
		return autoCompleteData;
	}
	
	@Extension
	public static final class DescriptorImpl extends Descriptor<AutocompleteDataProvider> {
		@Override
		public String getDisplayName() {
			return "Inline Json Array";
		}
	}
}
