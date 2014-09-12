package be.bow.cache;

import be.bow.util.DataLock;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Cache<T> {

    private Map<Long, T> cachedObjects;
    private Map<Long, T> oldCachedObjects;
    private final CacheableData data;
    private final int index;
    private final DataLock writeLock;
    private final String flushLock = new String("FLUSH_LOCK"); //Needs to be new object
    private final boolean isWriteBuffer;

    private int numHits;
    private int numOfFetches;
    private Map<T, T> commonValues;
    private long timeOfLastClean;
    private long timeOfLastFlush;

    public Cache(CacheableData data, int index, boolean isWriteBuffer) {
        this.data = data;
        this.cachedObjects = new ConcurrentHashMap<>();
        this.oldCachedObjects = new ConcurrentHashMap<>();
        this.numHits = 0;
        this.numOfFetches = 0;
        this.commonValues = null; //Will be initialized once we have enough values
        this.index = index;
        this.timeOfLastClean = System.currentTimeMillis();
        this.timeOfLastFlush = System.currentTimeMillis();
        this.writeLock = new DataLock();
        this.isWriteBuffer = isWriteBuffer;
    }

    public DataLock getWriteLock() {
        return writeLock;
    }

    public Iterator<Map.Entry<Long, T>> iterator() {
        return cachedObjects.entrySet().iterator();
    }

    public CacheableData getData() {
        return data;
    }

    public void clear() {
        cachedObjects.clear();
        oldCachedObjects.clear();
        timeOfLastClean = System.currentTimeMillis();
    }

    public Map<Long, T> getCachedObjects() {
        return cachedObjects;
    }

    public Map<Long, T> getOldCachedObjects() {
        return oldCachedObjects;
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
        this.oldCachedObjects = cachedObjects;
        this.cachedObjects = new ConcurrentHashMap<>();
        this.timeOfLastClean = System.currentTimeMillis();
    }

}
