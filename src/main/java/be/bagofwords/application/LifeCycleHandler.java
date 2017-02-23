package be.bagofwords.application;

import be.bagofwords.ui.UI;
import be.bagofwords.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by koen on 23/02/17.
 */
public class LifeCycleHandler {

    private static final Logger logger = LoggerFactory.getLogger(LifeCycleHandler.class);
    private boolean applicationWasTerminated = false;
    private boolean applicationWasStarted = false;
    private final Set<LifeCycleBean> stoppedBeans = new HashSet<>();
    private final Set<LifeCycleBean> beansBeingStopped = new HashSet<>();
    private final Set<LifeCycleBean> beansBeingStarted = new HashSet<>();
    private final Set<LifeCycleBean> startedBeans = new HashSet<>();

    public synchronized void terminateApplication(ApplicationContext applicationContext) {
        if (!applicationWasTerminated) {
            List<? extends LifeCycleBean> lifeCycleBeans = applicationContext.getBeans(LifeCycleBean.class);
            for (LifeCycleBean bean : lifeCycleBeans) {
                waitUntilBeanStopped(bean);
            }
            applicationWasTerminated = true;
            UI.write("Application has terminated. Goodbye!");
        } else {
            logger.info("Application was already terminated...");
        }
    }

    public void startApplication(ApplicationContext applicationContext) {
        if (!applicationWasStarted) {
            List<? extends LifeCycleBean> lifeCycleBeans = applicationContext.getBeans(LifeCycleBean.class);
            for (LifeCycleBean bean : lifeCycleBeans) {
                waitUntilBeanStarted(bean);
            }
            applicationWasStarted = true;
            UI.write("Application has terminated. Goodbye!");
        } else {
            logger.info("Application was already started...");
        }
    }

    public synchronized void waitUntilBeanStopped(LifeCycleBean bean) {
        if (beansBeingStopped.contains(bean)) {
            throw new RuntimeException("The stop() method of bean " + bean + " was already called. Possible cycle?");
        }
        if (stoppedBeans.contains(bean)) {
            return;
        }
        beansBeingStopped.add(bean);
        logger.info("Stopping bean " + bean);
        bean.stopBean();
        beansBeingStopped.remove(bean);
        stoppedBeans.add(bean);
    }

    public synchronized void waitUntilBeanStarted(LifeCycleBean bean) {
        if (beansBeingStarted.contains(bean)) {
            throw new RuntimeException("The stop() method of bean " + bean + " was already called. Possible cycle?");
        }
        if (startedBeans.contains(bean)) {
            return;
        }
        beansBeingStarted.add(bean);
        logger.info("Starting bean " + bean);
        bean.startBean();
        beansBeingStarted.remove(bean);
        startedBeans.add(bean);
    }

    public void waitUntilTerminated() {
        while (!applicationWasTerminated) {
            Utils.threadSleep(500);
        }
    }

    public boolean applicationWasTerminated() {
        return applicationWasTerminated;
    }

    public boolean applicationWasStarted() {
        return applicationWasStarted;
    }

    public void ensureBeanCorrectState(LifeCycleBean bean) {
        if (applicationWasStarted() && !applicationWasTerminated()) {
            waitUntilBeanStarted(bean);
        }
        if (applicationWasTerminated()) {
            waitUntilBeanStopped(bean);
        }
    }
}
