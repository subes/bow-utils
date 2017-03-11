package be.bagofwords.application;

import be.bagofwords.ui.UI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static be.bagofwords.util.Utils.noException;

public class ApplicationContext {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationContext.class);

    private List<Object> beans;

    private String applicationName;

    private Properties properties;
    private final LifeCycleHandler lifeCycleHandler = new LifeCycleHandler();
    private final BeanCreator beanCreator = new BeanCreator();

    public ApplicationContext() {
        this(Collections.emptyMap());
    }

    public ApplicationContext(Map<String, String> config) {
        this.applicationName = config.getOrDefault("application_name", "some_application");
        this.beans = new ArrayList<>();
        noException(() -> readProperties(config));
    }

    private void readProperties(Map<String, String> config) throws IOException {
        String propertyFile = System.getProperty("property-file");
        Properties defaultProperties = new Properties();
        InputStream defaultPropertiesInputStream = this.getClass().getResourceAsStream("/default.properties");
        if (defaultPropertiesInputStream == null) {
            logger.info("Could not find default.properties");
        } else {
            defaultProperties.load(defaultPropertiesInputStream);
        }
        properties = new Properties(defaultProperties);
        if (propertyFile == null) {
            logger.info("No property file specified. You can specify one with -Dproperty-file=\"/some/path\"");
        } else {
            properties.load(new FileInputStream(propertyFile));
        }
        properties.putAll(config);
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getConfig(String name, String defaultValue) {
        String value = properties.getProperty(name, null);
        if (value == null) {
            if (defaultValue == null) {
                throw new RuntimeException("The configuration option " + name + " was not found");
            } else {
                UI.writeWarning("No configuration found for " + name + ", using default value " + defaultValue);
                value = defaultValue;
            }
        }
        return value;
    }

    public String getConfig(String name) {
        return getConfig(name, null);
    }

    public synchronized <T> void registerBean(T bean) {
        beans.add(bean);
    }

    public <T> List<T> getBeans(Class<T> interfaceClass) {
        List<T> result = new ArrayList<>();
        for (Object bean : beans) {
            if (interfaceClass.isAssignableFrom(bean.getClass())) {
                result.add(interfaceClass.cast(bean));
            }
        }
        return result;
    }

    public <T> T getBeanIfPresent(Class<T> interfaceClass) {
        List<T> beans = getBeans(interfaceClass);
        if (beans.size() == 1) {
            return beans.get(0);
        } else if (beans.size() == 0) {
            return null;
        } else {
            throw new RuntimeException("Found " + beans.size() + " beans of type " + interfaceClass);
        }
    }

    public <T> T getBean(Class<T> interfaceClass) {
        List<T> matchingBeans = getBeans(interfaceClass);
        if (matchingBeans.size() == 1) {
            return matchingBeans.get(0);
        } else {
            T bean = beanCreator.createBean(interfaceClass, this);
            beans.add(bean);
            if (bean instanceof LifeCycleBean) {
                lifeCycleHandler.ensureBeanCorrectState((LifeCycleBean) bean);
            }
            return bean;
        }
    }

    public <T> void ensureBeanCreated(Class<T> beanClass) {
        getBean(beanClass);
    }


    public void startApplication() {
        lifeCycleHandler.startApplication(this);
    }

    public void terminateApplication() {
        lifeCycleHandler.terminateApplication(this);
    }

    public synchronized void waitUntilBeanStopped(LifeCycleBean bean) {
        lifeCycleHandler.waitUntilBeanStopped(bean);
    }

    public synchronized void waitUntilBeanStarted(LifeCycleBean bean) {
        lifeCycleHandler.waitUntilBeanStarted(bean);
    }

    public void waitUntilTerminated() {
        lifeCycleHandler.waitUntilTerminated();
    }
}
