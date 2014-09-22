package be.bagofwords.counts;

public class WindowOfCounts {

    private final long windowSize;

    private long countsInPreviousWindow;
    private long countsInCurrWindow;
    private long totalCounts;
    private long startOfCurrWindow;
    private long startOfPrevWindow;

    public WindowOfCounts(long windowSize) {
        this.windowSize = windowSize;
        this.startOfCurrWindow = System.currentTimeMillis();
        this.startOfPrevWindow = System.currentTimeMillis();
    }

    public void addCount() {
        if (System.currentTimeMillis() - startOfCurrWindow > windowSize) {
            synchronized (this) {
                if (System.currentTimeMillis() - startOfCurrWindow > windowSize) {
                    countsInPreviousWindow = countsInCurrWindow;
                    countsInCurrWindow = 0;
                    startOfPrevWindow = startOfCurrWindow;
                    startOfCurrWindow = System.currentTimeMillis();
                }
            }
        }
        countsInCurrWindow++;
        totalCounts++;
    }

    public double getMsPerCount() {
        double totalCounts = countsInPreviousWindow + countsInCurrWindow;
        if (totalCounts == 0) {
            return Double.MAX_VALUE;
        }
        return (System.currentTimeMillis() - startOfPrevWindow) / totalCounts;
    }

    public long getNeededTime(long countsToDo) {
        double msPerCount = getMsPerCount();
        if (msPerCount == Double.MAX_VALUE) {
            return Long.MAX_VALUE;
        } else {
            return Math.round(countsToDo * msPerCount);
        }
    }

    public long getTotalCounts() {
        return totalCounts;
    }

    public long getCounts() {
        return countsInPreviousWindow + countsInCurrWindow;
    }

    public void clearCounts() {
        startOfCurrWindow = System.currentTimeMillis();
        startOfPrevWindow = System.currentTimeMillis();
        totalCounts = 0;
        countsInCurrWindow = 0;
        countsInPreviousWindow = 0;
    }
}

