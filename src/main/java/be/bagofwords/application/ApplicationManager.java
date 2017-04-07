package be.bagofwords.application;

import be.bagofwords.ui.UI;

import java.util.Map;
import java.util.Set;

public class ApplicationManager {


    public static <T extends Runnable> void runSafely(MainClass main, Map<String, String> config, BaseApplicationContextFactory factory) {
        try {
            ApplicationContext applicationContext = new ApplicationContext(config);
            factory.wireApplicationContext(applicationContext);
            main.run(applicationContext);
        } catch (Throwable exp) {
            UI.writeError("Received unexpected exception, terminating application.", exp);
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
