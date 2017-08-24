package org.jenkinsci.plugins.autocompleteparameter.pages;

import hudson.model.Project;
import org.jenkinsci.plugins.autocompleteparameter.AbstractUiIT;
import org.jvnet.hudson.test.JenkinsRule;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;

import java.io.IOException;

public final class Pages {

    public static BuildWithParametersPage openBuildWithParameters(AbstractUiIT test, Project project) throws IOException {
        WebDriver webDriver = test.webDriver;
        JenkinsRule j = test.j;

        webDriver.get(j.getURL().toString() + "job/" + project.getName() + "/build");

        BuildWithParametersPage page = new BuildWithParametersPage(webDriver);
        PageFactory.initElements(webDriver, page);
        return page;
    }

}
