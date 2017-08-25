package org.jenkinsci.plugins.autocompleteparameter.pages;

import org.openqa.selenium.WebDriver;

public abstract class AbstractPage {

    protected WebDriver webDriver;

    protected AbstractPage(WebDriver webDriver) {
        this.webDriver = webDriver;
    }

}
