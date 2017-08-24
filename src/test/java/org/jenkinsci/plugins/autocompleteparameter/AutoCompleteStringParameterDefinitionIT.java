package org.jenkinsci.plugins.autocompleteparameter;

import hudson.model.FreeStyleProject;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.Project;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.jenkinsci.plugins.autocompleteparameter.pages.BuildWithParametersPage;
import org.jenkinsci.plugins.autocompleteparameter.pages.BuildWithParametersPage.AutoCompleteParameter;
import org.jenkinsci.plugins.autocompleteparameter.pages.Pages;
import org.jenkinsci.plugins.autocompleteparameter.providers.GroovyDataProvider;
import org.jenkinsci.plugins.autocompleteparameter.providers.InlineJsonDataProvider;
import org.jenkinsci.plugins.autocompleteparameter.providers.RemoteDataProvider;
import org.jenkinsci.plugins.autocompleteparameter.providers.SimpleTextProvider;
import org.junit.Test;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.xml.sax.SAXException;

import java.io.IOException;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class AutoCompleteStringParameterDefinitionIT extends AbstractUiIT {

    public Project setupJobWithRemoteDataProvider() throws IOException {
        FreeStyleProject project = j.createFreeStyleProject("remote");
        AutoCompleteStringParameterDefinition prefetchedParameter = new AutoCompleteStringParameterDefinition("user", "", "", "name", "email", false
                , new RemoteDataProvider(true, "http://localhost:29971/rest/users", "credentials"));
        AutoCompleteStringParameterDefinition asyncParameter = new AutoCompleteStringParameterDefinition("other", "", "", "name", "email", false
                , new RemoteDataProvider(false, "http://localhost:29971/rest/users?slow=true", "credentials"));
        project.addProperty(new ParametersDefinitionProperty(
                prefetchedParameter, asyncParameter
        ));
        return project;
    }

    public Project setupJobWithGroovyDataProvider() throws IOException {
        FreeStyleProject project = j.createFreeStyleProject("groovy");
        AutoCompleteStringParameterDefinition groovyParameter = new AutoCompleteStringParameterDefinition("fibonacci", "", "", "value", "key", false
                , new GroovyDataProvider("return ['1':'One', '2':'Two', '3':'Three', '5':'Five', '8':'Eight', '13':'Thirteen'].entrySet()"
                , true, null));
        project.addProperty(new ParametersDefinitionProperty(
                groovyParameter
        ));
        return project;
    }

    public Project setupJobWithInlineJsonDataProvider() throws IOException {
        FreeStyleProject project = j.createFreeStyleProject("inline");
        AutoCompleteStringParameterDefinition inlineParameter = new AutoCompleteStringParameterDefinition("characters", "", "", "name", "id", false
                , new InlineJsonDataProvider("["
                + "{'name':'Eddard Stark','id':'estark', 'house':'Stark'}"
                + ",{'name':'Jon Snow','id':'jsnow', 'house':'Stark'}"
                + ",{'name':'Tyrion Lannister','id':'tlannister', 'house':'Lannister'}"
                + ",{'name':'Robert Baratheon','id':'rbaratheon', 'house':'Baratheon'}"
                + "]"));

        project.addProperty(new ParametersDefinitionProperty(
                inlineParameter
        ));
        return project;
    }

    public Project setupJobWithSimpleTextProvider() throws IOException {
        FreeStyleProject project = j.createFreeStyleProject("simple");
        AutoCompleteStringParameterDefinition simpleParameter = new AutoCompleteStringParameterDefinition("hackers", "", "", "value.split('|')[1]", "value.split('|')[0]", false
                , new SimpleTextProvider(
                        "al|Ada Lovelace\n"
                        + "sj|Steve Jobs\n"
                        + "bg|Bill Gates\n"
                        + "lt|Linus Torvalds\n"
                ));
        project.addProperty(new ParametersDefinitionProperty(
                simpleParameter
        ));
        return project;
    }

    public RemoteServerMock remoteServer() throws IOException {
        return new RemoteServerMock();
    }

    @Test
    public void remoteDataProvider() throws IOException, SAXException, InterruptedException {
        Project project = setupJobWithRemoteDataProvider();
        try(RemoteServerMock server = remoteServer()) {
            server.start();

            BuildWithParametersPage buildWithParametersPage = Pages.openBuildWithParameters(this, project);
            String html = webDriver.getPageSource();
            assertThat(html, containsString("Beethoven"));
            assertThat(html, containsString("Chopin"));
            assertThat(html, containsString("Mozart"));

            // test prefetch autocomplete
            AutoCompleteParameter user = buildWithParametersPage.getParameter("user");
            user.sendKeys("Wolfg");
            assertThat(user.getSuggestionsBox(), isVisible());
            user.sendKeys(Keys.DOWN).sendKeys(Keys.RETURN);
            assertThat(user.getValue(), equalTo("mozart@mail.com"));

            // test async autocomplete
            AutoCompleteParameter other = buildWithParametersPage.getParameter("other");
            other.sendKeys("Fred");
            doWait().until(other.loadingIconVisible());
            assertThat(other.getLoadingIcon(), isVisible());
            doWait().until(other.suggestionsBoxVisible());
            assertThat(other.getSuggestionsBox(), isVisible());
            other.sendKeys(Keys.DOWN).sendKeys(Keys.RETURN);
            assertThat(other.getValue(), equalTo("chopin@mail.com"));
        }
    }

    @Test
    public void groovyDataProvider() throws IOException {
        Project project = setupJobWithGroovyDataProvider();

        BuildWithParametersPage buildWithParametersPage = Pages.openBuildWithParameters(this, project);
        String html = webDriver.getPageSource();
        assertThat(html, containsString("Five"));
        assertThat(html, containsString("Eight"));
        assertThat(html, containsString("Thirteen"));

        AutoCompleteParameter fibonacci = buildWithParametersPage.getParameter("fibonacci");
        fibonacci.sendKeys("On").sendKeys(Keys.DOWN).sendKeys(Keys.RETURN);
        fibonacci.sendKeys("Tw").sendKeys(Keys.DOWN).sendKeys(Keys.RETURN);
        fibonacci.sendKeys("Th").sendKeys(Keys.DOWN).sendKeys(Keys.RETURN);
        fibonacci.sendKeys("Th").sendKeys(Keys.DOWN).sendKeys(Keys.RETURN);

        assertThat(fibonacci.getValue(), equalTo("1, 2, 3, 13"));
    }

    @Test
    public void inlineJsonDataProvider() throws IOException {
        Project project = setupJobWithInlineJsonDataProvider();

        BuildWithParametersPage buildWithParametersPage = Pages.openBuildWithParameters(this, project);
        String html = webDriver.getPageSource();
        assertThat(html, containsString("Stark"));
        assertThat(html, containsString("Snow"));
        assertThat(html, containsString("Lannister"));
        assertThat(html, containsString("Baratheon"));

        AutoCompleteParameter characters = buildWithParametersPage.getParameter("characters");
        characters.sendKeys("rob").sendKeys(Keys.DOWN).sendKeys(Keys.RETURN);
        characters.sendKeys("edd").sendKeys(Keys.DOWN).sendKeys(Keys.RETURN);
        characters.sendKeys("sno").sendKeys(Keys.DOWN).sendKeys(Keys.RETURN);
        characters.sendKeys("tyr").sendKeys(Keys.DOWN).sendKeys(Keys.RETURN);

        assertThat(characters.getValue(), equalTo("rbaratheon, estark, jsnow, tlannister"));
        characters.getToken("Robert Baratheon").click().sendKeys(Keys.DELETE);
        characters.getToken("Eddard Stark").click().sendKeys(Keys.BACK_SPACE);
        assertThat(characters.getValue(), equalTo("jsnow, tlannister"));
    }

    @Test
    public void simpleDataProvider() throws IOException {
        Project project = setupJobWithSimpleTextProvider();

        BuildWithParametersPage buildWithParametersPage = Pages.openBuildWithParameters(this, project);
        String html = webDriver.getPageSource();
        assertThat(html, containsString("Lovelace"));
        assertThat(html, containsString("Jobs"));
        assertThat(html, containsString("Gates"));
        assertThat(html, containsString("Torvalds"));

        AutoCompleteParameter hackers = buildWithParametersPage.getParameter("hackers");
        hackers.sendKeys("jobs").sendKeys(Keys.DOWN).sendKeys(Keys.RETURN);
        hackers.sendKeys("bill").sendKeys(Keys.DOWN).sendKeys(Keys.RETURN);
        hackers.sendKeys("ada").sendKeys(Keys.DOWN).sendKeys(Keys.RETURN);
        hackers.sendKeys("linus").sendKeys(Keys.DOWN).sendKeys(Keys.RETURN);

        assertThat(hackers.getValue(), equalTo("sj, bg, al, lt"));
    }

    private Matcher<? super WebElement> isVisible() {
        return new BaseMatcher<WebElement>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("WebElement is not visible");
            }

            @Override
            public boolean matches(Object item) {
                WebElement element = (WebElement) item;
                return element.isDisplayed();
            }
        };
    }

    private WebDriverWait doWait() {
        return new WebDriverWait(webDriver, 30);
    }
}
