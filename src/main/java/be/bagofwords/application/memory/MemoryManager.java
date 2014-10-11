package be.bagofwords.application.memory;

import be.bagofwords.application.CloseableComponent;
import be.bagofwords.application.annotations.EagerBowComponent;
import be.bagofwords.ui.UI;
import be.bagofwords.util.SafeThread;
import be.bagofwords.util.Utils;
import com.sun.management.GarbageCollectionNotificationInfo;

import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeData;
import java.io.File;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

@EagerBowComponent
public class MemoryManager implements CloseableComponent {

    private final List<MemoryGobbler> memoryGobblers;
    private MemoryStatus memoryStatus;
    private ReentrantLock globalCleanInProgressLock;
    private final FreeMemoryThread freeMemoryThread;
    private boolean dumpHeapToFileWhenMemoryFull;

    public MemoryManager() {
        memoryGobblers = new ArrayList<>();
        globalCleanInProgressLock = new ReentrantLock();
        memoryStatus = MemoryStatus.FREE;
        freeMemoryThread = new FreeMemoryThread();
        freeMemoryThread.start();
    }

    public boolean getDumpHeapToFileWhenMemoryFull() {
        return dumpHeapToFileWhenMemoryFull;
    }

    public void setDumpHeapToFileWhenMemoryFull(boolean dumpHeapToFileWhenMemoryFull) {
        this.dumpHeapToFileWhenMemoryFull = dumpHeapToFileWhenMemoryFull;
    }

    @Override
    public void terminate() {
        freeMemoryThread.terminateAndWaitForFinish();
    }

    /**
     * This method should be called by methods that consume a lot of memory in order to free up other
     * memory gobblers and prevent an Out-Of-Memory error.
     */

    public void waitForSufficientMemory() {
        if (memoryStatus == MemoryStatus.CRITICAL && !(globalCleanInProgressLock.isLocked() && globalCleanInProgressLock.isHeldByCurrentThread())) {
            //Don't write until enough memory is free
            long start = System.currentTimeMillis();
            long timeOfLastWarning = System.currentTimeMillis();
            while (memoryStatus == MemoryStatus.CRITICAL) {
                Utils.threadSleep(20);
                if (System.currentTimeMillis() - timeOfLastWarning > 30000) {
                    UI.writeWarning("Method has been waiting for more memory for " + (System.currentTimeMillis() - start) + " ms");
                    timeOfLastWarning = System.currentTimeMillis();
                }
            }
        }
    }

    public void registerMemoryGobbler(MemoryGobbler memoryGobbler) {
        synchronized (memoryGobblers) {
            this.memoryGobblers.add(memoryGobbler);
        }
    }

    public MemoryStatus getMemoryStatus() {
        return memoryStatus;
    }

    private class FreeMemoryThread extends SafeThread {

        private FreeMemoryThread() {
            super("FreeMemoryThread", true);
        }

        public void runInt() {
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

            registerGarbageCollectionListener();

            while (!isTerminateRequested()) {
                try {
                    if (memoryStatus == MemoryStatus.LOW || memoryStatus == MemoryStatus.CRITICAL) {
                        globalCleanInProgressLock.lock();
                        //Low on memory, clean caches
                        if (dumpHeapToFileWhenMemoryFull) {
                            File dumpFile = new File("heap_" + System.currentTimeMillis() + ".bin");
                            HeapDumper.dumpHeap(dumpFile.getAbsolutePath(), false);
                            UI.write("Heap dumped to " + dumpFile.getAbsolutePath());
                        }
                        List<MemoryGobbler> currCollections;
                        synchronized (memoryGobblers) {
                            currCollections = new ArrayList<>(memoryGobblers);
                        }
                        if (memoryStatus == MemoryStatus.CRITICAL) {
                            UI.write("[Memory] Memory critical! Printing usage:");
                            for (MemoryGobbler memoryGobbler : memoryGobblers) {
                                UI.write("[Memory] " + memoryGobbler.getClass().getSimpleName() + " " + memoryGobbler.getMemoryUsage());
                            }
                        }
                        for (MemoryGobbler collection : currCollections) {
                            collection.freeMemory();
                        }
                        memoryStatus = MemoryStatus.FREE;
                        System.gc();
                        globalCleanInProgressLock.unlock();
                    }
                } catch (Throwable exp) {
                    UI.writeError("Exception in CleanObjectsThread!!!", exp);
                }
                Utils.threadSleep(50);
            }
        }

        //copied from http://www.fasterj.com/articles/gcnotifs.shtml

        private void registerGarbageCollectionListener() {
            List<GarbageCollectorMXBean> gcbeans = java.lang.management.ManagementFactory.getGarbageCollectorMXBeans();
            for (GarbageCollectorMXBean gcbean : gcbeans) {
                NotificationEmitter emitter = (NotificationEmitter) gcbean;

                NotificationListener listener = new NotificationListener() {
                    //keep a count of the total time spent in GCs
                    long totalGcDuration = 0;

                    //implement the notifier callback handler
                    @Override
                    public void handleNotification(Notification notification, Object handback) {
                        if (notification.getType().equals(GarbageCollectionNotificationInfo.GARBAGE_COLLECTION_NOTIFICATION)) {
                            //get the information associated with this notification
                            GarbageCollectionNotificationInfo info = GarbageCollectionNotificationInfo.from((CompositeData) notification.getUserData());
                            Map<String, MemoryUsage> mem = info.getGcInfo().getMemoryUsageAfterGc();
                            for (Map.Entry<String, MemoryUsage> entry : mem.entrySet()) {
                                String name = entry.getKey();
                                MemoryUsage memoryDetail = entry.getValue();
                                if ("PS Old Gen".equals(name)) {
                                    memoryStatus = findStatus(memoryDetail.getUsed(), memoryDetail.getMax());
                                }
                            }
                            totalGcDuration += info.getGcInfo().getDuration();
                        }
                    }
                };

                //Add the listener
                emitter.addNotificationListener(listener, null, null);
            }
        }
    }

    private MemoryStatus findStatus(long used, long max) {
        double fraction = used / (double) max;
        for (int i = MemoryStatus.values().length - 1; i >= 0; i--) {
            MemoryStatus curr = MemoryStatus.values()[i];
            if (curr.getMinMemoryUsage() <= fraction) {
                return curr;
            }
        }
        return MemoryStatus.FREE;
    }

}
