package be.bow.iterator;

import java.util.List;

public class SequentialIteratorOfIterators<U extends Object> extends CloseableIterator<U> {

    private final List<CloseableIterator<? extends U>> iterators;
    private int currPos;
    private U next;

    public SequentialIteratorOfIterators(List<CloseableIterator<? extends U>> iterators) {
        this.iterators = iterators;
        this.currPos = 0;
        next = findNext();
    }

    private U findNext() {
        while (currPos < iterators.size()) {
            if (iterators.get(currPos).hasNext()) {
                return iterators.get(currPos).next();
            } else {
                currPos++;
            }
        }
        return null;
    }

    @Override
    public boolean hasNext() {
        return next != null;
    }

    @Override
    public synchronized U next() {
        U result = next;
        next = findNext();
        return result;
    }

    @Override
    public void closeInt() {
        for (CloseableIterator iterator : iterators) {
            iterator.close();
        }
    }

}
