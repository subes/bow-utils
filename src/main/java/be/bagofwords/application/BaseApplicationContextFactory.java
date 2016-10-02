package be.bagofwords.application;

import java.util.Map;

public class BaseApplicationContextFactory {


    public ApplicationContext createApplicationContext(Map<String, String> config) {
        ApplicationContext context = new ApplicationContext(config);
        wireApplicationContext(context);
        return context;
    }

    public void wireApplicationContext(ApplicationContext context) {
        //Do nothing
    }

}
