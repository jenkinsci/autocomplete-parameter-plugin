package org.jenkinsci.plugins.autocompleteparameter;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.jenkinsci.plugins.autocompleteparameter.providers.AutocompleteDataProvider;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.bind.JavaScriptMethod;
import org.kohsuke.stapler.export.Exported;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.ParameterValue;
import hudson.model.StringParameterDefinition;
import net.sf.json.JSONObject;

public class AutoCompleteStringParameterDefinition extends StringParameterDefinition {
	private static final long serialVersionUID = 2691768740499486855L;
	private String displayExpression;
	private String valueExpression;
	private AutocompleteDataProvider dataProvider;
	private boolean allowUnrecognizedTokens;

	@DataBoundConstructor
	public AutoCompleteStringParameterDefinition(String name, 
			String defaultValue, 
			String description, 
			String displayExpression,
			String valueExpression,
			boolean allowUnrecognizedTokens,
			AutocompleteDataProvider dataProvider)
	{
		super(name, defaultValue, description);
		this.valueExpression = valueExpression;
		this.displayExpression = displayExpression;
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
	public String getDisplayExpression() {
		return displayExpression;
	}
		
	@Exported
	public void setDisplayExpression(String labelField) {
		this.displayExpression = labelField;
	}
	
	@Exported
	public String getValueExpression() {
		return valueExpression;
	}

	@Exported
	public void setValueExpression(String valueExpression) {
		this.valueExpression = valueExpression;
	}
	
	@Exported
	public String getDisplayExpressionJsSafe() {
		return Utils.normalizeExpression(displayExpression);
	}
	
	@Exported
	public String getValueExpressionJsSafe() {
		return Utils.normalizeExpression(valueExpression);
	}
	
	@Exported
	public String getAutoCompleteValuesScript() {
		if(isPrefetch()) {
			try {
				return JSONUtils.toJSON(dataProvider.getData(2, TimeUnit.MINUTES));
			} catch (Exception e) {
				return "'ERROR: Autocomplete data generation failure: " + e.getMessage() + "'";
			}
		} else {
			return "[]";
		}
	}

	@Exported
	public boolean isPrefetch() {
		return dataProvider.isPrefetch();
	}

	@JavaScriptMethod
	public String filterAutoCompleteValues(String query) {
		try {
			return JSONUtils.toJSON(dataProvider.filter(query, 30, TimeUnit.SECONDS));
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

    @Override
	public boolean equals(Object other) {
		return super.equals(other);
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}
}
