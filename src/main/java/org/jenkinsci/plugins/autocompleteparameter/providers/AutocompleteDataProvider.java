package org.jenkinsci.plugins.autocompleteparameter.providers;

import java.io.Serializable;
import java.util.Collection;

import org.jenkinsci.plugins.autocompleteparameter.SafeJenkins;

import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Describable;
import hudson.model.Descriptor;

@SuppressWarnings("serial")
public abstract class AutocompleteDataProvider implements Describable<AutocompleteDataProvider>, ExtensionPoint, Serializable {

	public abstract Collection<?> getData();
	
	public static DescriptorExtensionList<AutocompleteDataProvider, Descriptor<AutocompleteDataProvider>> all() {
        return SafeJenkins.getInstanceOrCry().getDescriptorList(AutocompleteDataProvider.class);
    }
	
	@SuppressWarnings("unchecked")
	public Descriptor<AutocompleteDataProvider> getDescriptor() {
		return SafeJenkins.getInstanceOrCry().getDescriptorOrDie(getClass());
	}
}
