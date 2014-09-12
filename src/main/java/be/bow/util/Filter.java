package be.bow.util;

public interface Filter<T> {

    boolean accept(T obj);

}
