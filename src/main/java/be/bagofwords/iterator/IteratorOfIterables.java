package be.bagofwords.iterator;

import org.apache.commons.lang3.mutable.MutableInt;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class IteratorOfIterables<T extends Object> implements Iterator<T> {

    private Iterator<Iterable<T>> intIterator;
    private Iterator<T> iterator;

    public IteratorOfIterables(Iterator<Iterable<T>> intIterator) {
        initialize(intIterator);
    }

    public IteratorOfIterables(final Iterable<T>... iterables) {
        this(Arrays.asList(iterables));
    }

    public IteratorOfIterables(final List<Iterable<T>> iterables) {
        final MutableInt ind = new MutableInt(0);
        initialize(new Iterator<Iterable<T>>() {
            @Override
            public boolean hasNext() {
                return ind.intValue() < iterables.size();
            }

            @Override
            public Iterable<T> next() {
                Iterable<T> next = iterables.get(ind.intValue());
                ind.increment();
                return next;
            }

            @Override
            public void remove() {
                throw new RuntimeException("Not possible with multiple iterables...");
            }
        });
    }

    private void initialize(Iterator<Iterable<T>> intIterator) {
        this.intIterator = intIterator;
        findNextIterator();
    }

    private void findNextIterator() {
        Iterator<T> nextIterator = null;
        while ((nextIterator == null || !nextIterator.hasNext()) && this.intIterator.hasNext()) {
            nextIterator = intIterator.next().iterator();
        }
        if (nextIterator != null && nextIterator.hasNext()) {
            this.iterator = nextIterator;
        } else {
            this.iterator = null;
        }
    }

    @Override
    public boolean hasNext() {
        return iterator != null && iterator.hasNext();
    }

    @Override
    public T next() {
        T nextVal = iterator.next();
        if (!iterator.hasNext()) {
            findNextIterator();
        }
        return nextVal;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
