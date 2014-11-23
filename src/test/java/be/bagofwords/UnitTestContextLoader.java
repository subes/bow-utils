package be.bagofwords;

import be.bagofwords.application.ApplicationContextFactory;
import be.bagofwords.application.BaseApplicationContextFactory;
import be.bagofwords.application.EnvironmentProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.test.context.ContextLoader;

/**
 * Created by Koen Deschacht (koendeschacht@gmail.com) on 23/11/14.
 */

public class UnitTestContextLoader implements ContextLoader {


    @Override
    public String[] processLocations(Class<?> aClass, String... locations) {
        return locations;
    }

    @Override
    public ApplicationContext loadContext(String... locations) throws Exception {
        ApplicationContextFactory applicationContextFactory = new UnitTestApplicationContextFactory();
        return applicationContextFactory.createApplicationContext();
    }

    private class UnitTestApplicationContextFactory extends BaseApplicationContextFactory {

        @Override
        public AnnotationConfigApplicationContext createApplicationContext() {
            scan("be.bagofwords");
            singleton("properties", new EnvironmentProperties() {
                @Override
                public boolean saveThreadSamplesToFile() {
                    return false;
                }

                @Override
                public String getThreadSampleLocation() {
                    return null;
                }

                @Override
                public String getApplicationUrlRoot() {
                    return "";
                }
            });
            return super.createApplicationContext();
        }
    }
}
