package be.bagofwords.iterator;

public interface DataIterable<T extends Object> extends Iterable<T> {

    CloseableIterator<T> iterator();

    long apprSize();

}
