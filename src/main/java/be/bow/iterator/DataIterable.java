package be.bow.iterator;

public interface DataIterable<T extends Object> {

    CloseableIterator<T> iterator();

    long apprSize();

}
