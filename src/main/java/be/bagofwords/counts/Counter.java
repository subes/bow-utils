package be.bagofwords.counts;

import be.bagofwords.ui.UI;
import be.bagofwords.util.Pair;
import org.codehaus.jackson.annotate.JsonIgnore;

import java.util.*;
import java.util.concurrent.Semaphore;

public class Counter<T extends Object> {

    private final Map<T, Long> counts;
    private long cachedTotal;
    private Semaphore lock;

    public Counter() {
        counts = new HashMap<>();
        cachedTotal = -1;
        lock = new Semaphore(1000);
    }

    public void inc(T s) {
        inc(s, 1l);
    }

    public synchronized void inc(T s, long count) {
        if (counts.containsKey(s)) {
            lock.acquireUninterruptibly(1);
            counts.put(s, counts.get(s) + count);
            lock.release();
        } else {
            lock.acquireUninterruptibly(1000);
            counts.put(s, count);
            lock.release(1000);
        }
        cachedTotal = -1;
    }

    public void print() {
        List<Map.Entry<T, Long>> orderedCounts = new ArrayList<>(counts.entrySet());
        Collections.sort(orderedCounts, new Comparator<Map.Entry<T, Long>>() {
            @Override
            public int compare(Map.Entry<T, Long> o1, Map.Entry<T, Long> o2) {
                return -o1.getValue().compareTo(o2.getValue());
            }
        });
        for (Map.Entry<T, Long> s : orderedCounts) {
            UI.write(s.getKey() + " (" + s.getValue() + ")");
        }
    }

    public Set<T> keySet() {
        return counts.keySet();
    }

    public List<T> sortedKeys() {
        Set<T> keys = counts.keySet();
        List<T> result = new ArrayList<>(keys);
        Collections.sort(result, new Comparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                return -Long.compare(get(o1), get(o2));
            }
        });
        return result;
    }

    public long get(T s) {
        Long count = counts.get(s);
        if (count == null) {
            return 0;
        } else {
            return count;
        }
    }

    @JsonIgnore
    public long getTotal() {
        if (cachedTotal == -1) {
            cachedTotal = 0;
            for (Long val : counts.values()) {
                cachedTotal += val;
            }
        }
        return cachedTotal;
    }

    public Set<Map.Entry<T, Long>> entrySet() {
        return counts.entrySet();
    }

    public Counter<T> clone() {
        Counter<T> clone = new Counter<>();
        clone.getMap().putAll(getMap());
        return clone;
    }

    @JsonIgnore
    public Map<T, Long> getMap() {
        return counts;
    }

    public int size() {
        return counts.size();
    }

    public void set(T s, long value) {
        counts.put(s, value);
    }

    public void addAll(Counter<T> other) {
        for (Map.Entry<T, Long> entry : other.entrySet()) {
            inc(entry.getKey(), entry.getValue());
        }
    }

    public void addAll(List<Pair<T, Long>> values) {
        counts.clear();
        for (Pair<T, Long> value : values) {
            counts.put(value.getFirst(), value.getSecond());
        }
    }

    public void trim(int maxSize) {
        if (maxSize <= 0) {
            throw new RuntimeException("Incorrect max size:" + maxSize);
        }
        if (size() > maxSize) {
            List<T> sortedKeys = sortedKeys();
            for (int i = maxSize; i < sortedKeys.size(); i++) {
                counts.remove(sortedKeys.get(i));
            }
        }
    }

    public void clear() {
        counts.clear();
    }

    public List<Pair<T, Long>> getValuesAsList() {
        List<Pair<T, Long>> result = new ArrayList<>();
        for (Map.Entry<T, Long> entry : counts.entrySet()) {
            result.add(new Pair<>(entry.getKey(), entry.getValue()));
        }
        return result;
    }

    public void setValuesAsList(List<Pair<T, Long>> values) {
        counts.clear();
        for (Pair<T, Long> value : values) {
            counts.put(value.getFirst(), value.getSecond());
        }
    }
}
