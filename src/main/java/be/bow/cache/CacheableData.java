package be.bow.cache;


import be.bow.util.KeyValue;

import java.util.List;

public interface CacheableData<T> {

    void removedValues(int ind, List<KeyValue<T>> valuesToRemove);

    String getName();

    CacheImportance getImportance();
}
