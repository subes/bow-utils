package be.bagofwords.iterator;

public interface DataIterable<T extends Object> {

    CloseableIterator<T> iterator();

    long apprSize();

}
