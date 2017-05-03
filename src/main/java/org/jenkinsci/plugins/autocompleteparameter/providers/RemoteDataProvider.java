package org.jenkinsci.plugins.autocompleteparameter.providers;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;

import org.apache.commons.io.IOUtils;
import org.jenkinsci.plugins.autocompleteparameter.CredentialsUtils;
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

public class RemoteDataProvider extends AutocompleteDataProvider {
	private static final long serialVersionUID = 5773462762109544336L;
	
	private String autoCompleteUrl;
	private String credentialsId;
	
	@DataBoundConstructor
	public RemoteDataProvider(String autoCompleteUrl, String credentialsId) {
		this.autoCompleteUrl = autoCompleteUrl;
		this.credentialsId = credentialsId;
	}

	@Override
	public Collection<?> getData() {
		return JSONUtils.toCanonicalCollection(performRequest(autoCompleteUrl, credentialsId));
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

		public ListBoxModel doFillCredentialsIdItems(@AncestorInPath Item context, @QueryParameter String credentialsId) {
            return new StandardListBoxModel()
            		.includeEmptyValue()
            		.includeAs(ACL.SYSTEM, context, StandardUsernamePasswordCredentials.class)
            		.includeCurrentValue(credentialsId);
        }
	}
	
	private static String performRequest(String uri, String credentialsId) {
		uri = GlobalVariableUtils.resolveVariables(uri);
		try {
			URL url = new URL(uri);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.addRequestProperty("Accept", "*/*");
			CredentialsUtils.addAuth(uri,  credentialsId, conn);
			return IOUtils.toString(conn.getInputStream(), "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalStateException(e);
		}
	}	
}
