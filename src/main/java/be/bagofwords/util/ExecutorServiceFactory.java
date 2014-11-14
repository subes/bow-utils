package be.bagofwords.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Created by Koen Deschacht (koendeschacht@gmail.com) on 14/11/14.
 */
public class ExecutorServiceFactory {

    private static final int DEFAULT_NUM_OF_THREADS = 8;

    public static ExecutorService createExecutorService(String name) {
        return createExecutorService(DEFAULT_NUM_OF_THREADS, name);
    }

    public static ExecutorService createExecutorService(int numberOfThreads, String name) {
        return Executors.newFixedThreadPool(numberOfThreads, new ThreadFactory() {

            private int threadNr = 0;

            @Override
            public synchronized Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName(name + "_" + threadNr++);
                return t;
            }

        });
    }

}
