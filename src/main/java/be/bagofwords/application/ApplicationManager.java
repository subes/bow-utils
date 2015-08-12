package be.bagofwords.application;

import be.bagofwords.ui.UI;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class ApplicationManager {

    public static void runSafely(ApplicationContextFactory applicationContextFactory) {
        try {
            applicationContextFactory.wireApplicationContext();
            AnnotationConfigApplicationContext applicationContext = applicationContextFactory.getApplicationContext();
            MainClass instance = applicationContext.getBean(MainClass.class);
            applicationContext.start();
            instance.run();
        } catch (Throwable exp) {
            UI.writeError("Received unexpected exception, terminating application.", exp);
        } finally {
            AnnotationConfigApplicationContext applicationContext = applicationContextFactory.getApplicationContext();
            if (applicationContext != null) {
                applicationContext.stop();
                applicationContext.close();
            }
        }
        UI.write("Application has terminated. Goodbye!");
    }

}
