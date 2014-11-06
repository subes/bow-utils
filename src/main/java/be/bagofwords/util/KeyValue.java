package be.bagofwords.util;

import java.io.Serializable;

public class KeyValue<T extends Object> implements Comparable<KeyValue<T>>, Serializable {

    private long key;
    private T value;

    public KeyValue(long key, T value) {
        this.key = key;
        this.value = value;
    }

    public long getKey() {
        return key;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public int compareTo(KeyValue<T> o) {
        return Long.compare(getKey(), o.getKey());
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof KeyValue) {
            return getKey() == ((KeyValue) o).getKey();
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return Long.toString(key);
    }

    //Serialization

    public KeyValue() {
    }

}
