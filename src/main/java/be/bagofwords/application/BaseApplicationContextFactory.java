package be.bagofwords.application;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.io.support.ResourcePatternResolver;

public abstract class BaseApplicationContextFactory implements ApplicationContextFactory {

    private AnnotationConfigApplicationContext applicationContext;

    protected BaseApplicationContextFactory() {
        setSaneDefaultsForLog4J();
        applicationContext = new AnnotationConfigApplicationContext();
    }

    protected BaseApplicationContextFactory resourceResolver(ResourcePatternResolver resourcePatternResolver) {
        applicationContext.setResourceLoader(resourcePatternResolver);
        return this;
    }

    protected BaseApplicationContextFactory classLoader(ClassLoader classLoader) {
        applicationContext.setClassLoader(classLoader);
        return this;
    }

    protected BaseApplicationContextFactory singleton(String name, Object object) {
        applicationContext.getBeanFactory().registerSingleton(name, object);
        return this;
    }

    protected BaseApplicationContextFactory bean(Class _class) {
        applicationContext.register(_class);
        return this;
    }

    protected BaseApplicationContextFactory scan(String prefix) {
        applicationContext.scan(prefix);
        return this;
    }

    @Override
    public AnnotationConfigApplicationContext createApplicationContext() {
        singleton("applicationContextFactory", this);
        applicationContext.refresh();
        applicationContext.registerShutdownHook();
        return applicationContext;
    }

    @Override
    public String getApplicationName() {
        return "";
    }

    private static void setSaneDefaultsForLog4J() {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.WARN);
    }
}
