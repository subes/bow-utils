package be.bagofwords.counts;

import be.bagofwords.ui.UI;
import be.bagofwords.util.Pair;
import com.fasterxml.jackson.annotation.JsonIgnore;
import it.unimi.dsi.fastutil.Function;

import java.util.*;
import java.util.concurrent.Semaphore;

public class DoubleCounter<T extends Object> {

    private final Map<T, Double> counts;
    private double cachedTotal;
    private Semaphore lock;

    public static <S, T> DoubleCounter<T> create(Collection<S> items, ItemMapping<S, T> mapping) {
        DoubleCounter<T> result = new DoubleCounter<>();
        for (S item : items) {
            Pair<T, Double> counts = mapping.mapItem(item);
            result.inc(counts.getF(), counts.getS());
        }
        return result;
    }

    public static <S, T> DoubleCounter<T> create(Collection<S> items, Function<S, Pair<T, Double>> mapping) {
        DoubleCounter<T> result = new DoubleCounter<>();
        for (S item : items) {
            Pair<T, Double> counts = mapping.get(item);
            result.inc(counts.getF(), counts.getS());
        }
        return result;
    }

    public DoubleCounter() {
        counts = new HashMap<>();
        cachedTotal = -1;
        lock = new Semaphore(1000);
    }

    public void inc(T s) {
        inc(s, 1.0);
    }

    public synchronized void inc(T s, double count) {
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
        List<Map.Entry<T, Double>> orderedCounts = new ArrayList<>(counts.entrySet());
        Collections.sort(orderedCounts, new Comparator<Map.Entry<T, Double>>() {
            @Override
            public int compare(Map.Entry<T, Double> o1, Map.Entry<T, Double> o2) {
                return -o1.getValue().compareTo(o2.getValue());
            }
        });
        for (Map.Entry<T, Double> s : orderedCounts) {
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
                return -Double.compare(get(o1), get(o2));
            }
        });
        return result;
    }

    public double get(T s) {
        Double count = counts.get(s);
        if (count == null) {
            return 0;
        } else {
            return count;
        }
    }

    @JsonIgnore
    public double getTotal() {
        if (cachedTotal == -1) {
            cachedTotal = 0;
            for (Double val : counts.values()) {
                cachedTotal += val;
            }
        }
        return cachedTotal;
    }

    public Set<Map.Entry<T, Double>> entrySet() {
        return counts.entrySet();
    }

    public DoubleCounter<T> clone() {
        DoubleCounter<T> clone = new DoubleCounter<>();
        clone.getMap().putAll(getMap());
        return clone;
    }

    @JsonIgnore
    public Map<T, Double> getMap() {
        return counts;
    }

    public int size() {
        return counts.size();
    }

    public void set(T s, double value) {
        counts.put(s, value);
    }

    public void addAll(DoubleCounter<T> other) {
        for (Map.Entry<T, Double> entry : other.entrySet()) {
            inc(entry.getKey(), entry.getValue());
        }
    }

    public void addAll(List<Pair<T, Double>> values) {
        counts.clear();
        for (Pair<T, Double> value : values) {
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

    public List<Pair<T, Double>> getValuesAsList() {
        List<Pair<T, Double>> result = new ArrayList<>();
        for (Map.Entry<T, Double> entry : counts.entrySet()) {
            result.add(new Pair<>(entry.getKey(), entry.getValue()));
        }
        return result;
    }

    public void setValuesAsList(List<Pair<T, Double>> values) {
        counts.clear();
        for (Pair<T, Double> value : values) {
            counts.put(value.getFirst(), value.getSecond());
        }
    }

    public interface ItemMapping<S, T> {
        Pair<T, Double> mapItem(S item);
    }
}
