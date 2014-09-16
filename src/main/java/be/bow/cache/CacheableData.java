package be.bow.cache;


import be.bow.util.KeyValue;

import java.util.List;

public interface CacheableData<T> {

    void removedValues(Cache cache, List<KeyValue<T>> valuesToRemove);

    Class<? extends T> getObjectClass();

}
