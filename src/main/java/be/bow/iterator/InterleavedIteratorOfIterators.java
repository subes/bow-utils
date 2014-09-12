
package be.bow.iterator;

import java.util.List;

public class InterleavedIteratorOfIterators<U extends Object> extends CloseableIterator<U> {

    private final List<CloseableIterator<? extends U>> iterators;
    private int currPos;
    private U next;

    public InterleavedIteratorOfIterators(List<CloseableIterator<? extends U>> iterators) {
        this.iterators = iterators;
        this.currPos = 0;
        next = findNext();
    }

    private U findNext() {
        U result = null;
        for (int i = 0; i < iterators.size(); i++) {
            if (iterators.get(currPos).hasNext()) {
                result = iterators.get(currPos).next();
                break;
            } else {
                currPos = (currPos + 1) % iterators.size();
            }
        }
        currPos = (currPos + 1) % iterators.size();
        return result;
    }

    @Override
    public boolean hasNext() {
        return next != null;
    }

    @Override
    public U next() {
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
