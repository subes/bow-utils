package be.bagofwords.cache;

import be.bagofwords.application.CloseableComponent;
import be.bagofwords.application.annotations.BowComponent;
import be.bagofwords.application.memory.MemoryGobbler;
import be.bagofwords.application.memory.MemoryManager;
import be.bagofwords.application.status.StatusViewable;
import be.bagofwords.ui.UI;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@BowComponent
public class CachesManager implements MemoryGobbler, StatusViewable, CloseableComponent {

    private List<Cache> caches;
    private MemoryManager memoryManager;

    @Autowired
    public CachesManager(MemoryManager memoryManager) {
        this.caches = new ArrayList<>();
        this.memoryManager = memoryManager;
        this.memoryManager.registerMemoryGobbler(this);
    }

    @Override
    public void freeMemory() {
        long oldSizeRead = sizeOfAllReadCaches();
        for (Cache cache : caches) {
            if (!cache.isWriteBuffer()) {
                cache.moveCachedObjectsToOld();
            }
        }
        long newSizeRead = sizeOfAllReadCaches();
        long sizeWrite = sizeOfAllWriteBuffers();
        UI.write("[Memory] Reduced size of caches read=" + oldSizeRead + "-->" + newSizeRead + ", write=" + sizeWrite);
    }

    private long sizeOfAllReadCaches() {
        long result = 0;
        for (Cache cache : caches) {
            if (!cache.isWriteBuffer()) {
                result += cache.completeSize();
            }
        }
        return result;
    }


    private long sizeOfAllWriteBuffers() {
        long result = 0;
        for (Cache cache : caches) {
            if (cache.isWriteBuffer()) {
                result += cache.size();
            }
        }
        return result;
    }

    public synchronized <T> Cache<T> createNewCache(boolean isWriteBuffer, String name, Class<? extends T> objectClass) {
        Cache newCache = new Cache<>(isWriteBuffer, name, objectClass);
        caches.add(newCache);
        return newCache;
    }

    public List<Cache> getAllCaches() {
        return caches;
    }


    @Override
    public void printHtmlStatus(StringBuilder sb) {
        sb.append("<h1>Caches</h1>");
        List<Cache> sortedCaches = new ArrayList<>(caches);
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

    @Override
    public synchronized void close() {
        //do nothing
    }

}

