package be.bagofwords.application;

import be.bagofwords.ui.UI;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseApplicationContextFactory implements ApplicationContextFactory {

    private AnnotationConfigApplicationContext applicationContext;
    private List<Object> singletons;
    private boolean triedToCloseOnce = false;

    protected BaseApplicationContextFactory() {
        setSaneDefaultsForLog4J();
        applicationContext = new AnnotationConfigApplicationContext();
        singletons = new ArrayList<>();
    }

    protected BaseApplicationContextFactory resourceResolver(ResourcePatternResolver resourcePatternResolver) {
        applicationContext.setResourceLoader(resourcePatternResolver);
        return this;
    }

    protected BaseApplicationContextFactory classLoader(ClassLoader classLoader) {
        applicationContext.setClassLoader(classLoader);
        return this;
    }

    protected synchronized BaseApplicationContextFactory singleton(String name, Object object) {
        applicationContext.getBeanFactory().registerSingleton(name, object);
        singletons.add(object);
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
        wireSingletons();
        return applicationContext;
    }

    /**
     * Ugly method, seems we are doing springs job here...
     */

    private void wireSingletons() {
        for (Object singleton : singletons) {
            applicationContext.getAutowireCapableBeanFactory().autowireBean(singleton);
        }
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
