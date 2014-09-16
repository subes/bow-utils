package be.bow.cache;


import be.bow.util.KeyValue;

import java.util.List;

public interface CacheableData<T> {

    void removedValues(int ind, List<KeyValue<T>> valuesToRemove);

    String getName();

    Class<? extends T> getObjectClass();

    CacheImportance getImportance();
}
