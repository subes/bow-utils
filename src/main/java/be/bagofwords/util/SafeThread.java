package be.bagofwords.util;

import be.bagofwords.application.CloseableComponent;
import be.bagofwords.ui.UI;

public abstract class SafeThread extends Thread implements CloseableComponent {

    private boolean terminateRequested;
    private boolean finished;

    public SafeThread(String name, boolean isDaemonThread) {
        super(name);
        super.setDaemon(isDaemonThread);
        finished = false;
        terminateRequested = false;
    }

    public void run() {
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
        terminateRequested = true;
        doTerminate();
        super.interrupt();
    }

    @Override
    public void terminate() {
        interrupt();
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

    protected abstract void runInt() throws Exception;

    public void waitForFinish(long timeToWait) {
        long start = System.currentTimeMillis();
        while (!isFinished() && (timeToWait == -1 || System.currentTimeMillis() - start < timeToWait)) {
            Utils.threadSleep(10);
        }
    }

    public void waitForFinish() {
        waitForFinish(-1);
    }

    public void terminateAndWaitForFinish() {
        terminate();
        waitForFinish();
    }
}
