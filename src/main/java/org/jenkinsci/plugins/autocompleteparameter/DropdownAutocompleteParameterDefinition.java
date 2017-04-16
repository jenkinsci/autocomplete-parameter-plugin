package org.jenkinsci.plugins.autocompleteparameter;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jenkinsci.plugins.autocompleteparameter.providers.AutocompleteDataProvider;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.Exported;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.ParameterValue;
import hudson.model.SimpleParameterDefinition;
import hudson.model.StringParameterValue;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

@SuppressWarnings("serial")
public class DropdownAutocompleteParameterDefinition extends SimpleParameterDefinition {
	private AutocompleteDataProvider dataProvider;
	private String displayExpression;
	private String valueExpression;
	private String defaultValue;
	
	@DataBoundConstructor
	public DropdownAutocompleteParameterDefinition(String name, 
			String description, 
			String displayExpression,
			String valueExpression,
			String defaultValue,
			AutocompleteDataProvider dataProvider) 
	{
		super(name, description);
		this.displayExpression = displayExpression;
		this.valueExpression = valueExpression;
		this.defaultValue = defaultValue;
		this.dataProvider = dataProvider;
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
	public String getValueExpression() {
		return valueExpression;
	}
	
	@Exported
	public String getDisplayExpressionJsSafe() {
		return Utils.normalizeExpression(displayExpression);
	}
	
	@Exported
	public String getValueExpressionJsSafe() {
		return Utils.normalizeExpression(valueExpression);
	}
	
	public void setValueExpression(String valueExpression) {
		this.valueExpression = valueExpression;
	}
	
	@Exported
	public String getDefaultValue() {
		return defaultValue;
	}
	
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	@Exported
	public Map<String, String> getChoices() {
		Collection<?> data = dataProvider.getData();
		
		LinkedHashMap<String, String> choices = new LinkedHashMap<String, String>();
		String expr = displayExpression;
		
		for (Object object : data) {
			String json;
			try {
				json = JSONUtils.toJSON(object);
			}catch(JSONException e) {
				json = object.toString();
			}
			choices.put(json, expr);
		}
		return choices;
	}

    @Override
    public ParameterValue createValue(StaplerRequest req, JSONObject jo) {
        StringParameterValue value = req.bindJSON(StringParameterValue.class, jo);
        value.setDescription(getDescription());
        return value;
    }

    @Override
    public ParameterValue createValue(String value) {
        return new StringParameterValue(getName(), value, getDescription());
    }	
	
	@Extension
    public static final class DescriptImpl extends ParameterDescriptor {

        @Override
        public String getDisplayName() {
            return "Dropdown Autocomplete Parameter";
        }
        
        public List<Descriptor<AutocompleteDataProvider>> getDataProviders() {
        	return AutocompleteDataProvider.all();
        }
    }

}
