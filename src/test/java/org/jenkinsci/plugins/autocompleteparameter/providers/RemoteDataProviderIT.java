package org.jenkinsci.plugins.autocompleteparameter.providers;

import hudson.slaves.EnvironmentVariablesNodeProperty;
import hudson.util.FormValidation;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class RemoteDataProviderIT {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Before
    public void setupEnvVar() {
        EnvironmentVariablesNodeProperty.Entry entry = new EnvironmentVariablesNodeProperty.Entry("REMOTE_URL", "http://remote");
        EnvironmentVariablesNodeProperty env = new EnvironmentVariablesNodeProperty(Collections.singletonList(entry));
        j.getInstance().getGlobalNodeProperties().add(env);
    }

    @Test
    public void urlValidation_prefetch_parameterSubstitution() {
        RemoteDataProvider.DescriptorImpl subject = new RemoteDataProvider.DescriptorImpl();
        FormValidation response = subject.doCheckAutoCompleteUrl(true, "$REMOTE_URL/rest");
        assertThat(response.kind, equalTo(FormValidation.Kind.OK));
    }

    @Test
    public void urlValidation_async_parameterSubstitution() {
        RemoteDataProvider.DescriptorImpl subject = new RemoteDataProvider.DescriptorImpl();
        FormValidation response = subject.doCheckAutoCompleteUrl(false, "$REMOTE_URL/rest");
        assertThat(response.kind, equalTo(FormValidation.Kind.OK));
        assertThat(response.renderHtml(), containsString("${query}"));
    }

    @Test
    public void urlValidation_async_parameterSubstitution_withQueryParameter() {
        RemoteDataProvider.DescriptorImpl subject = new RemoteDataProvider.DescriptorImpl();
        FormValidation response = subject.doCheckAutoCompleteUrl(false, "$REMOTE_URL/rest?q=${query}");
        assertThat(response.kind, equalTo(FormValidation.Kind.OK));
        assertThat(response.renderHtml(), not(containsString("${query}")));
    }
}
