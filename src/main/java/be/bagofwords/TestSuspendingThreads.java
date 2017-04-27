package be.bagofwords;

import be.bagofwords.logging.Log;
import be.bagofwords.util.Utils;

import java.util.concurrent.CountDownLatch;

/**
 * Created by Koen Deschacht (koendeschacht@gmail.com) on 14/10/14.
 */
public class TestSuspendingThreads {

    public static void main(String[] args) {
        measureAvgTime();
        measureWithNative();
        measureHacked();
        measureWithNative();
        measureHacked();
    }

    private static void measureHacked() {
        Thread measureStackTraces = new Thread() {
            public void run() {
                while (true) {
                    Thread currentThread = Thread.currentThread();
                    ThreadGroup currentGroup = currentThread.getThreadGroup();
                    Thread[] lstThreads = new Thread[100];
                    currentGroup.enumerate(lstThreads);
                    for (Thread thread : lstThreads) {
                        if (thread != null && thread != currentThread && thread.isAlive()) {
                            thread.suspend();
                            currentThread.getStackTrace();
                            thread.resume();
                        }
                    }
                    Utils.threadSleep(500);
                }
            }
        };
        measureStackTraces.start();
        measureAvgTime();
        measureStackTraces.stop();
    }

    private static void measureWithNative() {
        Thread measureStackTraces = new Thread() {
            public void run() {
                while (true) {
                    Thread.getAllStackTraces();
                    Utils.threadSleep(500);
                }
            }
        };
        measureStackTraces.start();
        measureAvgTime();
        measureStackTraces.stop();
    }

    private static void measureAvgTime() {
        long sum = 0;
        for (int i = 0; i < 10; i++) {
            long start = System.currentTimeMillis();
            doTask();
            sum += System.currentTimeMillis() - start;
        }
        Log.i("Avg took " + (sum / 10) + " ms");
    }

    private static void doTask() {
        final CountDownLatch countDownLatch = new CountDownLatch(20);
        for (int i = 0; i < 30; i++) {
            new Thread() {
                @Override
                public void run() {
                    double sum = 0;
                    for (int i = 0; i < 4000000; i++) {
                        sum += Math.pow(i, 20);
                    }
                    countDownLatch.countDown();
                }
            }.start();
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


}
