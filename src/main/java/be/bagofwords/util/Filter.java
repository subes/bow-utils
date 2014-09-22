package be.bagofwords.util;

public interface Filter<T> {

    boolean accept(T obj);

}
