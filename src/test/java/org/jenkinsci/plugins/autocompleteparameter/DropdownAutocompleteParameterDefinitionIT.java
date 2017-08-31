package org.jenkinsci.plugins.autocompleteparameter;

import hudson.model.FreeStyleProject;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.Project;
import org.jenkinsci.plugins.autocompleteparameter.pages.BuildWithParametersPage;
import org.jenkinsci.plugins.autocompleteparameter.pages.BuildWithParametersPage.DropdownParameter;
import org.jenkinsci.plugins.autocompleteparameter.pages.Pages;
import org.jenkinsci.plugins.autocompleteparameter.providers.RemoteDataProvider;
import org.junit.Test;
import org.openqa.selenium.Keys;

import java.io.IOException;

import static org.hamcrest.Matchers.equalTo;
import static org.jenkinsci.plugins.autocompleteparameter.SeleniumMatchers.isVisible;
import static org.junit.Assert.assertThat;

public class DropdownAutocompleteParameterDefinitionIT extends AbstractUiIT {

    public Project setupJobWithRemoteDataProvider(RemoteServerMock server) throws IOException {
        String endpoint = server.getAddress() + "/rest/users";
        String slowEndpoint = server.getAddress() + "/rest/users?slow=true";
        FreeStyleProject project = j.createFreeStyleProject("remote");
        DropdownAutocompleteParameterDefinition prefetchedParameter = new DropdownAutocompleteParameterDefinition("leader", "", "name", "email", "beethoven@mail.com"
                , new RemoteDataProvider(true, endpoint, "credentials"));
        DropdownAutocompleteParameterDefinition asyncParameter = new DropdownAutocompleteParameterDefinition("sub-leader", "", "name", "email", ""
                , new RemoteDataProvider(false, slowEndpoint, "credentials"));
        project.addProperty(new ParametersDefinitionProperty(
                prefetchedParameter, asyncParameter
        ));
        return project;
    }

    public RemoteServerMock remoteServer() throws IOException {
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
            doWait().until(subLeader.loadingIconVisible());
            assertThat(subLeader.getLoadingIcon(), isVisible());
            doWait().until(subLeader.dropdownBoxHighlightedItemVisible());
            assertThat(subLeader.getDropdownBoxHighlightedItem(), isVisible());
            subLeader.sendKeys(Keys.RETURN);
            assertThat(subLeader.getValue(), equalTo("chopin@mail.com"));
        }
    }
}
