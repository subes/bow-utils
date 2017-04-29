package be.bagofwords.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by koen on 28/04/17.
 */
public class SafeThreadTest {

    @Test
    public void testTerminate() throws InterruptedException {
        testCorrectExecution(new TestThread(false), true);
        testCorrectExecution(new TestThread(false), false);
    }

    @Test
    public void testTerminateIfThreadThrowsException() throws InterruptedException {
        testCorrectExecution(new TestThread(true), true);
        testCorrectExecution(new TestThread(true), false);
    }

    private void testCorrectExecution(TestThread t, boolean useJoin) throws InterruptedException {
        t.start();
        if (useJoin) {
            t.join();
        } else {
            t.waitForFinish();
        }
        Assert.assertTrue(t.didRun);
        Assert.assertTrue(t.cleanupMethodCalled);
    }

    public static class TestThread extends SafeThread {

        private final boolean shouldThrowError;
        public boolean didRun = false;
        public boolean cleanupMethodCalled = false;

        public TestThread(boolean shouldThrowError) {
            super("test-thread", false);
            this.shouldThrowError = shouldThrowError;
        }

        @Override
        protected void runImpl() throws Exception {
            Thread.sleep(1000);
            didRun = true;
            if (shouldThrowError) {
                throw new Exception("test exception");
            }
        }

        @Override
        public void cleanUp() {
            super.cleanUp();
            this.cleanupMethodCalled = true;
        }
    }

}