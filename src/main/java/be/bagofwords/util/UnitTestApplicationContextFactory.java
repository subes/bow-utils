package be.bagofwords.util;

import be.bagofwords.application.ApplicationContextFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Created by Koen Deschacht (koendeschacht@gmail.com) on 9/4/14.
 */
public class UnitTestApplicationContextFactory implements ApplicationContextFactory {

    private AnnotationConfigApplicationContext context;

    public UnitTestApplicationContextFactory() {
        context = new AnnotationConfigApplicationContext();
    }

    @Override
    public void wireApplicationContext() {
        context.getBeanFactory().registerSingleton("applicationContextFactory", this);
        context.scan("be.bagofwords");
    }

    @Override
    public AnnotationConfigApplicationContext getApplicationContext() {
        return context;
    }

    @Override
    public String getApplicationName() {
        return "UnitTest";
    }
}
