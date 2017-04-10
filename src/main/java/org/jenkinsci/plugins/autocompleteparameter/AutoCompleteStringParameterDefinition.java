package org.jenkinsci.plugins.autocompleteparameter;

import java.util.List;

import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.Exported;

import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Item;
import hudson.model.ParameterValue;
import hudson.model.StringParameterDefinition;
import hudson.security.ACL;
import hudson.util.ListBoxModel;
import net.sf.json.JSONObject;

public class AutoCompleteStringParameterDefinition extends StringParameterDefinition {
	private static final long serialVersionUID = 2691768740499486855L;
	private String labelField;
	private AutocompleteDataProvider dataProvider;
	
	@DataBoundConstructor
	public AutoCompleteStringParameterDefinition(String name, 
			String defaultValue, 
			String description, 
			String labelField,
			AutocompleteDataProvider dataProvider) 
	{
		super(name, defaultValue, description);
		this.labelField = labelField;
		this.dataProvider = dataProvider;
	}

	@Override
	public ParameterValue createValue(String value) {
		return super.createValue(value);
	}

	@Override
	public ParameterValue createValue(StaplerRequest req, JSONObject jo) {
		return super.createValue(req,jo);
	}
	
	public AutocompleteDataProvider getDataProvider() {
		return dataProvider;
	}
	
	public void setDataProvider(AutocompleteDataProvider dataProvider) {
		this.dataProvider = dataProvider;
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
			return dataProvider.getData();
		}catch(Exception e) {
			return "'ERROR: Autocomplete data generation failure: " + e.getMessage()+"'";
		}
	}

	@Extension
    public static final class DescriptImpl extends ParameterDescriptor {

        @Override
        public String getDisplayName() {
            return "Auto Complete String Parameter";
        }
        
        public List<Descriptor<AutocompleteDataProvider>> getDataProviders() {
        	return AutocompleteDataProvider.all();
        }
    }
}
