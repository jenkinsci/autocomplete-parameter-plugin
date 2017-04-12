package org.jenkinsci.plugins.autocompleteparameter.providers;

import java.util.Collection;

import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Describable;
import hudson.model.Descriptor;
import jenkins.model.Jenkins;

public abstract class AutocompleteDataProvider implements Describable<AutocompleteDataProvider>, ExtensionPoint {
	public abstract Collection<?> getData();
	
	public static DescriptorExtensionList<AutocompleteDataProvider, Descriptor<AutocompleteDataProvider>> all() {
        return Jenkins.getInstance().<AutocompleteDataProvider, Descriptor<AutocompleteDataProvider>>getDescriptorList(AutocompleteDataProvider.class);
    }
	
	@SuppressWarnings("unchecked")
	public Descriptor<AutocompleteDataProvider> getDescriptor() {
		return Jenkins.getInstance().getDescriptorOrDie(getClass());
	}
}
