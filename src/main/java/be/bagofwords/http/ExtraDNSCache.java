package be.bagofwords.http;

import be.bagofwords.ui.UI;
import be.bagofwords.util.Pair;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Semaphore;

public class ExtraDNSCache {

    //This class was introduced to solve some troubles with the default java DNS implementation
    //
    // Two problems are being solved:
    // - occasional bursts of UnknownHostExceptions for valid hosts under certain conditions
    // - blocking of Inet6AddressImpl.lookupAllHostAddr(..) under certain conditions.

    public static final int TIME_TO_KEEP_ADDRESSES = 1000 * 60 * 60; //Keep addresses for max. one hour
    public static final int TIME_TO_KEEP_NEGATIVE_ADDRESSES = 1000 * 60; //Keep negative addresses for max. one minute

    private static long timeOfLastClean = 0;
    private static final Map<String, Pair<InetAddress, Long>> storedAddresses = new HashMap<>();

    private static final Semaphore fetchDnsAddressLock = new Semaphore(5); //only allow 5 simultaneous DNS requests

    public static InetAddress getAddress(String host) throws UnknownHostException {
        if (timeToClean()) {
            synchronized (storedAddresses) {
                if (timeToClean()) {
                    cleanOldAddresses();
                }
            }
        }
        Pair<InetAddress, Long> cachedAddress;
        synchronized (storedAddresses) {
            cachedAddress = storedAddresses.get(host);
        }
        if (cachedAddress != null) {
            //host DNS entry was cached
            InetAddress address = cachedAddress.getFirst();
            if (address == null) {
                throw new UnknownHostException("Could not find host " + host + " (cached response)");
            } else {
                return address;
            }
        } else {
            //host DNS entry was not cached
            fetchDnsAddressLock.acquireUninterruptibly();
            try {
                InetAddress addr = InetAddress.getByName(host);
                synchronized (storedAddresses) {
                    storedAddresses.put(host, new Pair<>(addr, System.currentTimeMillis()));
                }
                return addr;
            } catch (UnknownHostException exp) {
                synchronized (storedAddresses) {
                    storedAddresses.put(host, new Pair<InetAddress, Long>(null, System.currentTimeMillis()));
                }
                UI.write("[Dns lookup] " + host + " --> not found");
                throw exp;
            } finally {
                fetchDnsAddressLock.release();
            }

        }
    }


    private static void cleanOldAddresses() {
        //Occasionally, we remove really old addresses :
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<String, Pair<InetAddress, Long>>> iterator = storedAddresses.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Pair<InetAddress, Long>> curr = iterator.next();
            boolean removeEntry;
            if (curr.getValue().getFirst() == null) {
                //negative address
                removeEntry = curr.getValue().getSecond() + TIME_TO_KEEP_NEGATIVE_ADDRESSES < now;
            } else {
                removeEntry = curr.getValue().getSecond() + TIME_TO_KEEP_ADDRESSES < now;
            }
            if (removeEntry) {
                iterator.remove();
            }
        }
        timeOfLastClean = System.currentTimeMillis();
    }

    private static boolean timeToClean() {
        return timeOfLastClean + Math.min(TIME_TO_KEEP_NEGATIVE_ADDRESSES, TIME_TO_KEEP_ADDRESSES) / 2 < System.currentTimeMillis();
    }


}
