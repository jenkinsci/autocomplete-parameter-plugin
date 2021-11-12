package org.jenkinsci.plugins.autocompleteparameter.pages;

import hudson.model.Descriptor;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.Project;
import org.jenkinsci.plugins.autocompleteparameter.DropdownAutocompleteParameterDefinition;
import org.jenkinsci.plugins.autocompleteparameter.providers.AutocompleteDataProvider;
import org.jenkinsci.plugins.autocompleteparameter.providers.GroovyDataProvider;
import org.jenkinsci.plugins.autocompleteparameter.providers.InlineJsonDataProvider;
import org.jenkinsci.plugins.autocompleteparameter.providers.RemoteDataProvider;
import org.jenkinsci.plugins.autocompleteparameter.providers.SimpleTextProvider;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class JobConfigurationPage extends AbstractPage {

    private final Project project;

    protected JobConfigurationPage(WebDriver webDriver, Project project) {
        super(webDriver);
        this.project = project;
    }

    public Parameter getParameter(String name) {
        WebElement parentDiv = webDriver.findElement(By.xpath("//div[@name='parameterDefinitions'][descendant::input[@value='" + name + "']]"));
        return new Parameter(parentDiv, name);
    }

    public enum DataProviderType {
        GROOVY(GroovyDataProvider.class)
        , INLINE(InlineJsonDataProvider.class)
        , SIMPLE(SimpleTextProvider.class)
        , REMOTE(RemoteDataProvider.class);

        private final Class<? extends AutocompleteDataProvider> providerClass;

        DataProviderType(Class<? extends AutocompleteDataProvider> providerClass) {
            this.providerClass = providerClass;
        }

        public static DataProviderType valueFor(Class cl) {
            for(DataProviderType type : values()) {
                if(type.providerClass == cl)
                    return type;
            }
            throw new IllegalArgumentException(
                    "No enum constant for " + cl.getSimpleName());
        }
    }

    public class Parameter {

        private final WebElement parentDiv;
        private final String name;
        private final WebElement dataProviderSelector;
        private final WebElement dataProvider;

        public Parameter(WebElement parentDiv, String name) {
            this.parentDiv = parentDiv;
            this.name = name;
            this.dataProviderSelector = parentDiv.findElement(By.cssSelector("select.setting-input.dropdownList"));
            this.dataProvider = parentDiv.findElement(By.cssSelector("div[name=dataProvider]"));
        }

        public DataProvider getDataProvider() {
            return new DataProvider();
        }

        public class DataProvider {

            public DataProviderType getType() {
                int selectedId = Integer.parseInt(dataProviderSelector.getAttribute("value"));
                Descriptor<AutocompleteDataProvider> descriptor = AutocompleteDataProvider.all().get(selectedId);
                return DataProviderType.valueFor(descriptor.clazz);
            }

            public String getMainValue() {
                DataProviderType type = getType();
                switch(type) {
                    case GROOVY:
                        return dataProvider.findElement(By.cssSelector("textarea[name='_.script']")).getAttribute("value");
                    case INLINE:
                        return dataProvider.findElement(By.cssSelector("textarea[name='_.autoCompleteData']")).getAttribute("value");
                    case SIMPLE:
                        return dataProvider.findElement(By.cssSelector("textarea[name='parameter.autoCompleteData']")).getAttribute("value");
                    case REMOTE:
                        return dataProvider.findElement(By.cssSelector("input[name='parameter.autoCompleteUrl']")).getAttribute("value");
                    default:
                        throw new RuntimeException("Unexpected data provider " + type);
                }
            }

            public String getPersistedMainValue() {
                ParametersDefinitionProperty property = (ParametersDefinitionProperty) project.getProperty(ParametersDefinitionProperty.class);
                DropdownAutocompleteParameterDefinition definition = (DropdownAutocompleteParameterDefinition) property.getParameterDefinition(name);

                AutocompleteDataProvider provider = definition.getDataProvider();
                if(provider instanceof GroovyDataProvider)
                    return ((GroovyDataProvider)provider).getScript();
                if(provider instanceof InlineJsonDataProvider)
                    return ((InlineJsonDataProvider)provider).getAutoCompleteData();
                if(provider instanceof SimpleTextProvider)
                    return ((SimpleTextProvider)provider).getAutoCompleteData();
                if(provider instanceof RemoteDataProvider)
                    return ((RemoteDataProvider)provider).getAutoCompleteUrl();
                throw new RuntimeException("Unexpected data provider " + provider.getClass());
            }
        }
    }
}
