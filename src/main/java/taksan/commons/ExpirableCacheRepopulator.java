package taksan.commons;

public interface ExpirableCacheRepopulator<T> {
	public T call(T expiredValue);
}
