package org.jenkinsci.plugins.autocompleteparameter;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.jvnet.hudson.test.JenkinsRule;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;

public abstract class AbstractUiIT {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Rule
    public TestName testName = new TestName();

    public static WebDriver webDriver;

    @BeforeClass
    public static void openBrowser() {
        if (System.getProperty("webdriver.gecko.driver") == null)
            System.setProperty("webdriver.gecko.driver", "drivers/linux/marionette/64bit/geckodriver");

        if (!new File("drivers/linux/marionette/64bit/geckodriver").exists())
            throw new IllegalStateException("To run integration tests, you must run 'mvn clean install' at least once to download gecko driver");

        webDriver = new FirefoxDriver();
    }

    @AfterClass
    public static void closeBrowser() {
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

    protected WebDriverWait doWait() {
        return new WebDriverWait(webDriver, 30);
    }
}
