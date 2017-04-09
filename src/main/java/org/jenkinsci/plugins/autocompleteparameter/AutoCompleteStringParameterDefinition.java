package org.jenkinsci.plugins.autocompleteparameter;

import static net.sf.json.JSONSerializer.toJSON;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.Exported;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;

import hudson.Extension;
import hudson.model.Item;
import hudson.model.ParameterValue;
import hudson.model.StringParameterDefinition;
import hudson.security.ACL;
import hudson.util.ListBoxModel;
import net.sf.json.JSONObject;

public class AutoCompleteStringParameterDefinition extends StringParameterDefinition {
	private static final long serialVersionUID = 2691768740499486855L;
	private String autoCompleteData;
	private String labelField;
	private String dataSourceType;
	private String credentialsId;
	private String autoCompleteUrl;
	
	@DataBoundConstructor
	public AutoCompleteStringParameterDefinition(String name, 
			String defaultValue, 
			String description, 
			String labelField, 
			String autoCompleteData,
			String autoCompleteUrl,
			String credentialsId,
			String dataSourceType) 
	{
		super(name, defaultValue, description);
		this.labelField = labelField;
		this.autoCompleteData = autoCompleteData;
		this.autoCompleteUrl = autoCompleteUrl;
		this.credentialsId = credentialsId;
		this.dataSourceType = dataSourceType;
	}

	@Override
	public ParameterValue createValue(String value) {
		return super.createValue(value);
	}

	@Override
	public ParameterValue createValue(StaplerRequest req, JSONObject jo) {
		return super.createValue(req,jo);
	}
	
	@Exported
	public String getAutoCompleteData() {
		return autoCompleteData;
	}
	
	@Exported
	public String getDataSourceType() {
		return dataSourceType;
	}
	
	public void setDataSourceType(String dataSourceType) {
		this.dataSourceType = dataSourceType;
	}

	public void setAutoCompleteValues(String autoCompleteValues) {
		this.autoCompleteData = autoCompleteValues;
	}	
	
	@Exported
	public String getLabelField() {
		return labelField;
	}
		
	public void setLabelField(String labelField) {
		this.labelField = labelField;
	}
	
	@Exported
	public String getLabelFieldJsSafe() {
		return labelField.replace("\"", "\\\"");
	}
	
	@Exported
	public String getAutoCompleteValuesScript() {
		try {
			switch(dataSourceType) {
				case "source.groovyScript":
					break;
				case "source.httpRequest":
					return getRequestData();
				case "source.inlineJson":
					return autoCompleteData;
				case "source.inlineList":
					return toJSON(Arrays.asList(autoCompleteData.split("\n"))).toString();
			}
		}catch(Exception e) {
			return "'ERROR: Autocomplete data generation failure: " + e.getMessage()+"'";
		}
		return autoCompleteData;
	}

	@Exported
	public String getCredentialsId() {
		return credentialsId;
	}
	
	@Exported
	public String getAutoCompleteUrl() {
		return autoCompleteUrl;
	}
	
	public void setAutoCompleteUrl(String autoCompleteUrl) {
		this.autoCompleteUrl = autoCompleteUrl;
	}

	public void setCredentialsId(String credentialsId) {
		this.credentialsId = credentialsId;
	}
	
	private String getRequestData()  {
		StandardUsernamePasswordCredentials credentials = lookupCredentials(autoCompleteData);
		return performRequest(autoCompleteUrl, credentials);
	}

	private StandardUsernamePasswordCredentials lookupCredentials(String uri) {
		return CredentialsMatchers.firstOrNull(
				CredentialsProvider.lookupCredentials(
						StandardUsernamePasswordCredentials.class,
						(Item)null,
						ACL.SYSTEM,
						URIRequirementBuilder.fromUri(uri).build()),
					CredentialsMatchers.withId(getCredentialsId())
				);
	}

	private static String performRequest(String uri, StandardUsernamePasswordCredentials credentials) {
		try {
			URL url = new URL(uri);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.addRequestProperty("Accept-Encoding", "gzip,deflate");
			conn.addRequestProperty("Accept", "*/*");
			if (credentials != null) {
				String auth = Base64.encodeBase64String((credentials.getUsername() + ":" + credentials.getPassword()).getBytes());
				conn.addRequestProperty("Authorization", "Basic " + auth);
			}
			return IOUtils.toString(conn.getInputStream(), "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalStateException(e);
		}
	}

	@Extension
    public static final class DescriptImpl extends ParameterDescriptor {

        @Override
        public String getDisplayName() {
            return "Auto Complete String Parameter";
        }
        
        public ListBoxModel doFillCredentialsIdItems(@AncestorInPath Item context, @QueryParameter String credentialsId) {
            return new StandardListBoxModel()
            		.includeEmptyValue()
            		.includeAs(ACL.SYSTEM, context, StandardUsernamePasswordCredentials.class)
            		.includeCurrentValue(credentialsId);
        }
        
        public ListBoxModel doFillDataSourceTypeItems(@AncestorInPath Item context, @QueryParameter String dataSourceType) {
        	ListBoxModel boxModel = new ListBoxModel();
        	boxModel.add(new ListBoxModel.Option("Simple List of Values", "source.inlineList"));
        	boxModel.add(new ListBoxModel.Option("Inline Json",           "source.inlineJson"));
        	boxModel.add(new ListBoxModel.Option("Remote Request",        "source.httpRequest"));
        	boxModel.add(new ListBoxModel.Option("Groovy Script",         "source.groovyScript"));
        	Iterator<hudson.util.ListBoxModel.Option> it = boxModel.iterator();
        	while (it.hasNext()) {
        		hudson.util.ListBoxModel.Option option = it.next();
        		option.selected = option.value.equals(dataSourceType);
        	}

			return boxModel;
        }
    }
}
