package be.bagofwords.util;

import be.bagofwords.application.ApplicationContextFactory;
import org.springframework.context.ApplicationContext;
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
    public ApplicationContext wireApplicationContext() {
        context.getBeanFactory().registerSingleton("applicationContextFactory", this);
        context.scan("be.bagofwords");
        return context;
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
