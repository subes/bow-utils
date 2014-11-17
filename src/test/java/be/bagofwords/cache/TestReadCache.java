package be.bagofwords.cache;

import be.bagofwords.application.BowTaskScheduler;
import be.bagofwords.application.memory.MemoryManager;
import org.junit.Assert;
import org.junit.Test;

import java.util.Random;

/**
 * Created by Koen Deschacht (koendeschacht@gmail.com) on 9/25/14.
 */
public class TestReadCache {

    @Test
    public void testRemoval() throws Exception {
        final int numOfValues = 1000;
        Random random = new Random();
        char[] randomString = createRandomString(random);
        MemoryManager freeMemoryManager = new MemoryManager();
        BowTaskScheduler taskScheduler = new BowTaskScheduler();
        CachesManager cachesManager = new CachesManager(freeMemoryManager, taskScheduler);
        ReadCache<String> firstReadCache = cachesManager.createNewCache("test", String.class);
        for (int i = 0; i < numOfValues; i++) {
            firstReadCache.put(random.nextLong(), new String(randomString));
        }
        Assert.assertTrue(firstReadCache.size() > 0);
        //Eventually all values should be removed
        ReadCache<String> otherReadCache = cachesManager.createNewCache("test", String.class);
        long maxTimeToTry = 5 * 60 * 1000; //usually this should take less then 5 minutes
        long start = System.currentTimeMillis();
        long timeOfLastPrint = System.currentTimeMillis();
        while (start + maxTimeToTry >= System.currentTimeMillis() && firstReadCache.size() > 0) {
            otherReadCache.put(random.nextLong(), new String(randomString));
            if (System.currentTimeMillis() - timeOfLastPrint > 1000) {
                timeOfLastPrint = System.currentTimeMillis();
            }
        }
        Assert.assertEquals(0, firstReadCache.size());
        cachesManager.createNewCache("unused_cache", Long.class); //just to make sure that the caches manager is not garbage collected before the end of the test
        freeMemoryManager.terminate();
        taskScheduler.terminate();
    }

    private char[] createRandomString(Random random) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append(random.nextLong());
        }
        return sb.toString().toCharArray();
    }

}
