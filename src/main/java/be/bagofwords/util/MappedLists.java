package be.bagofwords.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MappedLists<S, T> extends HashMap<S, List<T>> {

    public MappedLists() {
    }

    public MappedLists(MappedLists<? extends S, ? extends T> m) {
        super((Map) m);
    }

    @Override
    public List<T> get(Object key) {
        List<T> result = super.get(key);
        if (result == null) {
            result = new ArrayList<>();
            put((S) key, result);
        }
        return result;
    }
}
