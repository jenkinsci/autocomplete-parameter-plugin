package org.jenkinsci.plugins.autocompleteparameter.pages;

import hudson.model.Project;
import org.jenkinsci.plugins.autocompleteparameter.AbstractUiIT;
import org.jvnet.hudson.test.JenkinsRule;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;

import java.io.IOException;

public final class Pages {

	public static BuildWithParametersPage openBuildWithParameters(AbstractUiIT test, Project<?, ?> project) throws IOException {
		return openBuildWithParameters(AbstractUiIT.webDriver, test.j, project);
	}
	
    public static BuildWithParametersPage openBuildWithParameters(WebDriver webDriver, JenkinsRule j, Project<?, ?> project) throws IOException {
        webDriver.get(j.getURL().toString() + "job/" + project.getName() + "/build");

        BuildWithParametersPage page = new BuildWithParametersPage(webDriver);
        PageFactory.initElements(webDriver, page);
        return page;
    }

    public static JobConfigurationPage openJobConfiguration(AbstractUiIT test, Project<?, ?> project) throws IOException {
        return openJobConfiguration(AbstractUiIT.webDriver, test.j, project);
    }

    public static JobConfigurationPage openJobConfiguration(WebDriver webDriver, JenkinsRule j, Project<?, ?> project) throws IOException {
        webDriver.get(j.getURL().toString() + "job/" + project.getName() + "/configure");

        JobConfigurationPage page = new JobConfigurationPage(webDriver, project);
        PageFactory.initElements(webDriver, page);
        return page;
    }

}
