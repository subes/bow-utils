package be.bow.util;

public abstract class OccasionalAction<T extends Object> {

    private final long interval;
    private long previous;
    private long start;

    public OccasionalAction(long interval) {
        this.interval = interval;
        previous = System.currentTimeMillis();
        start = -1;
    }

    public void doOccasionalAction(T curr) {
        long timeNow = System.currentTimeMillis();
        if (start == -1) {
            start = timeNow;
        }
        if (timeNow - previous > interval) {
            previous = timeNow;
            doAction(curr);
        }
    }

    public long getStart() {
        return start;
    }

    protected abstract void doAction(T curr);

}
