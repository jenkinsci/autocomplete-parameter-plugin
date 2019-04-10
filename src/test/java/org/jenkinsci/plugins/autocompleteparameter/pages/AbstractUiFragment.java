package org.jenkinsci.plugins.autocompleteparameter.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public abstract class AbstractUiFragment {

    protected WebDriver webDriver;

    protected AbstractUiFragment(WebDriver driver) {
        this.webDriver = driver;
    }

    private WebDriverWait doWait() {
        return new WebDriverWait(webDriver, 30);
    }

    private void waitUntilElementExists(final By by) {
        doWait().until(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver element) {
                return element.findElements(by).size() > 0;
            }
        });
    }

    private void waitUntilElementVisible(final By by) {
        doWait().until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    protected WebElement getElementWhenItExists(By by) {
        waitUntilElementExists(by);
        return webDriver.findElement(by);
    }

    protected WebElement getElementWhenVisible(By by) {
        waitUntilElementVisible(by);
        return webDriver.findElement(by);
    }
}
