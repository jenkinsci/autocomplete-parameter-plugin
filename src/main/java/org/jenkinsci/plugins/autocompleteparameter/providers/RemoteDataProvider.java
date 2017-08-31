package org.jenkinsci.plugins.autocompleteparameter.providers;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import hudson.util.FormValidation;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrSubstitutor;
import org.jenkinsci.plugins.autocompleteparameter.GlobalVariableUtils;
import org.jenkinsci.plugins.autocompleteparameter.JSONUtils;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.export.Exported;

import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Item;
import hudson.security.ACL;
import hudson.util.ListBoxModel;
import wirelabs.commons.RequestBuilder;

public class RemoteDataProvider extends AutocompleteDataProvider {
	private static final long serialVersionUID = 5773462762109544336L;

	private boolean prefetch;
	private String autoCompleteUrl;
	private String credentialsId;
	
	@DataBoundConstructor
	public RemoteDataProvider(boolean prefetch, String autoCompleteUrl, String credentialsId) {
		this.prefetch = prefetch;
		this.autoCompleteUrl = autoCompleteUrl;
		this.credentialsId = credentialsId;
	}

	@Override
	public Collection<?> getData() {
		return JSONUtils.toCanonicalCollection(performRequest(autoCompleteUrl, credentialsId));
	}

	@Override
	public Collection<?> filter(String query) {
		Map<String, Object> parameters = new HashMap<>();
		try {
			parameters.put("query", URLEncoder.encode(query, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		String replaced = StrSubstitutor.replace(autoCompleteUrl, parameters);
		if(autoCompleteUrl.equals(replaced))
			replaced = autoCompleteUrl + query;
		return JSONUtils.toCanonicalCollection(
				performRequest(
						replaced
						, credentialsId
				)
		);
	}

	@Override
	public boolean isPrefetch() {
		return prefetch;
	}

	@Exported
	public String getCredentialsId() {
		return credentialsId;
	}
	
	@Exported
	public String getAutoCompleteUrl() {
		return autoCompleteUrl;
	}
	
	@Extension
	public static final class DescriptorImpl extends Descriptor<AutocompleteDataProvider> {
		@Override
		public String getDisplayName() {
			return "Remote request";
		}

		public FormValidation doCheckAutoCompleteUrl(@QueryParameter boolean prefetch, @QueryParameter String value) {
			if(StringUtils.isEmpty(value))
				return FormValidation.error("Invalid URL");
			try {
				new URL(GlobalVariableUtils.resolveVariables(value));
			} catch (MalformedURLException e) {
				return FormValidation.error("Invalid URL: " + e.getMessage());
			}
			if(!prefetch && !value.contains("${query}"))
				return FormValidation.respond(FormValidation.Kind.OK, "<div class=\"observation\">Optional: You may add ${query} to url and it will replace with typed text.<br/>e.g.: http://remote/search?q=${query}<br/>e.g.: http://remote/search/${query}.json</div>");
			return FormValidation.ok();
		}

		public ListBoxModel doFillCredentialsIdItems(@AncestorInPath Item context, @QueryParameter String credentialsId) {
            return new StandardListBoxModel()
            		.includeEmptyValue()
            		.includeAs(ACL.SYSTEM, context, StandardUsernamePasswordCredentials.class)
            		.includeCurrentValue(credentialsId);
        }
	}
	
	private static String performRequest(String uri, String credentialsId) {
		return RequestBuilder
				.url(GlobalVariableUtils.resolveVariables(uri))
				.cache()
				.header("Accept", "*/*")
				.credentials(credentialsId)
				.get()
				.content;
	}
}
