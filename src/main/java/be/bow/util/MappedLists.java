package be.bow.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MappedLists<S, T> extends HashMap<S, List<T>> {

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
