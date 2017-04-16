package org.jenkinsci.plugins.autocompleteparameter.providers;

import java.util.Collection;
import java.util.Collections;

import org.jenkinsci.plugins.autocompleteparameter.JSONUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.util.FormValidation;

public class InlineJsonDataProvider extends AutocompleteDataProvider {

	private static final long serialVersionUID = 5282725458728513422L;
	private String autoCompleteData;

	@DataBoundConstructor
	public InlineJsonDataProvider(String autoCompleteData) {
		this.autoCompleteData = autoCompleteData;
	}

	@Override
	public Collection<?> getData() {
		if (autoCompleteData == null)
			return Collections.emptyList();
		
		return JSONUtils.toCanonicalCollection(autoCompleteData);
	}

	@Extension
	public static final class DescriptorImpl extends Descriptor<AutocompleteDataProvider> {
		@Override
		public String getDisplayName() {
			return "Inline Json Array";
		}
		
		
		public FormValidation doCheckAutoCompleteData(@QueryParameter String autoCompleteData) {
			try {
				JSONUtils.toCanonicalCollection(autoCompleteData);
				return FormValidation.ok();
			}catch(Exception e) {
				return FormValidation.error(e.getMessage());
			}
		}
	}
}
