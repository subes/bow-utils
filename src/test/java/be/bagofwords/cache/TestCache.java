package be.bagofwords.cache;

import be.bagofwords.application.memory.MemoryManager;
import be.bagofwords.util.HashUtils;
import be.bagofwords.util.Utils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Random;

/**
 * Created by Koen Deschacht (koendeschacht@gmail.com) on 9/25/14.
 */
public class TestCache {

    @Test
    public void testRemoval() throws Exception {
        final int numOfValues = 1000000;
        MemoryManager freeMemoryManager = new MemoryManager();
        CachesManager cachesManager = new CachesManager(freeMemoryManager);
        Cache<Long> firstCache = cachesManager.createNewCache("test", Long.class);
        Random random = new Random();
        for (int i = 0; i < numOfValues; i++) {
            int cacheInd = random.nextInt();
            long key = HashUtils.hashCode(cacheInd + "_" + Long.toString(random.nextLong()));
            firstCache.put(key, key);
        }
        Assert.assertTrue(firstCache.size() > 0);
        //Eventually all values should be removed
        Cache otherCache = cachesManager.createNewCache("test", Long.class);
        long maxTimeToTry = 5 * 60 * 1000; //usually this should take less then 5 minutes
        long start = System.currentTimeMillis();
        while (start + maxTimeToTry >= System.currentTimeMillis() && firstCache.size() > 0) {
            long key = HashUtils.hashCode(Long.toString(random.nextLong()));
            otherCache.put(key, key);
        }
        Assert.assertEquals(0, firstCache.size());
        cachesManager.createNewCache("unused_cache", Long.class); //just to make sure that the caches manager is not garbage collected before the end of the test
        freeMemoryManager.terminate();
    }

}
