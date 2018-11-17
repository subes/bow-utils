package be.bagofwords.util;

import be.bagofwords.logging.Log;

public abstract class SafeThread extends Thread {

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
            runImpl();
        } catch (Throwable t) {
            Log.e("Unexpected exception while running " + getName(), t);
        } finally {
            try {
                cleanUp();
            } catch (Throwable t) {
                Log.e("Unexpected exception while cleaning up " + getName(), t);
            }
            finished = true;
        }
    }

    @Override
    public void interrupt() {
        terminateRequested = true;
        doTerminate();
        super.interrupt();
    }

    protected void doTerminate() {
        //Default implementation is empty
    }

    public void cleanUp() {
        //Default implementation is empty
    }

    public boolean isTerminateRequested() {
        return terminateRequested;
    }

    public boolean isFinished() {
        return finished;
    }

    protected abstract void runImpl() throws Exception;

    public void waitForFinish(long timeToWait) {
        long start = System.currentTimeMillis();
        while (!isFinished() && (timeToWait == -1 || System.currentTimeMillis() - start < timeToWait)) {
            try {
                long maxTimeToWait = 10_000;
                if (timeToWait != -1) {
                    maxTimeToWait = Math.min(maxTimeToWait, timeToWait + start - System.currentTimeMillis());
                }
                this.join(maxTimeToWait);
            } catch (InterruptedException e) {
                //ok
            }
            if (!isFinished() && isTerminateRequested()) {
                Log.i("Waiting for thread " + getName() + " to finish");
            }
        }
    }

    public void waitForFinish() {
        waitForFinish(-1);
    }

    public void terminateAndWaitForFinish() {
        interrupt();
        waitForFinish();
    }

}
