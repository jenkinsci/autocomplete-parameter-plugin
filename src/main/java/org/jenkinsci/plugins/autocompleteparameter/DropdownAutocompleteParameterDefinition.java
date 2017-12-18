package org.jenkinsci.plugins.autocompleteparameter;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.jenkinsci.plugins.autocompleteparameter.providers.AutocompleteDataProvider;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.bind.JavaScriptMethod;
import org.kohsuke.stapler.export.Exported;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.ParameterValue;
import hudson.model.SimpleParameterDefinition;
import hudson.model.StringParameterValue;
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
        } catch (Exception e) {
            return "'ERROR: Dropdown data generation failure: " + e.getMessage() + "'";
        }
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
