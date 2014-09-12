package be.bow.cache;

import be.bow.application.CloseableComponent;
import be.bow.application.annotations.BowComponent;
import be.bow.application.memory.MemoryManager;
import be.bow.application.memory.MemoryGobbler;
import be.bow.application.status.StatusViewable;
import be.bow.counts.Counter;
import be.bow.ui.UI;
import be.bow.util.KeyValue;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@BowComponent
public class CachesManager implements MemoryGobbler, StatusViewable, CloseableComponent {

    private static final int FLUSH_BATCH_SIZE = 1000000;
    public static final long TIME_BETWEEN_FLUSHES_FOR_WRITE_BUFFER = 1000; //Flush write buffer every second
    public static final long TIME_FOR_VALUES_TO_BE_INVALIDATED = 30 * 60 * 1000; //In read cache, don't keep values cached for longer then 30 minutes

    private Cache[] caches;
    private MemoryManager memoryManager;
    private FlushWriteBufferThread flushWriteBufferThread;
    private RemoveOldValuesThread removeOldValuesThread;

    private final static String NULL_VALUE = "xxxxxNULLxxxx";

    @Autowired
    public CachesManager(MemoryManager memoryManager) {
        caches = new Cache[0];
        this.memoryManager = memoryManager;
        this.memoryManager.registerMemoryGobbler(this);
        this.flushWriteBufferThread = new FlushWriteBufferThread(this, memoryManager);
        this.flushWriteBufferThread.start();
        this.removeOldValuesThread = new RemoveOldValuesThread(this);
        this.removeOldValuesThread.start();
    }

    public <T> T get(int cacheInd, long key) {
        if (cacheInd < 0 || cacheInd >= caches.length) {
            UI.write("Unknown cache " + cacheInd);
        }
        Cache<T> cache = caches[cacheInd];
        cache.incrementFetches();
        T cachedObj = cache.getCachedObjects().get(key);
        if (cachedObj != null) {
            cache.incrementHits();
        } else {
            //maybe in old objects?
            cachedObj = cache.getOldCachedObjects().remove(key);
            if (cachedObj != null) {
                //put in new
                cache.getCachedObjects().put(key, cachedObj);
                cache.incrementHits();
            }
        }
        if (cachedObj == NULL_VALUE) {
            cachedObj = null;
        }
        return cachedObj;
    }

    public <T> void put(int cacheInd, long key, T value) {
        Cache<T> cache = caches[cacheInd];
        if (value == null) {
            ((Cache) cache).getCachedObjects().put(key, NULL_VALUE);
        } else {
            value = makeSharedValueIfPossible(value, cache);
            cache.getCachedObjects().put(key, value);
        }
    }

    private <T> T makeSharedValueIfPossible(T value, Cache<T> cache) {
        Map<T, T> commonValues = cache.getCommonValues();
        if (valueCanBeCommon(value)) {
            if (commonValues == null) {
                //Can we compute the common values?
                if (cache.getCommonValues() == null && cache.getCachedObjects().size() > 10000) {
                    cache.setCommonValues(computeCommonValues(cache));
                }
            } else {
                //Fetch common value
                T commonValue = commonValues.get(value);
                if (commonValue != null) {
                    return commonValue;
                }
            }
        }
        return value;
    }

    private <T> boolean valueCanBeCommon(T value) {
        return value instanceof String || value instanceof Long || value instanceof Byte || value instanceof Character || value instanceof Boolean || value instanceof Double || value instanceof Float;
    }

    public void remove(int cacheInd, long key) {
        Cache<?> cache = caches[cacheInd];
        if (!cache.getCachedObjects().isEmpty()) {
            cache.getCachedObjects().remove(key);
        }
        if (!cache.getOldCachedObjects().isEmpty()) {
            cache.getOldCachedObjects().remove(key);
        }
    }

    public void flush(int cacheInd) {
        Cache cache = caches[cacheInd];
        flush(cache);
    }

    /**
     * Not that this method is synchronized on
     * - the flush lock of the cache to make sure that flushes happen in order
     * - the write lock of the cache to make sure that no other methods write to this cache while emptying it. The write lock is released
     * before the writing of the data to the underlying datainterface, since this is usually the slow part of the flush.
     */

    void flush(Cache cache) {
        synchronized (cache.getFlushLock()) {
            cache.getWriteLock().lockWriteAll();
            cache.setTimeOfLastFlush(System.currentTimeMillis());
            List<KeyValue> valuesToRemove = new ArrayList<>();
            Iterator<Map.Entry<Long, Object>> iterator = cache.iterator();
            while (iterator.hasNext()) {
                Map.Entry<Long, Object> next = iterator.next();
                Object value = next.getValue();
                if (value == NULL_VALUE) {
                    value = null;
                }
                valuesToRemove.add(new KeyValue(next.getKey(), value));
                iterator.remove(); //Free some memory
                if (valuesToRemove.size() > FLUSH_BATCH_SIZE) {
                    cache.getData().removedValues(cache.getIndex(), valuesToRemove);
                    valuesToRemove.clear();
                }
            }
            cache.clear(); //also clear old cached objects
            cache.getWriteLock().unlockWriteAll();
            if (!valuesToRemove.isEmpty()) {
                cache.getData().removedValues(cache.getIndex(), valuesToRemove);
            }

        }
    }

    public void clear(int cacheInd) {
        caches[cacheInd].clear();
    }

    @Override
    public void freeMemory() {
        long oldSizeRead = sizeOfAllReadCaches();
        removeOldValuesFromReadCaches(CacheFlushType.FORCE);
        long newSizeRead = sizeOfAllReadCaches();
        long oldSizeWrite = sizeOfAllWriteBuffers();
        UI.write("[Memory] Reduced size of caches read=" + oldSizeRead + "-->" + newSizeRead + ", write=" + oldSizeWrite);
    }

    public void removeOldValuesFromReadCaches(CacheFlushType cacheFlushType) {
        for (Cache cache : caches) {
            if (!cache.isWriteBuffer()) {
                boolean removeData = cacheFlushType == CacheFlushType.FORCE;
                long timeSinceLastClean = System.currentTimeMillis() - cache.getTimeOfLastClean();
                removeData = removeData || cacheFlushType == CacheFlushType.NOT_USED_IN_LONG_TIME && timeSinceLastClean >= CachesManager.TIME_FOR_VALUES_TO_BE_INVALIDATED;
                if (removeData) {
                    cache.getWriteLock().lockWriteAll();
                    cache.moveCachedObjectsToOld();
                    cache.getWriteLock().unlockWriteAll();
                }
            }
        }
    }

    public void flushWriteBuffers(long timeDiff) {
        for (Cache cache : caches) {
            if (cache.isWriteBuffer() && !cache.getCachedObjects().isEmpty() && System.currentTimeMillis() - cache.getTimeOfLastFlush() >= timeDiff) {
                try {
                    flush(cache);
                } catch (Exception exp) {
                    throw new RuntimeException("Received exception while flushing cache " + cache.getData().getName(), exp);
                }
            }
        }
    }

    private Map computeCommonValues(Cache cache) {
        Counter<Object> counter = new Counter<>();
        int counts = 0;
        Map<Object, Object> cachedObjects = cache.getCachedObjects();
        for (Object obj : cachedObjects.values()) {
            if (valueCanBeCommon(obj)) {
                counter.inc(obj);
                counts++;
                if (counts >= 10000) {
                    break;
                }
            }
        }
        List<Object> sorted = counter.sortedKeys();
        Map<Object, Object> result = new HashMap<>();
        for (int i = 0; i < sorted.size() && i < 1000; i++) {
            result.put(sorted.get(i), sorted.get(i));
        }
        return result;
    }

    private long sizeOfAllReadCaches() {
        long result = 0;
        for (Cache cache : caches) {
            if (!cache.isWriteBuffer()) {
                result += cache.getCachedObjects().size() + cache.getOldCachedObjects().size();
            }
        }
        return result;
    }


    private long sizeOfAllWriteBuffers() {
        long result = 0;
        for (Cache cache : caches) {
            if (cache.isWriteBuffer()) {
                result += cache.getCachedObjects().size() + cache.getOldCachedObjects().size();
            }
        }
        return result;
    }

    public synchronized <T> int createNewCache(CacheableData<T> cacheableData, boolean isWriteBuffer) {
        int ind = caches.length;
        Cache[] newCaches = Arrays.copyOf(caches, caches.length + 1);
        Cache newCache = new Cache<T>(cacheableData, ind, isWriteBuffer);
        newCaches[ind] = newCache;
        caches = newCaches;
        return ind;
    }

    public Cache[] getAllCaches() {
        return caches;
    }

    public Map getCache(int cacheInd) {
        return caches[cacheInd].getCachedObjects();
    }

    public void flushAll() {
        for (Cache cache : caches) {
            flush(cache);
        }
    }

    @Override
    public void printHtmlStatus(StringBuilder sb) {
        sb.append("<h1>Caches</h1>");
        List<Cache> caches = new ArrayList<>(Arrays.asList(getAllCaches()));
        Collections.sort(caches, new Comparator<Cache>() {
            @Override
            public int compare(Cache o1, Cache o2) {
                return -Double.compare(o1.getCachedObjects().size(), o2.getCachedObjects().size());
            }
        });
        for (Cache cache : caches) {
            double hitRatio = cache.getNumHits() == 0 ? 0 : cache.getNumHits() / (double) cache.getNumOfFetches();
            sb.append(cache.getData().getName() + " (" + cache.getIndex() + ") size=" + cache.getCachedObjects().size() + " fetches=" + cache.getNumOfFetches() + " hits=" + cache.getNumHits() + " hitRatio=" + hitRatio);
            sb.append("<br>");
        }
    }

    public void lockWrite(int cacheInd, long key) {
        caches[cacheInd].getWriteLock().lockWrite(key);
    }

    public void unlockWrite(int cacheInd, long key) {
        caches[cacheInd].getWriteLock().unlockWrite(key);
    }

    @Override
    public synchronized void close() {
        flushWriteBufferThread.close();
        removeOldValuesThread.close();
        flushWriteBufferThread.waitForFinish(60 * 1000);
        removeOldValuesThread.waitForFinish(60 * 1000);
        flushAll();
    }

    public void lockWriteAll(int cacheInd) {
        caches[cacheInd].getWriteLock().lockWriteAll();
    }

    public void unlockWriteAll(int cacheInd) {
        caches[cacheInd].getWriteLock().unlockWriteAll();
    }

}

