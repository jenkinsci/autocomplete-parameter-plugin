package org.jenkinsci.plugins.autocompleteparameter.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

public class BuildWithParametersPage extends AbstractPage {

    @FindBy(name = "parameters")
    private WebElement form;

    protected BuildWithParametersPage(WebDriver webDriver) {
        super(webDriver);
    }

    public AutoCompleteParameter getAutoComplete(String name) {
        WebElement parentDiv = form.findElement(By.xpath("//div[input[@id='autocomplete-" + name + "']]"));
        return new AutoCompleteParameter(parentDiv);
    }

    public DropdownParameter getDropdown(String name) {
        WebElement parentDiv = form.findElement(By.xpath("//div[input[@value='" + name + "']]"));
        return new DropdownParameter(parentDiv);
    }

    public class AutoCompleteParameter {

        private final WebElement parentDiv;
        private final WebElement input;
        private final WebElement value;

        public AutoCompleteParameter(WebElement parentDiv) {
            this.parentDiv = parentDiv;
            this.input = parentDiv.findElement(By.cssSelector(".tt-input"));
            this.value = parentDiv.findElement(By.cssSelector("input[name=value]"));
        }

        public AutoCompleteParameter sendKeys(String keys) {
            input.sendKeys(keys);
            return this;
        }

        public AutoCompleteParameter sendKeys(Keys code) {
            input.sendKeys(code);
            return this;
        }

        public WebElement getSuggestionsBox() {
            return parentDiv.findElement(suggestionsBoxSelector());
        }

        public WebElement getLoadingIcon() {
            return parentDiv.findElement(loadingIconSelector());
        }

        private By suggestionsBoxSelector() {
            return By.cssSelector(".tt-suggestions");
        }

        public By loadingIconSelector() {
            return By.cssSelector("img");
        }

        public ExpectedCondition<List<WebElement>> suggestionsBoxVisible() {
            return ExpectedConditions.visibilityOfNestedElementsLocatedBy(parentDiv, suggestionsBoxSelector());
        }

        public ExpectedCondition<List<WebElement>> loadingIconVisible() {
            return ExpectedConditions.visibilityOfNestedElementsLocatedBy(parentDiv, loadingIconSelector());
        }

        public String getValue() {
            return value.getAttribute("value");
        }

        public Token getToken(String token) {
            WebElement element = parentDiv.findElement(By.xpath("//div[contains(@class, 'token')][span[text()='" + token + "']]"));
            return new Token(element);
        }
    }

    public class ElementWrapper<T extends ElementWrapper<T>> {

        private final T self;

        public ElementWrapper() {
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

    public class Token extends ElementWrapper<Token> {

        private final WebElement element;

        public Token(WebElement element) {
            this.element = element;
        }

        public Token click() {
            element.click();
            return this;
        }
    }

    public class DropdownParameter extends ElementWrapper<DropdownParameter> {

        private final WebElement parentDiv;
        private final WebElement select;
        private final WebElement value;

        public DropdownParameter(WebElement parentDiv) {
            this.parentDiv = parentDiv;
            this.select = parentDiv.findElement(By.cssSelector(".select2-container"));
            this.value = parentDiv.findElement(By.cssSelector("select"));
        }

        public DropdownParameter click() {
            select.click();
            return this;
        }

        public WebElement getDropdownBoxHighlightedItem() {
            return webDriver.findElement(dropdownBoxHighlightedItemSelector());
        }

        public WebElement getLoadingIcon() {
            return parentDiv.findElement(loadingIconSelector());
        }

        private By dropdownBoxHighlightedItemSelector() {
            return By.cssSelector(".select2-results__option--highlighted");
        }

        private By loadingIconSelector() {
            return By.cssSelector("img");
        }

        public ExpectedCondition<WebElement> dropdownBoxHighlightedItemVisible() {
            return ExpectedConditions.visibilityOfElementLocated(dropdownBoxHighlightedItemSelector());
        }

        public ExpectedCondition<List<WebElement>> loadingIconVisible() {
            return ExpectedConditions.visibilityOfNestedElementsLocatedBy(parentDiv, loadingIconSelector());
        }

        public String getValue() {
            return value.getAttribute("value");
        }
    }
}
