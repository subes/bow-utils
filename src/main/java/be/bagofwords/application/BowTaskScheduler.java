package be.bagofwords.application;

import be.bagofwords.application.annotations.BowComponent;
import be.bagofwords.util.ExecutorServiceFactory;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by Koen Deschacht (koendeschacht@gmail.com) on 14/11/14.
 * <p>
 * Thin wrapper around spring's task scheduler
 */

@BowComponent
public class BowTaskScheduler implements CloseableComponent {

    private ConcurrentTaskScheduler taskScheduler;
    private ExecutorService executor;
    private ScheduledExecutorService scheduledExecutorService;

    public BowTaskScheduler() {
        executor = ExecutorServiceFactory.createExecutorService("periodic_task");
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        taskScheduler = new ConcurrentTaskScheduler(executor, scheduledExecutorService);
    }

    public void schedulePeriodicTask(Runnable task, long period) {
        taskScheduler.scheduleAtFixedRate(task, period);
    }

    @Override
    public void terminate() {
        executor.shutdown();
        scheduledExecutorService.shutdown();
    }
}
