package be.bagofwords.cache;


import be.bagofwords.util.KeyValue;

import java.util.List;

public interface CacheableData<T> {

    void removedValuesFromCache(Cache cache, List<KeyValue<T>> valuesToRemove);

}
