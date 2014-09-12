package be.bow.cache;

public enum CacheImportance {

    NOT_IMPORTANT(1000), DEFAULT(10 * 1000), IMPORTANT(50 * 1000), VERY_IMPORTANT(100 * 1000);

    private long minimumKeepAliveInMs;

    CacheImportance(long minimumKeepAliveInMs) {
        this.minimumKeepAliveInMs = minimumKeepAliveInMs;
    }

    public long getMinimumKeepAliveInMs() {
        return minimumKeepAliveInMs;
    }
}
