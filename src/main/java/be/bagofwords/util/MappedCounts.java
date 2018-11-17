package be.bagofwords.util;

import be.bagofwords.counts.Counter;

import java.util.HashMap;
import java.util.Map;

public class MappedCounts<S, T> extends HashMap<S, Counter<T>> {

    public MappedCounts() {
    }

    public MappedCounts(MappedCounts<? extends S, ? extends T> m) {
        super((Map) m);
    }

    @Override
    public Counter<T> get(Object key) {
        Counter<T> result = super.get(key);
        if (result == null) {
            result = new Counter<>();
            put((S) key, result);
        }
        return result;
    }

    public Map<S, Map<T, Long>> toMaps() {
        Map<S, Map<T, Long>> result = new HashMap<>();
        for (Map.Entry<S, Counter<T>> entry : this.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getMap());
        }
        return result;
    }
}
