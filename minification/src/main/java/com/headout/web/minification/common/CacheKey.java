package com.headout.web.minification.common;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This class is used for caching minified JS code. For each key there exists a unique Cachekey instance.Cache content
 * are modified when lastModified attribute of file is changed. This cache is threadsafe. Every CacheKey instance holds
 * its own lock object. When there are multiple threads trying to update the content of the same Cachekey instance, Lock
 * instance of that CacheKey instance is used for making that operation threadsafe.
 *
 * @author harshal
 *
 */
public class CacheKey {

	private static final ConcurrentHashMap<String, CacheKey> keyInstanceMap = new ConcurrentHashMap<String, CacheKey>();

	/**
	 * This function generates unique key .
	 *
	 * @param absolutePath absolutepath of file
	 * @param tagId JspId
	 * @param isInline if isLine is true that means it's inline JS code wiithin JSP file, else it's external referenced
	 *            JS/CSS file
	 * @return returns unique key
	 */

	public static String generateKey(String absolutePath, String tagId, Boolean isInline) {
		if (isInline) {
			return absolutePath + tagId;
		}
		return absolutePath;
	}

	/**
	 * @param key key for which CacheKey is required
	 * @return CacheKey instance corresponding to the given input key
	 */
	public static CacheKey getInstance(String key) {
		CacheKey keyInstance = keyInstanceMap.get(key);
		if (keyInstance == null) {
			keyInstance = new CacheKey(key);
			keyInstanceMap.putIfAbsent(key, keyInstance);
			keyInstance = keyInstanceMap.get(key);
		}
		return keyInstance;
	}

	private long lastModified;
	private final String key;
	private volatile ReentrantReadWriteLock lock;
	private String minifiedCode;

	public CacheKey(String key) {
		this.key = key;
		this.lock = new ReentrantReadWriteLock();
		this.lastModified = -1;
	}

	protected String getKey() {
		return key;
	}

	protected long getLastModified() {
		return lastModified;
	}

	public ReentrantReadWriteLock getLock() {
		return lock;
	}

	protected String getMinifiedCode() {
		return minifiedCode;
	}

	private void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}

	private void setMinifiedCode(String minifiedCode) {
		this.minifiedCode = minifiedCode;
	}

	/**
	 * Cache content is updated each time when corresponding file gets modified. This function Can be executed by
	 * multiple threads and only that minified body is returned which is present in cache
	 *
	 * @param lastModified time of file is checked, to verify that cache is holding latest minified content of that file
	 * @param afterMinified minified bodycontent
	 * @return returns The final minified bodycontent present in the cache.
	 */
	public String updateMinifiedBodyIfApplicable(long lastModified, String afterMinified) {
		ReentrantReadWriteLock.WriteLock wlock = getLock().writeLock();
		// Before writing to cache, get write lock on resource where we are going to update in cache.
		// Update only if file status shows modified after getting write lock.
		try {
			wlock.lock();
			// check before inserting if other thread has not modified content of cache
			if (lastModified > getLastModified()) {
				setMinifiedCode(afterMinified);
				setLastModified(lastModified);
			} else {
				afterMinified = getMinifiedCode();
			}
		} finally {
			wlock.unlock();
		}
		return afterMinified;
	}

}
