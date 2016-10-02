package be.bagofwords.application;

import be.bagofwords.ui.UI;

import java.util.Map;

public class ApplicationManager {


    public static <T extends Runnable> void runSafely(MainClass main, Map<String, String> config, BaseApplicationContextFactory factory) {
        ApplicationContext applicationContext = null;
        try {
            applicationContext = new ApplicationContext(config);
            factory.wireApplicationContext(applicationContext);
            main.run(applicationContext);
        } catch (Throwable exp) {
            UI.writeError("Received unexpected exception, terminating application.", exp);
        } finally {
            if (applicationContext != null) {
                applicationContext.terminateApplication();
            }
        }
        UI.write("Application has terminated. Goodbye!");
    }

}
