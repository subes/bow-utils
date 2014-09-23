package be.bagofwords.application;

import be.bagofwords.ui.UI;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class ApplicationManager {

    public static void runSafely(RunnableApplicationContextFactory applicationContextFactory) {
        AnnotationConfigApplicationContext applicationContext = null;
        try {
            applicationContext = applicationContextFactory.createApplicationContext();
            MainClass instance = applicationContextFactory.getMainClass();
            applicationContext.getAutowireCapableBeanFactory().autowireBean(instance);
            instance.run();
        } catch (Throwable exp) {
            UI.writeError("Received unexpected exception, terminating application.", exp);
        } finally {
            if (applicationContext != null) {
                applicationContext.close();
            }
        }
        UI.write("Application was terminated. Goodbye!");
    }

}
