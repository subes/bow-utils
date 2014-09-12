package be.bow.util;

import org.apache.commons.lang3.mutable.MutableInt;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Koen Deschacht (koendeschacht@gmail.com) on 9/11/14.
 */
public class DataLockTest {

    @Test
    public void testDataLock() {
        DataLock lock = new DataLock();
        lock.lockWrite(10);
        lock.lockWrite(11);
        lock.unlockWrite(10);
        lock.lockRead(10);
        lock.lockRead(10);
        lock.unlockRead(10);
        lock.unlockRead(10);
        lock.unlockWrite(11);
    }

    @Test
    public void testDataLockMultipleThreads() {
        final DataLock lock = new DataLock();
        final MutableInt valueToWrite = new MutableInt(0);
        lock.lockWrite(10);
        lock.lockWrite(11);
        Thread t = new Thread() {
            public void run() {
                lock.lockWrite(10);
                valueToWrite.setValue(2);
                lock.lockWrite(11);
                valueToWrite.setValue(4);
            }
        };
        t.start();
        Utils.threadSleep(100);
        Assert.assertEquals(0, valueToWrite.longValue());
        valueToWrite.setValue(1);
        Assert.assertEquals(1, valueToWrite.longValue());
        lock.unlockWrite(10);
        Utils.threadSleep(100);
        Assert.assertEquals(2, valueToWrite.longValue());
        valueToWrite.setValue(3);
        Utils.threadSleep(100);
        Assert.assertEquals(3, valueToWrite.longValue());
        lock.unlockWrite(11);
        Utils.threadSleep(100);
        Assert.assertEquals(4, valueToWrite.longValue());
    }

}
