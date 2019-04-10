package org.jenkinsci.plugins.autocompleteparameter.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.pagefactory.ByChained;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class BuildWithParametersPage extends AbstractPage {

    private final By formSelector;

    protected BuildWithParametersPage(WebDriver webDriver) {
        super(webDriver);
        formSelector = By.cssSelector("form[name='parameters']");
    }

    public AutoCompleteParameter getAutoComplete(String name) {
        return new AutoCompleteParameter(webDriver, formSelector, name);
    }

    public DropdownParameter getDropdown(String name) {
        return new DropdownParameter(webDriver, formSelector, name);
    }

    public static class AutoCompleteParameter extends ElementWrapper<AutoCompleteParameter> {

        private final By parentDivSelector;
        private final By inputSelector;
        private final By valueSelector;
        private final By suggestionsBoxSelector;
        private final By loadingIconSelector;

        public AutoCompleteParameter(WebDriver webDriver, By formSelector, String name) {
            super(webDriver);
            parentDivSelector = new ByChained(formSelector, By.xpath("//div[input[@id='autocomplete-" + name + "']]"));
            inputSelector = new ByChained(parentDivSelector, By.cssSelector(".tt-input"));
            valueSelector = new ByChained(parentDivSelector, By.cssSelector("input[name=value]"));
            suggestionsBoxSelector = new ByChained(parentDivSelector, By.cssSelector(".tt-suggestions"));
            loadingIconSelector = new ByChained(parentDivSelector, By.cssSelector("img"));
        }

        @Override
        public AutoCompleteParameter sendKeys(String keys) {
            getInputElement().sendKeys(keys);
            return this;
        }

        public AutoCompleteParameter waitSuggestionBoxVisible() {
            getElementWhenVisible(suggestionsBoxSelector);
            return this;
        }

        public WebElement getSuggestionsBox() {
            return getElementWhenItExists(suggestionsBoxSelector);
        }

        public WebElement getLoadingIcon() {
            return getElementWhenItExists(loadingIconSelector);
        }

        public ExpectedCondition<WebElement> suggestionsBoxVisible() {
            return ExpectedConditions.visibilityOfElementLocated(suggestionsBoxSelector);
        }

        public ExpectedCondition<WebElement> loadingIconVisible() {
            return ExpectedConditions.visibilityOfElementLocated(loadingIconSelector);
        }

        public String getValue() {
            return getValueElement().getAttribute("value");
        }

        public Token getToken(String token) {
            return new Token(webDriver, parentDivSelector, token);
        }

        private WebElement getInputElement() {
            return getElementWhenItExists(inputSelector);
        }

        private WebElement getValueElement() {
            return getElementWhenItExists(valueSelector);
        }
    }

    public static class ElementWrapper<T extends ElementWrapper<T>> extends AbstractUiFragment {

        private final T self;

        public ElementWrapper(WebDriver webDriver) {
            super(webDriver);
            @SuppressWarnings("unchecked")
            T self = (T)this;
            this.self = self;
        }

        public T sendKeys(String keys) {
            WebElement focused = webDriver.switchTo().activeElement();
            focused.sendKeys(keys);
            return self;
        }

        public T sendKeys(Keys code) {
            WebElement focused = webDriver.switchTo().activeElement();
            focused.sendKeys(code);
            return self;
        }
    }

    public static class Token extends ElementWrapper<Token> {

        private final By tokenSelector;

        public Token(WebDriver webDriver, By parentDivSelector, String token) {
            super(webDriver);
            tokenSelector = new ByChained(parentDivSelector, By.xpath("//div[contains(@class, 'token')][span[text()='" + token + "']]"));
        }

        public Token click() {
            getTokenElement().click();
            return this;
        }

        private WebElement getTokenElement() {
            return getElementWhenItExists(tokenSelector);
        }
    }

    public static class DropdownParameter extends ElementWrapper<DropdownParameter> {

        private final By parentDivSelector;
        private final By selectSelector;
        private final By valueSelector;
        private final By dropdownBoxHighlightedItemSelector;
        private final By loadingIconSelector;

        public DropdownParameter(WebDriver webDriver, By formSelector, String name) {
            super(webDriver);
            parentDivSelector = new ByChained(formSelector, By.xpath("//div[input[@value='" + name + "']]"));
            selectSelector = new ByChained(parentDivSelector, By.cssSelector(".select2-container"));
            valueSelector = new ByChained(parentDivSelector, By.cssSelector("select"));
            dropdownBoxHighlightedItemSelector = By.cssSelector(".select2-results__option--highlighted");
            loadingIconSelector = new ByChained(parentDivSelector, By.cssSelector("img"));
        }

        public DropdownParameter click() {
            getSelectElement().click();
            return this;
        }

        public WebElement getDropdownBoxHighlightedItem() {
            return getElementWhenItExists(dropdownBoxHighlightedItemSelector);
        }

        public WebElement getLoadingIcon() {
            return getElementWhenItExists(loadingIconSelector);
        }

        public ExpectedCondition<WebElement> dropdownBoxHighlightedItemVisible() {
            return ExpectedConditions.visibilityOfElementLocated(dropdownBoxHighlightedItemSelector);
        }

        public ExpectedCondition<WebElement> loadingIconVisible() {
            return ExpectedConditions.visibilityOfElementLocated(loadingIconSelector);
        }

        public String getValue() {
            return getValueElement().getAttribute("value");
        }

        private WebElement getSelectElement() {
            return getElementWhenItExists(selectSelector);
        }

        private WebElement getValueElement() {
            return getElementWhenItExists(valueSelector);
        }
    }
}
