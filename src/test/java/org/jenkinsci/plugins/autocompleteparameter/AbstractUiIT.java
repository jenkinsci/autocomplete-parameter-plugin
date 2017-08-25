package org.jenkinsci.plugins.autocompleteparameter;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.jvnet.hudson.test.JenkinsRule;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.io.File;

public abstract class AbstractUiIT {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    public WebDriver webDriver;

    @Before
    public void openBrowser() {
        if (System.getProperty("webdriver.gecko.driver") == null)
            System.setProperty("webdriver.gecko.driver", "drivers/linux/marionette/64bit/geckodriver");


        if (!new File("drivers/linux/marionette/64bit/geckodriver").exists())
            throw new IllegalStateException("To run integration tests, you must run 'mvn clean install' at least once to download gecko driver");

        webDriver = new FirefoxDriver();
    }

    @After
    public void closeBrowser() {
        if (webDriver == null)
            return;

        try {
            webDriver.close();
        } catch (Exception e) {
//            e.printStackTrace();
        }
        try {
            webDriver.quit();
        } catch (Exception e) {
//            e.printStackTrace();
        }
    }
}
