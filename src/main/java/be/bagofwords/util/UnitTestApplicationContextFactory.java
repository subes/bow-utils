package be.bagofwords.util;

import be.bagofwords.application.ApplicationContextFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Created by Koen Deschacht (koendeschacht@gmail.com) on 9/4/14.
 */
public class UnitTestApplicationContextFactory implements ApplicationContextFactory {

    @Override
    public AnnotationConfigApplicationContext createApplicationContext() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.getBeanFactory().registerSingleton("applicationContextFactory", this);
        context.scan("be/bow");
        return context;
    }

    @Override
    public String getApplicationName() {
        return "UnitTest";
    }
}
