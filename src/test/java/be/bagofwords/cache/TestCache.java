package be.bagofwords.cache;

import be.bagofwords.application.memory.MemoryManager;
import be.bagofwords.util.HashUtils;
import be.bagofwords.util.KeyValue;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
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
        CacheableData<Long> data = new CacheableData<Long>() {

            @Override
            public void removedValuesFromCache(Cache cache, List<KeyValue<Long>> valuesToRemove) {
                //OK
            }

        };
        Cache<Long> firstCache = cachesManager.createNewCache(data, false, "test", Long.class);
        Random random = new Random();
        for (int i = 0; i < numOfValues; i++) {
            int cacheInd = random.nextInt();
            long key = HashUtils.hashCode(cacheInd + "_" + Long.toString(random.nextLong()));
            firstCache.put(key, key);
        }
        Assert.assertTrue(firstCache.size() > 0);
        //Eventually all values should be removed
        CacheableData<Long> otherData = new CacheableData<Long>() {
            @Override
            public void removedValuesFromCache(Cache cache, List<KeyValue<Long>> valuesToRemove) {
                //ok
            }

        };
        Cache otherCache = cachesManager.createNewCache(otherData, false, "test", Long.class);
        long maxTimeToTry = 5 * 60 * 1000; //usually this should take less then 5 minutes
        long start = System.currentTimeMillis();
        while (start + maxTimeToTry >= System.currentTimeMillis() && firstCache.size() > 0) {
            long key = HashUtils.hashCode(Long.toString(random.nextLong()));
            otherCache.put(key, key);
        }
        Assert.assertEquals(0, firstCache.size());
        freeMemoryManager.close();
    }

    @Test
    public void testUseCommonValues() {
        final int numOfDifferentValues = 20;
        final MutableBoolean dataWasRemoved = new MutableBoolean(false);
        MemoryManager freeMemoryManager = new MemoryManager();
        CachesManager cachesManager = new CachesManager(freeMemoryManager);
        final Random random = new Random();
        CacheableData<String> data = new CacheableData<String>() {
            @Override
            public void removedValuesFromCache(Cache cache, List<KeyValue<String>> valuesToRemove) {
                dataWasRemoved.setValue(valuesToRemove.size() > numOfDifferentValues * 10);
            }

        };
        Cache<String> cache = cachesManager.createNewCache(data, false, "test", String.class);
        long maxTimeToTry = 30000; //30s
        long start = System.currentTimeMillis();
        while (start + maxTimeToTry >= System.currentTimeMillis() && !dataWasRemoved.booleanValue()) {
            long key = random.nextInt();
            String value = Integer.toString(random.nextInt(numOfDifferentValues));
            cache.put(key, value);
        }
        cache.flush();
        List<String> values = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            long key = i;
            String value = Integer.toString(i * numOfDifferentValues / 100);
            cache.put(key, value);
            String cachedValue = cache.get(key);
            Assert.assertEquals(value, cachedValue);
            if (!values.contains(cachedValue)) {
                values.add(cachedValue);
            } else {
                boolean identicalObj = false;
                for (String existingValue : values) {
                    if (existingValue == cachedValue) {
                        identicalObj = true;
                    }
                }
                Assert.assertTrue(identicalObj);
            }
        }
        freeMemoryManager.close();
    }

}
