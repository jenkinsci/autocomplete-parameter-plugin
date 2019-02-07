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
	private String xpath;

	@DataBoundConstructor
	public RemoteDataProvider(boolean prefetch, String autoCompleteUrl, String credentialsId, String xpath) {
		this.prefetch = prefetch;
		this.autoCompleteUrl = autoCompleteUrl;
		this.credentialsId = credentialsId;
		this.xpath = xpath;
	}

	@Override
	public Collection<?> getData() {
		return mapData(performRequest(autoCompleteUrl, credentialsId));
	}

	@Override
	public Collection<?> filter(String query) {
		Map<String, Object> parameters = new HashMap<>();
		try {
			parameters.put("query", URLEncoder.encode(query, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		String url = StrSubstitutor.replace(autoCompleteUrl, parameters);
		return mapData(performRequest(url, credentialsId));
	}

	@Override
	public boolean isPrefetch() {
		return prefetch;
	}

	@Exported
	public String getXpath() {
		return xpath;
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

	private Collection<?> mapData(String data) {
        String options = JSONUtils.traverseJson(data, xpath);
        return JSONUtils.toCanonicalCollection(options);
	}
}
