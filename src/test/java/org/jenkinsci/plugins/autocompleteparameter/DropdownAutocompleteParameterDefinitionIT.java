package org.jenkinsci.plugins.autocompleteparameter;

import hudson.model.FreeStyleProject;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.Project;
import org.jenkinsci.plugins.autocompleteparameter.pages.BuildWithParametersPage;
import org.jenkinsci.plugins.autocompleteparameter.pages.BuildWithParametersPage.DropdownParameter;
import org.jenkinsci.plugins.autocompleteparameter.pages.JobConfigurationPage;
import org.jenkinsci.plugins.autocompleteparameter.pages.JobConfigurationPage.Parameter;
import org.jenkinsci.plugins.autocompleteparameter.pages.JobConfigurationPage.Parameter.DataProvider;
import org.jenkinsci.plugins.autocompleteparameter.pages.JobConfigurationPage.DataProviderType;
import org.jenkinsci.plugins.autocompleteparameter.pages.Pages;
import org.jenkinsci.plugins.autocompleteparameter.providers.GroovyDataProvider;
import org.jenkinsci.plugins.autocompleteparameter.providers.InlineJsonDataProvider;
import org.jenkinsci.plugins.autocompleteparameter.providers.RemoteDataProvider;
import org.jenkinsci.plugins.autocompleteparameter.providers.SimpleTextProvider;
import org.junit.Test;
import org.openqa.selenium.Keys;

import java.io.IOException;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.jenkinsci.plugins.autocompleteparameter.SeleniumMatchers.isVisible;
import static org.junit.Assert.assertThat;

public class DropdownAutocompleteParameterDefinitionIT extends AbstractUiIT {

    public Project setupJobWithRemoteDataProvider(RemoteServerMock server) throws IOException {
        String endpoint = server.getAddress() + "/rest/users";
        String slowEndpoint = server.getAddress() + "/rest/users?slow=true";
        FreeStyleProject project = j.createFreeStyleProject(testName.getMethodName());
        DropdownAutocompleteParameterDefinition prefetchedParameter = new DropdownAutocompleteParameterDefinition("leader", "", "name", "email", "beethoven@mail.com"
                , new RemoteDataProvider(true, endpoint, "credentials"));
        DropdownAutocompleteParameterDefinition asyncParameter = new DropdownAutocompleteParameterDefinition("sub-leader", "", "name", "email", ""
                , new RemoteDataProvider(false, slowEndpoint, "credentials"));
        project.addProperty(new ParametersDefinitionProperty(
                prefetchedParameter, asyncParameter
        ));
        return project;
    }

    public Project setupJobWithAllDataProviders() throws IOException {
        FreeStyleProject project = j.createFreeStyleProject(testName.getMethodName());
        DropdownAutocompleteParameterDefinition remoteParameter = new DropdownAutocompleteParameterDefinition("remote", "", "description", "full_name", ""
                , new RemoteDataProvider(true, "https://api.github.com/search/repositories?q=${query}+user:jenkinsci", null));
        DropdownAutocompleteParameterDefinition groovyParameter = new DropdownAutocompleteParameterDefinition("groovy", "", "value", "key", ""
                , new GroovyDataProvider("return ['1':'One', '2':'Two', '3':'Three', '5':'Five', '8':'Eight', '13':'Thirteen'].entrySet()"
                , true, null));
        DropdownAutocompleteParameterDefinition inlineParameter = new DropdownAutocompleteParameterDefinition("inline", "", "name", "id", ""
                , new InlineJsonDataProvider("["
                + "{'name':'Eddard Stark','id':'estark', 'house':'Stark'}"
                + ",{'name':'Jon Snow','id':'jsnow', 'house':'Stark'}"
                + ",{'name':'Tyrion Lannister','id':'tlannister', 'house':'Lannister'}"
                + ",{'name':'Robert Baratheon','id':'rbaratheon', 'house':'Baratheon'}"
                + "]"));
        DropdownAutocompleteParameterDefinition simpleParameter = new DropdownAutocompleteParameterDefinition("simple", "", "value.split('|')[1]", "value.split('|')[0]", ""
                , new SimpleTextProvider(
                        "al|Ada Lovelace\n"
                        + "sj|Steve Jobs\n"
                        + "bg|Bill Gates\n"
                        + "lt|Linus Torvalds\n"
        ));

        project.addProperty(new ParametersDefinitionProperty(
                remoteParameter, groovyParameter, inlineParameter, simpleParameter
        ));
        return project;
    }

    public Project setupJobWithInlineJsonDataProvider(String json, String displayExpression, String valueExpression) throws IOException {
        FreeStyleProject project = j.createFreeStyleProject(testName.getMethodName());

        DropdownAutocompleteParameterDefinition inlineParameter = new DropdownAutocompleteParameterDefinition("inline", "", displayExpression, valueExpression, ""
                , new InlineJsonDataProvider(json));

        project.addProperty(new ParametersDefinitionProperty(
                inlineParameter
        ));
        return project;
    }

    public RemoteServerMock remoteServer() {
        return new RemoteServerMock();
    }

    @Test
    public void remoteDataProvider() throws IOException {
        try(RemoteServerMock server = remoteServer()) {
            server.start();
            Project project = setupJobWithRemoteDataProvider(server);

            BuildWithParametersPage buildWithParametersPage = Pages.openBuildWithParameters(this, project);

            // test prefetch dropdown
            DropdownParameter leader = buildWithParametersPage.getDropdown("leader");
            assertThat("Default value not set", leader.getValue(), equalTo("beethoven@mail.com"));
            leader.click().sendKeys("Wolf");
            assertThat(leader.getDropdownBoxHighlightedItem(), isVisible());
            leader.getDropdownBoxHighlightedItem().click();
            assertThat(leader.getValue(), equalTo("mozart@mail.com"));

            // test async dropdown
            DropdownParameter subLeader = buildWithParametersPage.getDropdown("sub-leader");
            subLeader.click().sendKeys("Fred");
            doWait().until(__ -> subLeader.loadingIconVisible());
            assertThat(subLeader.getLoadingIcon(), isVisible());
            doWait().until(__ -> subLeader.dropdownBoxHighlightedItemVisible());
            assertThat(subLeader.getDropdownBoxHighlightedItem(), isVisible());
            subLeader.sendKeys(Keys.RETURN);
            assertThat(subLeader.getValue(), equalTo("chopin@mail.com"));
        }
    }

    @Test
    public void configuration() throws IOException {
        Project project = setupJobWithAllDataProviders();

        JobConfigurationPage jobConfigurationPage = Pages.openJobConfiguration(this, project);

        Parameter remote = jobConfigurationPage.getParameter("remote");
        DataProvider dataProvider = remote.getDataProvider();
        assertThat(dataProvider.getType(), is(DataProviderType.REMOTE));
        assertThat(dataProvider.getMainValue(), is(dataProvider.getPersistedMainValue()));

        Parameter groovy = jobConfigurationPage.getParameter("groovy");
        dataProvider = groovy.getDataProvider();
        assertThat(dataProvider.getType(), is(DataProviderType.GROOVY));
        assertThat(dataProvider.getMainValue(), is(dataProvider.getPersistedMainValue()));

        Parameter inline = jobConfigurationPage.getParameter("inline");
        dataProvider = inline.getDataProvider();
        assertThat(dataProvider.getType(), is(DataProviderType.INLINE));
        assertThat(dataProvider.getMainValue(), is(dataProvider.getPersistedMainValue()));

        Parameter simple = jobConfigurationPage.getParameter("simple");
        dataProvider = simple.getDataProvider();
        assertThat(dataProvider.getType(), is(DataProviderType.SIMPLE));
        assertThat(dataProvider.getMainValue(), is(dataProvider.getPersistedMainValue()));
    }

    @Test
    public void dataWithNumber() throws IOException {
        String json = "[{'id':1,'value':1}, {'id':2,'value':2}]";
        Project project = setupJobWithInlineJsonDataProvider(json, "value * 100", "id * 2");

        BuildWithParametersPage buildWithParametersPage = Pages.openBuildWithParameters(this, project);

        DropdownParameter template = buildWithParametersPage.getDropdown("inline");
        template.click().sendKeys("10");
        assertThat(template.getDropdownBoxHighlightedItem(), isVisible());
        template.getDropdownBoxHighlightedItem().click();
        assertThat(template.getValue(), equalTo("2"));
    }

    @Test
    public void dataWithNull() throws IOException {
        String json = "[{'id':1,'name':null}, {'id':2,'name':'example'}]";
        Project project = setupJobWithInlineJsonDataProvider(json, "name", "id");

        BuildWithParametersPage buildWithParametersPage = Pages.openBuildWithParameters(this, project);

        DropdownParameter template = buildWithParametersPage.getDropdown("inline");
        template.click().sendKeys("nul");
        assertThat(template.getDropdownBoxHighlightedItem(), isVisible());
        template.getDropdownBoxHighlightedItem().click();
        assertThat(template.getValue(), equalTo("1"));
    }

    @Test
    public void dataWithObject() throws IOException {
        String json = "[{'id':1,'value':{'name':'demo'}}, {'id':2,'value':{'name':'sample'}}]";
        Project project = setupJobWithInlineJsonDataProvider(json, "value.name", "id");

        BuildWithParametersPage buildWithParametersPage = Pages.openBuildWithParameters(this, project);

        DropdownParameter template = buildWithParametersPage.getDropdown("inline");
        template.click().sendKeys("sam");
        assertThat(template.getDropdownBoxHighlightedItem(), isVisible());
        template.getDropdownBoxHighlightedItem().click();
        assertThat(template.getValue(), equalTo("2"));
    }

    @Test
    public void dataWithArray() throws IOException {
        String json = "[{'id':1,'value':['A','B','C',1]}, {'id':2,'value':['X','Y','Z',2]}]";
        Project project = setupJobWithInlineJsonDataProvider(json, "value[0] + value[1] + value[2] + value[3]", "id");

        BuildWithParametersPage buildWithParametersPage = Pages.openBuildWithParameters(this, project);

        DropdownParameter template = buildWithParametersPage.getDropdown("inline");
        template.click().sendKeys("XY");
        assertThat(template.getDropdownBoxHighlightedItem(), isVisible());
        template.getDropdownBoxHighlightedItem().click();
        assertThat(template.getValue(), equalTo("2"));
    }
}
