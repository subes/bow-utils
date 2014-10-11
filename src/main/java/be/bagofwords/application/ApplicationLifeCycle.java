package be.bagofwords.application;

import be.bagofwords.application.annotations.EagerBowComponent;
import be.bagofwords.util.SafeThread;
import be.bagofwords.util.SpringUtils;
import be.bagofwords.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

import java.util.List;

@EagerBowComponent
public class ApplicationLifeCycle implements ApplicationListener<ContextClosedEvent> {

    private boolean applicationWasTerminated = false;

    @Autowired
    private ApplicationContext applicationContext;

    public ApplicationLifeCycle() {
    }

    public synchronized void terminateApplication() {
        if (!applicationWasTerminated) {
            List<? extends CloseableComponent> terminatableBeans = SpringUtils.getInstantiatedBeans(applicationContext, CloseableComponent.class);
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

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        terminateApplication();
    }
}
