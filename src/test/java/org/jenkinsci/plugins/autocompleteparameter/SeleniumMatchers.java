package org.jenkinsci.plugins.autocompleteparameter;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.openqa.selenium.WebElement;

public class SeleniumMatchers {

    public static Matcher<? super WebElement> isVisible() {
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
}
