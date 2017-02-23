package be.bagofwords.util;

import be.bagofwords.application.LifeCycleBean;
import be.bagofwords.ui.UI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SafeThread extends Thread implements LifeCycleBean {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private boolean terminateRequested;
    private boolean finished;
    private boolean started;

    public SafeThread(String name, boolean isDaemonThread) {
        super(name);
        super.setDaemon(isDaemonThread);
        finished = false;
        started = false;
        terminateRequested = false;
    }

    public void run() {
        started = true;
        try {
            runInt();
        } catch (Throwable t) {
            UI.writeError("Received exception while running " + getName(), t);
        } finally {
            finished = true;
        }
    }

    @Override
    public void interrupt() {
        terminate();
    }

    public void terminate() {
        terminateRequested = true;
        doTerminate();
    }

    protected void doTerminate() {
        //Default implementation is empty
    }

    public boolean isTerminateRequested() {
        return terminateRequested;
    }

    public boolean isFinished() {
        return finished;
    }

    public boolean wasStarted() {
        return started;
    }

    protected abstract void runInt() throws Exception;

    public void waitForFinish(long timeToWait) {
        long start = System.currentTimeMillis();
        long timeOfLastMessage = start;
        while (wasStarted() && !isFinished() && (timeToWait == -1 || System.currentTimeMillis() - start < timeToWait)) {
            Utils.threadSleep(10);
            if (System.currentTimeMillis() - timeOfLastMessage > 10 * 1000 && isTerminateRequested()) {
                UI.write("Waiting for thread " + getName() + " to finish");
                timeOfLastMessage = System.currentTimeMillis();
            }
        }
    }

    public void waitForFinish() {
        waitForFinish(-1);
    }

    public void terminateAndWaitForFinish() {
        terminate();
        waitForFinish();
    }

    @Override
    public void startBean() {
        start();
    }

    @Override
    public void stopBean() {
        terminate();
        waitForFinish(10 * 1000);
        if (!finished) {
            logger.warn("Bean " + getName() + " did not stop after waiting 10s");
        }
    }
}
