package be.bagofwords.util;

/**
 * Created by koen on 01.11.16.
 */
public interface SegmentedLock {

    void lockRead(long key);

    void unlockRead(long key);

    void lockWrite(long key);

    void unlockWrite(long key);

    void lockReadAll();

    void unlockReadAll();

    void lockWriteAll();

    void unlockWriteAll();
}
