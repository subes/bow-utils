package be.bagofwords.application;

import be.bagofwords.ui.UI;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class ApplicationManager {

    public static void runSafely(MainClass main, BaseApplicationContextFactory factory) {
        runSafely(main, Collections.emptyMap(), factory);
    }

    public static void runSafely(MainClass main) {
        runSafely(main, Collections.emptyMap(), null);
    }

    public static void runSafely(MainClass main, Map<String, String> config) {
        runSafely(main, config, null);
    }

    public static void runSafely(MainClass main, Map<String, String> config, BaseApplicationContextFactory factory) {
        ApplicationContext applicationContext = new ApplicationContext(config);
        try {
            if (factory != null) {
                factory.wireApplicationContext(applicationContext);
            }
            applicationContext.startApplication();
            main.run(applicationContext);
        } catch (Throwable exp) {
            UI.writeError("Received unexpected exception, terminating application.", exp);
        } finally {
            applicationContext.terminateApplication();
        }
    }

    private boolean applicationTerminated;
    private ApplicationContext context;

    public ApplicationManager(ApplicationContext context) {
        this.context = context;
        this.applicationTerminated = false;
        context.getBean(BowTaskScheduler.class).schedulePeriodicTask(this::checkMainMethodTermination, 1000);
    }

    public void checkMainMethodTermination() {
        if (!applicationTerminated) {
            Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
            boolean mainThreadFound = false;
            for (Thread thread : threadSet) {
                if (thread.getId() == 1) {
                    mainThreadFound = true;
                }
            }
            if (!mainThreadFound) {
                context.terminateApplication();
                applicationTerminated = true;
            }
        }
    }
}
