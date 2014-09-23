package be.bagofwords.application;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.io.support.ResourcePatternResolver;

public abstract class BaseApplicationContextFactory implements ApplicationContextFactory {

    private AnnotationConfigApplicationContext applicationContext;

    protected BaseApplicationContextFactory() {
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
        return applicationContext;
    }


}
