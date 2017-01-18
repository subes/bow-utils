package be.bagofwords.application;

import be.bagofwords.ui.UI;
import be.bagofwords.util.SafeThread;
import be.bagofwords.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ApplicationContext {

    private List<Object> beans;
    private Map<String, String> config;
    private boolean applicationWasTerminated = false;
    private String applicationName;

    public ApplicationContext(Map<String, String> config) {
        this.applicationName = config.getOrDefault("application_name", "some_application");
        this.config = config;
        this.beans = new ArrayList<>();
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getConfig(String name, String defaultValue) {
        String value = config.getOrDefault(name, null);
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
        List<T> beans = getBeans(interfaceClass);
        if (beans.size() == 1) {
            return beans.get(0);
        } else {
            throw new RuntimeException("Found " + beans.size() + " beans of type " + interfaceClass);
        }
    }

    public synchronized void terminateApplication() {
        if (!applicationWasTerminated) {
            List<? extends CloseableComponent> terminatableBeans = getBeans(CloseableComponent.class);
            for (CloseableComponent object : terminatableBeans) {
                if (!(object instanceof LateCloseableComponent)) {
                    object.terminate();
                }
            }
            for (CloseableComponent object : terminatableBeans) {
                if (!(object instanceof LateCloseableComponent) && object instanceof SafeThread) {
                    ((SafeThread) object).waitForFinish();
                }
            }
            for (CloseableComponent object : terminatableBeans) {
                if (object instanceof LateCloseableComponent) {
                    object.terminate();
                }
            }
            for (CloseableComponent object : terminatableBeans) {
                if (object instanceof LateCloseableComponent && object instanceof SafeThread) {
                    ((SafeThread) object).waitForFinish();
                }
            }
            applicationWasTerminated = true;
            UI.write("Application has terminated. Goodbye!");
        }
    }

    public void waitUntilTerminated() {
        while (!applicationWasTerminated) {
            Utils.threadSleep(500);
        }
    }

    public boolean applicationWasTerminated() {
        return applicationWasTerminated;
    }
}
