package be.bow.cache;

import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.Map;
import java.util.concurrent.Semaphore;

public class Cache<T> {

    private static final int NUMBER_OF_SEGMENTS = 64;
    private static final int NUMBER_OF_READ_PERMITS = 1000;

    private final CacheableData data;
    private final int index;
    private final Semaphore[] locks;
    private final Map<Long, T>[] cachedObjects;
    private final Map<Long, T>[] oldCachedObjects;
    private final String flushLock = new String("FLUSH_LOCK"); //Needs to be new object
    private final boolean isWriteBuffer;
    private final T nullValue;

    private int numHits;
    private int numOfFetches;
    private Map<T, T> commonValues;
    private long timeOfLastClean;
    private long timeOfLastFlush;

    public Cache(CacheableData data, int index, boolean isWriteBuffer) {
        this.data = data;
        this.cachedObjects = new Map[NUMBER_OF_SEGMENTS];
        createMaps(cachedObjects);
        this.oldCachedObjects = new Map[NUMBER_OF_SEGMENTS];
        createMaps(oldCachedObjects);
        this.locks = new Semaphore[NUMBER_OF_SEGMENTS];
        for (int i = 0; i < locks.length; i++) {
            this.locks[i] = new Semaphore(NUMBER_OF_READ_PERMITS);
        }
        this.numHits = 0;
        this.numOfFetches = 0;
        this.commonValues = null; //Will be initialized once we have enough values
        this.index = index;
        this.timeOfLastClean = System.currentTimeMillis();
        this.timeOfLastFlush = System.currentTimeMillis();
        this.isWriteBuffer = isWriteBuffer;
        this.nullValue = getNullValueForType(data.getObjectClass());
    }

    private T getNullValueForType(Class objectClass) {
        if (objectClass == Long.class) {
            return (T) new Long(Long.MAX_VALUE);
        } else if (objectClass == Double.class) {
            return (T) new Double(Double.MAX_VALUE);
        } else {
            return (T) "xxxNULLxxx";
        }
    }

    public T get(long key) {
        int cacheInd = getCacheInd(key);
        lockRead(cacheInd);
        T result = (T) cachedObjects[cacheInd].get(key);
        if (result == null) {
            //maybe in old objects?
            result = (T) oldCachedObjects[cacheInd].remove(key);
            unlockRead(cacheInd);
            if (result != null) {
                lockWrite(cacheInd);
                //found in old, put in new
                cachedObjects[cacheInd].put(key, result);
                unlockWrite(cacheInd);
            } else {
                return null;
            }
        } else {
            unlockRead(cacheInd);
        }
        if (result.equals(nullValue)) {
            return null;
        } else {
            return result;
        }
    }

    public void put(long key, T value) {
        if (value == null) {
            value = nullValue;
        }
        int cacheInd = getCacheInd(key);
        lockWrite(cacheInd);
        cachedObjects[cacheInd].put(key, value);
        oldCachedObjects[cacheInd].remove(key);
        unlockWrite(cacheInd);
    }

    private void lockRead(int cacheInd) {
        locks[cacheInd].acquireUninterruptibly(1);
    }

    private void unlockRead(int cacheInd) {
        locks[cacheInd].release(1);
    }

    private void lockReadAll() {
        for (int i = 0; i < NUMBER_OF_SEGMENTS; i++) {
            locks[i].acquireUninterruptibly(1);
        }
    }

    private void unlockReadAll() {
        for (int i = 0; i < NUMBER_OF_SEGMENTS; i++) {
            locks[i].release(1);
        }
    }

    private void lockWrite(int cacheInd) {
        locks[cacheInd].acquireUninterruptibly(NUMBER_OF_READ_PERMITS);
    }

    private void unlockWrite(int cacheInd) {
        locks[cacheInd].release(NUMBER_OF_READ_PERMITS);
    }

    private void lockWriteAll() {
        for (int i = 0; i < NUMBER_OF_SEGMENTS; i++) {
            locks[i].acquireUninterruptibly(NUMBER_OF_READ_PERMITS);
        }
    }

    private void unlockWriteAll() {
        for (int i = 0; i < NUMBER_OF_SEGMENTS; i++) {
            locks[i].release(NUMBER_OF_READ_PERMITS);
        }
    }

    public void doActionOnValues(ValueAction<T> valueAction) {
        for (int cacheInd = 0; cacheInd < NUMBER_OF_SEGMENTS; cacheInd++) {
            lockRead(cacheInd);
            for (Map.Entry<Long, T> entry : cachedObjects[cacheInd].entrySet()) {
                T value = entry.getValue();
                if (value.equals(nullValue)) {
                    value = null;
                }
                valueAction.doAction(entry.getKey(), value);
            }
            unlockRead(cacheInd);
        }
    }

    public CacheableData getData() {
        return data;
    }

    public void clear() {
        createMaps(cachedObjects);
        createMaps(oldCachedObjects);
        timeOfLastClean = System.currentTimeMillis();
    }

    public Map<T, T> getCommonValues() {
        return commonValues;
    }

    public void setCommonValues(Map commonValues) {
        this.commonValues = commonValues;
    }

    public void incrementFetches() {
        this.numOfFetches++;
    }

    public void incrementHits() {
        this.numHits++;
    }

    public int getNumHits() {
        return numHits;
    }

    public int getNumOfFetches() {
        return numOfFetches;
    }

    public int getIndex() {
        return index;
    }

    public long getTimeOfLastClean() {
        return timeOfLastClean;
    }

    public long getTimeOfLastFlush() {
        return timeOfLastFlush;
    }

    public void setTimeOfLastFlush(long timeOfLastFlush) {
        this.timeOfLastFlush = timeOfLastFlush;
    }

    public String getFlushLock() {
        return flushLock;
    }

    public boolean isWriteBuffer() {
        return isWriteBuffer;
    }

    public void moveCachedObjectsToOld() {
        lockWriteAll();
        for (int i = 0; i < cachedObjects.length; i++) {
            oldCachedObjects[i] = cachedObjects[i];
        }
        createMaps(cachedObjects);
        unlockWriteAll();
        this.timeOfLastClean = System.currentTimeMillis();
    }

    private void createMaps(Map[] result) {
        for (int i = 0; i < result.length; i++) {
            if (data.getObjectClass() == Long.class) {
                result[i] = new Long2LongOpenHashMap();
            } else if (data.getObjectClass() == Double.class) {
                result[i] = new Long2DoubleOpenHashMap();
            } else {
                result[i] = new Long2ObjectOpenHashMap();
            }
        }
    }

    private int getCacheInd(long key) {
        return (int) ((key >> 59) + 32); //divide by 32 and add 32 (because of negative keys)
    }

    public long size() {
        long result = 0;
        for (Map map : cachedObjects) {
            result += map.size();
        }
        return result;
    }

    public long completeSize() {
        long result = size();
        for (Map map : oldCachedObjects) {
            result += map.size();
        }
        return result;
    }

    public void remove(long key) {
        int cacheInd = getCacheInd(key);
        cachedObjects[cacheInd].remove(key);
        oldCachedObjects[cacheInd].remove(key);
    }

    public static interface ValueAction<T> {

        void doAction(long key, T value);

    }
}
