package be.bow.cache;

import be.bow.counts.Counter;
import be.bow.util.KeyValue;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

public class Cache<T> {

    private static final int FLUSH_BATCH_SIZE = 1000000;
    private static final int NUMBER_OF_SEGMENTS = 64;
    private static final int NUMBER_OF_READ_PERMITS = 1000;

    private final CacheableData data;
    private final Semaphore[] locks;
    private final Map<Long, T>[] cachedObjects;
    private final Map<Long, T>[] oldCachedObjects;
    private final boolean isWriteBuffer;
    private final T nullValue;
    private final String name;

    private int numHits;
    private int numOfFetches;
    private Map<T, T> commonValues;

    public Cache(CacheableData<T> data, boolean isWriteBuffer, String name) {
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
        this.isWriteBuffer = isWriteBuffer;
        this.nullValue = getNullValueForType(data.getObjectClass());
        this.name = name;
    }

    public T get(long key) {
        incrementFetches();
        int cacheInd = getCacheInd(key);
        lockRead(cacheInd);
        T result = cachedObjects[cacheInd].get(key);
        if (result == null) {
            //maybe in old objects?
            result = oldCachedObjects[cacheInd].remove(key);
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
        incrementHits();
        if (result.equals(nullValue)) {
            return null;
        } else {
            return result;
        }
    }

    public void put(long key, T value) {
        if (value == null) {
            value = nullValue;
        } else {
            value = makeSharedValueIfPossible(value);
        }
        int cacheInd = getCacheInd(key);
        lockWrite(cacheInd);
        cachedObjects[cacheInd].put(key, value);
        oldCachedObjects[cacheInd].remove(key);
        unlockWrite(cacheInd);
    }

    public void flush() {
        final List<KeyValue> valuesToRemove = new ArrayList<>();
        doActionOnValues(new ValueAction() {
            @Override
            public void doAction(long key, Object value) {
                valuesToRemove.add(new KeyValue(key, value));
                if (valuesToRemove.size() > FLUSH_BATCH_SIZE) {
                    getData().removedValues(Cache.this, valuesToRemove);
                    valuesToRemove.clear();
                }
            }
        });
        clear(); //also clear old cached objects
        if (!valuesToRemove.isEmpty()) {
            getData().removedValues(this, valuesToRemove);
        }
    }

    public CacheableData getData() {
        return data;
    }

    public void clear() {
        createMaps(cachedObjects);
        createMaps(oldCachedObjects);
    }

    public void moveCachedObjectsToOld() {
        lockWriteAll();
        for (int i = 0; i < cachedObjects.length; i++) {
            oldCachedObjects[i] = cachedObjects[i];
        }
        createMaps(cachedObjects);
        unlockWriteAll();
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

    public String getName() {
        return name;
    }

    public int getNumberOfHits() {
        return numHits;
    }

    public int getNumberOfFetches() {
        return numOfFetches;
    }

    public boolean isWriteBuffer() {
        return isWriteBuffer;
    }

    private static <T> T getNullValueForType(Class<T> objectClass) {
        if (objectClass == Long.class) {
            return (T) new Long(Long.MAX_VALUE);
        } else if (objectClass == Double.class) {
            return (T) new Double(Double.MAX_VALUE);
        } else {
            return (T) "xxxNULLxxx";
        }
    }

    private void lockRead(int cacheInd) {
        locks[cacheInd].acquireUninterruptibly(1);
    }

    private void unlockRead(int cacheInd) {
        locks[cacheInd].release(1);
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

    private void doActionOnValues(ValueAction<T> valueAction) {
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

    private void incrementFetches() {
        this.numOfFetches++;
    }

    private void incrementHits() {
        this.numHits++;
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

    private T makeSharedValueIfPossible(T value) {
        if (valueCanBeCommon(value)) {
            if (commonValues == null) {
                //Can we compute the common values?
                if (size() > 10000) {
                    commonValues = computeCommonValues();
                }
            }
            if (commonValues != null) {
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
        return value != null && (value instanceof String || value instanceof Byte || value instanceof Character || value instanceof Boolean || value instanceof Float);
    }

    private Map computeCommonValues() {
        final Counter<Object> counter = new Counter<>();
        doActionOnValues(new Cache.ValueAction() {
            @Override
            public void doAction(long key, Object value) {
                if (counter.size() < 10000 && valueCanBeCommon(value)) {
                    counter.inc(value);
                }
            }
        });
        List<Object> sorted = counter.sortedKeys();
        Map<Object, Object> result = new HashMap<>();
        for (int i = 0; i < sorted.size() && i < 1000; i++) {
            result.put(sorted.get(i), sorted.get(i));
        }
        return result;
    }

    private interface ValueAction<T> {

        void doAction(long key, T value);

    }
}
