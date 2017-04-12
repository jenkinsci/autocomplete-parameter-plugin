package org.jenkinsci.plugins.autocompleteparameter;

import java.util.List;

import org.jenkinsci.plugins.autocompleteparameter.providers.AutocompleteDataProvider;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.Exported;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.ParameterValue;
import hudson.model.StringParameterDefinition;
import net.sf.json.JSONObject;

public class AutoCompleteStringParameterDefinition extends StringParameterDefinition {
	private static final long serialVersionUID = 2691768740499486855L;
	private String labelField;
	private AutocompleteDataProvider dataProvider;
	private boolean allowUnrecognizedTokens;
	
	@DataBoundConstructor
	public AutoCompleteStringParameterDefinition(String name, 
			String defaultValue, 
			String description, 
			String labelField,
			boolean allowUnrecognizedTokens,
			AutocompleteDataProvider dataProvider) 
	{
		super(name, defaultValue, description);
		this.labelField = labelField;
		this.allowUnrecognizedTokens = allowUnrecognizedTokens;
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
			return JSONUtils.toJSON(dataProvider.getData());
		}catch(Exception e) {
			return "'ERROR: Autocomplete data generation failure: " + e.getMessage()+"'";
		}
	}
	
	public boolean isAllowUnrecognizedTokens() {
		return allowUnrecognizedTokens;
	}

	public void setAllowUnrecognizedTokens(boolean allowUnrecognizedTokens) {
		this.allowUnrecognizedTokens = allowUnrecognizedTokens;
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
