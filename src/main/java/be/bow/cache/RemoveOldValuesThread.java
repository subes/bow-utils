package be.bow.cache;

import be.bow.util.SafeThread;
import be.bow.util.Utils;

public class RemoveOldValuesThread extends SafeThread {

    private CachesManager cachesManager;

    public RemoveOldValuesThread(CachesManager cachesManager) {
        super("RemoveOldCachedValues", false);
        this.cachesManager = cachesManager;
    }

    @Override
    public void runInt() {
        while (!isTerminateRequested()) {
            cachesManager.removeOldValuesFromReadCaches(CacheFlushType.NOT_USED_IN_LONG_TIME);
            Utils.threadSleep(500);
        }
    }
}
