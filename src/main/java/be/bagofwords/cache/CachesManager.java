package be.bagofwords.cache;

import be.bagofwords.application.annotations.BowComponent;
import be.bagofwords.application.memory.MemoryGobbler;
import be.bagofwords.application.memory.MemoryManager;
import be.bagofwords.application.status.StatusViewable;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@BowComponent
public class CachesManager implements MemoryGobbler, StatusViewable {

    private List<WeakReference<ReadCache>> caches;

    @Autowired
    public CachesManager(MemoryManager memoryManager) {
        this.caches = new ArrayList<>();
        memoryManager.registerMemoryGobbler(this);
    }

    @Override
    public synchronized void freeMemory() {
        for (WeakReference<ReadCache> reference : caches) {
            ReadCache readCache = reference.get();
            if (readCache != null) {
                readCache.moveCachedObjectsToOld();
            }
        }
    }

    @Override
    public String getMemoryUsage() {
        return "caches size=" + sizeOfAllReadCaches();
    }

    private synchronized long sizeOfAllReadCaches() {
        long result = 0;
        for (WeakReference<ReadCache> reference : caches) {
            ReadCache readCache = reference.get();
            if (readCache != null) {
                result += readCache.completeSize();
            }
        }
        return result;
    }


    public synchronized <T> ReadCache<T> createNewCache(String name, Class<? extends T> objectClass) {
        ReadCache<T> newReadCache = new ReadCache<>(name, objectClass);
        caches.add(new WeakReference<ReadCache>(newReadCache));
        return newReadCache;
    }

    @Override
    public synchronized void printHtmlStatus(StringBuilder sb) {
        sb.append("<h1>Caches</h1>");
        List<ReadCache> sortedCaches = new ArrayList<>();
        for (WeakReference<ReadCache> reference : caches) {
            ReadCache readCache = reference.get();
            if (readCache != null) {
                sortedCaches.add(readCache);
            }
        }
        Collections.sort(sortedCaches, new Comparator<ReadCache>() {
            @Override
            public int compare(ReadCache o1, ReadCache o2) {
                return -Double.compare(o1.size(), o2.size());
            }
        });
        for (ReadCache readCache : sortedCaches) {
            double hitRatio = readCache.getNumberOfHits() == 0 ? 0 : readCache.getNumberOfHits() / (double) readCache.getNumberOfFetches();
            sb.append(readCache.getName() + " size=" + readCache.size() + " fetches=" + readCache.getNumberOfFetches() + " hits=" + readCache.getNumberOfHits() + " hitRatio=" + hitRatio);
            sb.append("<br>");
        }
    }


}

