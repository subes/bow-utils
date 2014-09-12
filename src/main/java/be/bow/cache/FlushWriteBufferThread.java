package be.bow.cache;

import be.bow.application.memory.MemoryManager;
import be.bow.application.memory.MemoryStatus;
import be.bow.ui.UI;
import be.bow.util.SafeThread;
import be.bow.util.Utils;

public class FlushWriteBufferThread extends SafeThread {

    private final CachesManager cachesManager;
    private final MemoryManager memoryManager;

    public FlushWriteBufferThread(CachesManager cachesManager, MemoryManager memoryManager) {
        super("FlushWriteBuffer", false);
        this.cachesManager = cachesManager;
        this.memoryManager = memoryManager;
    }

    @Override
    public void runInt() {
        while (!isTerminateRequested()) {
            try {
                long timeDiff = CachesManager.TIME_BETWEEN_FLUSHES_FOR_WRITE_BUFFER;
                if (memoryManager.getMemoryStatus() == MemoryStatus.CRITICAL) {
                    timeDiff = 0;
                }
                cachesManager.flushWriteBuffers(timeDiff);
            } catch (Throwable t) {
                UI.writeError("Received exception while flushing write buffers!", t);
            }
            Utils.threadSleep(CachesManager.TIME_BETWEEN_FLUSHES_FOR_WRITE_BUFFER / 10);
        }
    }
}
