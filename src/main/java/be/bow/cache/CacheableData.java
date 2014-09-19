package be.bow.cache;


import be.bow.util.KeyValue;

import java.util.List;

public interface CacheableData<T> {

    void removedValuesFromCache(Cache cache, List<KeyValue<T>> valuesToRemove);

}
