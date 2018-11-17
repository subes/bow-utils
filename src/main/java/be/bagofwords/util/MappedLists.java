package be.bagofwords.util;

import be.bagofwords.iterator.IterableUtils;

import java.util.*;

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

    public static <S, T> MappedLists<S, T> create(Collection<T> items, IterableUtils.MappingFunction<T, S> mappingFunction) {
        MappedLists<S, T> result = new MappedLists<>();
        for (T item : items) {
            result.get(mappingFunction.map(item)).add(item);
        }
        return result;
    }
}
