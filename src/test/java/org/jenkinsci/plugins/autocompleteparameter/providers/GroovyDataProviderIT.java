package org.jenkinsci.plugins.autocompleteparameter.providers;

import org.jenkinsci.plugins.scriptsecurity.scripts.ClasspathEntry;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class GroovyDataProviderIT {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void simpleScript() {
        // given
        String script = "return ['1':'One', '2':'Two', '3':'Three', '5':'Five', '8':'Eight', '13':'Thirteen'].entrySet()";
        boolean sandbox = true;
        LinkedList<ClasspathEntry> classpath = null;
        GroovyDataProvider subject = new GroovyDataProvider(script, sandbox, classpath);

        // when
        @SuppressWarnings("unchecked")
        Collection<Map.Entry<String, String>> data = (Collection<Map.Entry<String, String>>) subject.getData();
        Iterator<Map.Entry<String, String>> it = data.iterator();

        // then
        Map.Entry<String, String> actual = it.next();
        assertEquals("1", actual.getKey());
        assertEquals("One", actual.getValue());
        actual = it.next();
        assertEquals("2", actual.getKey());
        assertEquals("Two", actual.getValue());
        actual = it.next();
        assertEquals("3", actual.getKey());
        assertEquals("Three", actual.getValue());
        actual = it.next();
        assertEquals("5", actual.getKey());
        assertEquals("Five", actual.getValue());
        actual = it.next();
        assertEquals("8", actual.getKey());
        assertEquals("Eight", actual.getValue());
        actual = it.next();
        assertEquals("13", actual.getKey());
        assertEquals("Thirteen", actual.getValue());
    }

    @Test
    public void timeout() {
        // given
        String script = "Thread.sleep(10000)"
                + "\nreturn ['1':'One', '2':'Two', '3':'Three', '5':'Five', '8':'Eight', '13':'Thirteen'].entrySet()";
        boolean sandbox = false;
        LinkedList<ClasspathEntry> classpath = null;
        GroovyDataProvider subject = new GroovyDataProvider(script, sandbox, classpath);

        try {
            // when
            subject.getData(1, TimeUnit.SECONDS);
            fail();
        } catch (Exception ex) {
            // then
            assertThat(ex, instanceOf(TimeoutException.class));
        }
    }
}
