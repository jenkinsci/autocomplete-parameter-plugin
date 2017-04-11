package org.jenkinsci.plugins.autocompleteparameter.providers;

import static net.sf.json.JSONSerializer.toJSON;

import java.util.Arrays;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.model.Descriptor;

public class SimpleTextProvider extends AutocompleteDataProvider {
	private String autoCompleteData;
	
	@DataBoundConstructor
	public SimpleTextProvider(String autoCompleteData) {
		this.autoCompleteData = autoCompleteData;
	}

	@Override
	public String getData() {
		return toJSON(Arrays.asList(autoCompleteData.split("\n"))).toString();
	}
	
	@Extension
	public static final class DescriptorImpl extends Descriptor<AutocompleteDataProvider> {
		@Override
		public String getDisplayName() {
			return "List of text values";
		}
	}
}
