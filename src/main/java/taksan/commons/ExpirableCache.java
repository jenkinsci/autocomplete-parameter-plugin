package taksan.commons;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

public class ExpirableCache<T> implements Serializable {
	private static final long serialVersionUID = 4974308528003877624L;
	
	private ConcurrentHashMap<String, Long> expirationTime = new ConcurrentHashMap<>();
	private ConcurrentHashMap<String, T> data  = new ConcurrentHashMap<>();;
	private long ttl;
	
	public ExpirableCache(long ttl) {
		this.ttl = ttl;
	}
	
	public T put(final String key, final T value) {
		long expiration = System.currentTimeMillis() + ttl;
		expirationTime.put(key, expiration);
		return data.put(key, value);
	}
	
	public T get(final String key, ExpirableCacheRepopulator<T> fallback) {
		T result = data.get(key);
		
		if (result == null || expirationTime.get(key) < System.currentTimeMillis()) { 
			try {
				result = fallback.call(result);
			}catch(Exception e) {
				throw new RuntimeException(e);
			}
			put(key, result);
		}
		return result;
	}
}
