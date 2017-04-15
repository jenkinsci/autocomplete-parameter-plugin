package org.jenkinsci.plugins.autocompleteparameter;

public interface Transformer<V> {
	public String transform(V s);
}
