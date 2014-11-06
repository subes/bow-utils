package be.bagofwords.cache;

import be.bagofwords.counts.Counter;
import be.bagofwords.ui.UI;
import be.bagofwords.util.KeyValue;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

public class Cache<T> {

    private static final int NUMBER_OF_SEGMENTS_EXPONENT = 7;
    private static final int NUMBER_OF_SEGMENTS = 1 << NUMBER_OF_SEGMENTS_EXPONENT;
    private static final long SEGMENTS_KEY_MASK = NUMBER_OF_SEGMENTS - 1;
    private static final int NUMBER_OF_READ_PERMITS = 1000;

    private final Semaphore[] locks;
    private final DynamicMap<T>[] cachedObjects;
    private final DynamicMap<T>[] oldCachedObjects;
    private final Class<? extends T> objectClass;
    private final String name;

    private long numHits;
    private long numOfFetches;
    private Map<T, T> commonValues;

    public Cache(String name, Class<? extends T> objectClass) {
        this.objectClass = objectClass;
        this.cachedObjects = new DynamicMap[NUMBER_OF_SEGMENTS];
        createMaps(cachedObjects);
        this.oldCachedObjects = new DynamicMap[NUMBER_OF_SEGMENTS];
        createMaps(oldCachedObjects);
        this.locks = new Semaphore[NUMBER_OF_SEGMENTS];
        for (int i = 0; i < locks.length; i++) {
            this.locks[i] = new Semaphore(NUMBER_OF_READ_PERMITS);
        }
        this.numHits = 0;
        this.numOfFetches = 0;
        this.commonValues = null; //Will be initialized once we have enough values
        this.name = name;
    }

    public KeyValue<T> get(long key) {
        incrementFetches();
        int segmentInd = getSegmentInd(key);
        lockRead(segmentInd);
        KeyValue<T> result = cachedObjects[segmentInd].get(key);
        if (result == null) {
            //maybe in old objects?
            result = oldCachedObjects[segmentInd].get(key);
            unlockRead(segmentInd);
            if (result != null) {
                lockWrite(segmentInd);
                //found in old, put in new
                cachedObjects[segmentInd].put(key, result.getValue());
                oldCachedObjects[segmentInd].remove(key);
                unlockWrite(segmentInd);
            } else {
                return null; //not in cache
            }
        } else {
            unlockRead(segmentInd);
        }
        incrementHits();
        return result;
    }

    public void put(long key, T value) {
        value = makeSharedValueIfPossible(value);
        int segmentInd = getSegmentInd(key);
        lockWrite(segmentInd);
        cachedObjects[segmentInd].put(key, value);
        oldCachedObjects[segmentInd].remove(key);
        unlockWrite(segmentInd);
    }

    public void clear() {
        lockWriteAll();
        createMaps(cachedObjects);
        createMaps(oldCachedObjects);
        unlockWriteAll();
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
        for (DynamicMap<T> map : cachedObjects) {
            result += map.size();
        }
        return result;
    }

    public long completeSize() {
        long result = size();
        for (DynamicMap<T> map : oldCachedObjects) {
            result += map.size();
        }
        return result;
    }

    public void remove(long key) {
        int segmentInd = getSegmentInd(key);
        lockWrite(segmentInd);
        cachedObjects[segmentInd].remove(key);
        oldCachedObjects[segmentInd].remove(key);
        unlockWrite(segmentInd);
    }

    public String getName() {
        return name;
    }

    public long getNumberOfHits() {
        return numHits;
    }

    public long getNumberOfFetches() {
        return numOfFetches;
    }

    private void lockRead(int segmentInd) {
        locks[segmentInd].acquireUninterruptibly(1);
    }

    private void unlockRead(int segmentInd) {
        locks[segmentInd].release(1);
    }

    private void lockWrite(int segmentInd) {
        locks[segmentInd].acquireUninterruptibly(NUMBER_OF_READ_PERMITS);
    }

    private void unlockWrite(int segmentInd) {
        locks[segmentInd].release(NUMBER_OF_READ_PERMITS);
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
        for (int segmentInd = 0; segmentInd < NUMBER_OF_SEGMENTS; segmentInd++) {
            lockRead(segmentInd);
            for (KeyValue<T> entry : cachedObjects[segmentInd].getAllValues()) {
                valueAction.doAction(entry.getKey(), entry.getValue());
            }
            unlockRead(segmentInd);
        }
    }

    private void incrementFetches() {
        this.numOfFetches++;
    }

    private void incrementHits() {
        this.numHits++;
    }

    private void createMaps(DynamicMap[] result) {
        for (int i = 0; i < result.length; i++) {
            result[i] = new DynamicMap<>(objectClass);
        }
    }

    private int getSegmentInd(long key) {
        return (int) (key & SEGMENTS_KEY_MASK);
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
        return value != null && (value instanceof String || value instanceof Byte || value instanceof Character || value instanceof Boolean);
    }

    private Map computeCommonValues() {
        final Counter<Object> counter = new Counter<>();
        doActionOnValues(new ValueAction<T>() {
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

    public Iterator<KeyValue<T>> iterator() {
        return new Iterator<KeyValue<T>>() {

            private Iterator<KeyValue<T>> valuesInCurrSegment = null;
            private int segmentInd = -1;

            {
                //constructor
                findNext();
            }

            private void findNext() {
                while (segmentInd < NUMBER_OF_SEGMENTS - 1 && (valuesInCurrSegment == null || !valuesInCurrSegment.hasNext())) {
                    segmentInd++;
                    lockRead(segmentInd);
                    List<KeyValue<T>> allValues = cachedObjects[segmentInd].getAllValues();
                    valuesInCurrSegment = allValues.iterator();
                    unlockRead(segmentInd);
                }
            }

            @Override
            public boolean hasNext() {
                return valuesInCurrSegment != null && valuesInCurrSegment.hasNext();
            }

            @Override
            public KeyValue<T> next() {
                KeyValue<T> next = valuesInCurrSegment.next();
                if (!valuesInCurrSegment.hasNext()) {
                    findNext();
                }
                return next;
            }

            @Override
            public void remove() {
                throw new RuntimeException("Not supported");
            }
        };
    }

    private interface ValueAction<T> {

        void doAction(long key, T value);

    }
}
