package be.bagofwords.iterator;

public abstract class SimpleIterator<T> {

    public abstract T next() throws Exception;

    public void close() throws Exception {
        //Do nothing in default implementation
    }

}
