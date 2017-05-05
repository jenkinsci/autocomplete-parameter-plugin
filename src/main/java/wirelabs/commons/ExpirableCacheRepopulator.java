package wirelabs.commons;

/**
 * This interface to implement cache repopulation
 * 
 * @author takeuchi
 *
 * @param <T> The value that will be returned. Should match ExpirableCache stored value type
 */
public interface ExpirableCacheRepopulator<T> {
	public T call(T expiredValue);
}
