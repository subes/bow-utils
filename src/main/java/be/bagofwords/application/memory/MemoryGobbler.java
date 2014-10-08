package be.bagofwords.application.memory;

/**
 * Interface to be implemented by components that use up a lot of memory and that can free some memory if
 * the JVM is running low on memory (e.g. caches)
 */

public interface MemoryGobbler {

    void freeMemory();

    String getMemoryUsage();

}
