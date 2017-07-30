package be.bagofwords.util;

import java.util.HashMap;
import java.util.Map;

public class MappedMaps<S, T, U> extends HashMap<S, Map<T, U>> {

    public MappedMaps() {
    }

    public MappedMaps(MappedMaps<? extends S, ? extends T, ? extends U> m) {
        super((Map) m);
    }

    @Override
    public Map<T, U> get(Object key) {
        Map<T, U> result = super.get(key);
        if (result == null) {
            result = new HashMap<>();
            put((S) key, result);
        }
        return result;
    }
}
