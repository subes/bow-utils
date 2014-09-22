package be.bow.application;

import be.bow.ui.UI;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class ApplicationManager {

    public static void runSafely(RunnableApplicationContextFactory applicationContextFactory) {
        setSaneDefaultsForLog4J();
        AnnotationConfigApplicationContext applicationContext = null;
        try {
            applicationContext = applicationContextFactory.createApplicationContext();
            applicationContext.refresh();
            MainClass instance = applicationContextFactory.getMainClass();
            applicationContext.getAutowireCapableBeanFactory().autowireBean(instance);
            instance.run();
        } catch (Throwable exp) {
            UI.writeError("Received unexpected exception, terminating application.", exp);
        } finally {
            System.gc(); //We might need some finalizers to run
            if (applicationContext != null) {
                applicationContext.getBean(ApplicationLifeCycle.class).terminateApplication();
                applicationContext.close();
            }
        }
        UI.write("Application was terminated. Goodbye!");
    }

    private static void setSaneDefaultsForLog4J() {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.WARN);
    }

}
