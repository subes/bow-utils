package be.bagofwords.http;

import be.bagofwords.util.Pair;
import be.bagofwords.util.URLUtils;
import be.bagofwords.util.Utils;

import java.util.HashMap;

public class HttpRequestPacer {

    public static final long MIN_TIME_BETWEEN_INTERNET_ACCESS = 10; //allow access 100 times a second

    private static final String internetDummy = "DUMMY_USED_FOR_SYNCHRONIZATION";
    private long lastAccessToInternet;

    private final HashMap<String, Pair<Long, String>> lastAccessOfDomain;

    public HttpRequestPacer() {
        lastAccessOfDomain = new HashMap<>();
        lastAccessToInternet = System.currentTimeMillis();
    }


    public void allowAccessToInternet() {
        synchronized (internetDummy) {
            while (System.currentTimeMillis() < lastAccessToInternet + MIN_TIME_BETWEEN_INTERNET_ACCESS) {
                Utils.threadSleep(5);
            }
            lastAccessToInternet = System.currentTimeMillis();
        }
    }

    public void allowAccessToDomain(String url, long timeBetweenRequests) {
        Pair<Long, String> lastAccess;
        String domain = URLUtils.getDomain(url);
        synchronized (lastAccessOfDomain) {
            lastAccess = lastAccessOfDomain.get(domain);
            if (lastAccess == null) {
                lastAccess = new Pair<>(0l, domain);
                lastAccessOfDomain.put(domain, lastAccess);
            }
        }
        synchronized (lastAccess) {
            while (lastAccess.getFirst() + timeBetweenRequests > System.currentTimeMillis()) {
                Utils.threadSleep(5);
            }
            lastAccess.setFirst(System.currentTimeMillis());
        }
    }

}
