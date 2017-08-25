package org.jenkinsci.plugins.autocompleteparameter.providers;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang.NotImplementedException;
import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.model.Descriptor;

public class SimpleTextProvider extends AutocompleteDataProvider {
	private static final long serialVersionUID = -9208140863503788547L;
	private String autoCompleteData;
	
	@DataBoundConstructor
	public SimpleTextProvider(String autoCompleteData) {
		this.autoCompleteData = autoCompleteData;
	}

	@Override
	public Collection<?> getData() {
		return Arrays.asList(autoCompleteData.split("\n"));
	}

	@Override
	public Collection<?> filter(String query) {
		throw new NotImplementedException("Filter not implemented for " + getClass().getSimpleName());
	}

	public String getAutoCompleteData() {
		return autoCompleteData;
	}
	
	public void setAutoCompleteData(String autoCompleteData) {
		this.autoCompleteData = autoCompleteData;
	}
	
	@Extension
	public static final class DescriptorImpl extends Descriptor<AutocompleteDataProvider> {
		@Override
		public String getDisplayName() {
			return "List of text values";
		}
	}
}
