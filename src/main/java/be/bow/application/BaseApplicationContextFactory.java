package be.bow.application;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.io.support.ResourcePatternResolver;

public abstract class BaseApplicationContextFactory implements RunnableApplicationContextFactory {

    private final Class<? extends MainClass> mainClass;
    private AnnotationConfigApplicationContext applicationContext;

    protected BaseApplicationContextFactory(Class<? extends MainClass> mainClass) {
        this.mainClass = mainClass;
        applicationContext = new AnnotationConfigApplicationContext();
        applicationContext.register(mainClass);
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

    public Class<? extends MainClass> getMainClass() {
        return mainClass;
    }

    @Override
    public AnnotationConfigApplicationContext createApplicationContext() {
        applicationContext.getBeanFactory().registerSingleton("applicationContextFactory", this);
        return applicationContext;
    }

    @Override
    public String getApplicationName() {
        return getMainClass().getSimpleName();
    }
}
