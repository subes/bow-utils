package be.bow.counts;

import be.bow.ui.UI;

import java.util.*;

public class DoubleCounter<T extends Object> {

    private final HashMap<T, Double> counts;

    public DoubleCounter() {
        counts = new HashMap<>();
    }

    public void inc(T s) {
        inc(s, 1.0);
    }

    public void inc(T s, double value) {
        if (counts.containsKey(s))
            counts.put(s, counts.get(s) + value);
        else
            counts.put(s, value);
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

    public String[] keySet() {
        Set<T> var = counts.keySet();
        return var.toArray(new String[var.size()]);
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

    public double getTotal() {
        int total = 0;
        for (Double val : counts.values()) {
            total += val;
        }
        return total;
    }

    public double getMax() {
        double max = 0;
        for (Double val : counts.values()) {
            max = Math.max(max, val);
        }
        return max;
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

    private Set<Map.Entry<T, Double>> entrySet() {
        return counts.entrySet();
    }
}
