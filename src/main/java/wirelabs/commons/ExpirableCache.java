package wirelabs.commons;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A cache for expirable entries. The cache is basically a map of entries indexed by strings.
 * 
 * @author takeuchi
 *
 * @param <T> the type of stored values
 */
public class ExpirableCache<T> implements Serializable {
	private static final long serialVersionUID = 4974308528003877624L;
	
	private ConcurrentHashMap<String, Long> expirationTime = new ConcurrentHashMap<>();
	private ConcurrentHashMap<String, T> data  = new ConcurrentHashMap<>();;
	private long ttl;
	
	/**
	 * 
	 * @param ttl The default "Time to live" (ttl) for the entries 
	 */
	public ExpirableCache(long ttl) {
		this.ttl = ttl;
	}
	
	/**
	 * Puts a value in the cache. The value will expire after the default ttl
	 * 
	 * @param key   the entry key
	 * @param value the entry value
	 * @return the stored entry
	 */
	public T put(final String key, final T value) {
		return this.put(key, value, ttl);
	}
	
	/**
	 * Puts a value in the cache.
	 * 
	 * @param key   the entry key
	 * @param value the entry value
	 * @param ttl   a custom time to live for this entry
	 * @return the stored entry
	 */
	public T put(final String key, final T value, long ttl) {
		long expiration = System.currentTimeMillis() + ttl;
		expirationTime.put(key, expiration);
		return data.put(key, value);
	}
	
	/**
	 * Retrieves a value from the cache. Uses the default ttl
	 * 
	 * @param key the key to retrieve
	 * @param populate the function to re-populate if the value is expired or non existent
	 * @return
	 */
	public T get(final String key, ExpirableCacheRepopulator<T> populate) {
		return get(key, ttl, populate);
	}
	
	/**
	 * Retrieves a value from the cache
	 * 
	 * @param key       the key to retrieve
	 * @param ttl       the time to live in case the value is repopulated
	 * @param populate  the function to re-populate if the value is expired or non existent
	 * @return the value for the key or the value produced by the populate function
	 */
	public T get(final String key, long ttl, ExpirableCacheRepopulator<T> populate) {
		T result = data.get(key);
		
		if (result == null || expirationTime.get(key) < System.currentTimeMillis()) { 
			try {
				result = populate.call(result);
			}catch(Exception e) {
				throw new RuntimeException(e);
			}
			put(key, result, ttl);
		}
		return result;
	}
}
