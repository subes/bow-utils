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

    private List<WeakReference<Cache>> caches;
    private MemoryManager memoryManager;

    @Autowired
    public CachesManager(MemoryManager memoryManager) {
        this.caches = new ArrayList<>();
        this.memoryManager = memoryManager;
        this.memoryManager.registerMemoryGobbler(this);
    }

    @Override
    public void freeMemory() {
        for (WeakReference<Cache> reference : caches) {
            Cache cache = reference.get();
            if (cache != null) {
                cache.moveCachedObjectsToOld();
            }
        }
    }

    @Override
    public String getMemoryUsage() {
        return "caches size=" + sizeOfAllReadCaches();
    }

    private long sizeOfAllReadCaches() {
        long result = 0;
        for (WeakReference<Cache> reference : caches) {
            Cache cache = reference.get();
            if (cache != null) {
                result += cache.completeSize();
            }
        }
        return result;
    }


    public synchronized <T> Cache<T> createNewCache(String name, Class<? extends T> objectClass) {
        Cache<T> newCache = new Cache<>(name, objectClass);
        caches.add(new WeakReference<Cache>(newCache));
        return newCache;
    }

    @Override
    public void printHtmlStatus(StringBuilder sb) {
        sb.append("<h1>Caches</h1>");
        List<Cache> sortedCaches = new ArrayList<>();
        for (WeakReference<Cache> reference : caches) {
            Cache cache = reference.get();
            if (cache != null) {
                sortedCaches.add(cache);
            }
        }
        Collections.sort(sortedCaches, new Comparator<Cache>() {
            @Override
            public int compare(Cache o1, Cache o2) {
                return -Double.compare(o1.size(), o2.size());
            }
        });
        for (Cache cache : sortedCaches) {
            double hitRatio = cache.getNumberOfHits() == 0 ? 0 : cache.getNumberOfHits() / (double) cache.getNumberOfFetches();
            sb.append(cache.getName() + " size=" + cache.size() + " fetches=" + cache.getNumberOfFetches() + " hits=" + cache.getNumberOfHits() + " hitRatio=" + hitRatio);
            sb.append("<br>");
        }
    }


}

