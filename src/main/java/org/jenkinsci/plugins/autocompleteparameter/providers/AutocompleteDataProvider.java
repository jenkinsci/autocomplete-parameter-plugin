package org.jenkinsci.plugins.autocompleteparameter.providers;

import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.jenkinsci.plugins.autocompleteparameter.SafeJenkins;

import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Describable;
import hudson.model.Descriptor;

@SuppressWarnings("serial")
public abstract class AutocompleteDataProvider implements Describable<AutocompleteDataProvider>, ExtensionPoint, Serializable {

	private static final transient ExecutorService executor = Executors.newCachedThreadPool();

	protected static <T> T executeWithTimeout(Callable<T> callable, long timeout, TimeUnit unit) throws Exception {
		Future<T> task = executor.submit(callable);
		try {
			return task.get(timeout, unit);
		} catch (TimeoutException ex) {
			throw new TimeoutException("Failed to execute within time limit: " + timeout + " " + unit.name());
		} catch (ExecutionException ex) {
			throw (Exception) ex.getCause();
		}
	}

	public abstract Collection<?> getData();

	public Collection<?> getData(long timeout, TimeUnit unit) throws Exception {
		Callable<Collection<?>> callable = new Callable<Collection<?>>() {
			@Override
			public Collection<?> call() throws Exception {
				return getData();
			}
		};
		return executeWithTimeout(callable, timeout, unit);
	}

	public boolean isPrefetch() {
		return true;
	}

	public abstract Collection<?> filter(String query);

	public Collection<?> filter(final String query, long timeout, TimeUnit unit) throws Exception {
		Callable<Collection<?>> callable = new Callable<Collection<?>>() {
			@Override
			public Collection<?> call() throws Exception {
				return filter(query);
			}
		};
		return executeWithTimeout(callable, timeout, unit);
	}

	public static DescriptorExtensionList<AutocompleteDataProvider, Descriptor<AutocompleteDataProvider>> all() {
        return SafeJenkins.getInstanceOrCry().getDescriptorList(AutocompleteDataProvider.class);
    }

	@SuppressWarnings("unchecked")
	public Descriptor<AutocompleteDataProvider> getDescriptor() {
		return SafeJenkins.getInstanceOrCry().getDescriptorOrDie(getClass());
	}
}
