package be.bagofwords.iterator;

import be.bagofwords.ui.UI;

import java.util.Iterator;

public class MultipleSwipesIterator<T extends Object> implements Iterator<T> {

    private final DataIterable<T> iterable;
    private Iterator<T> currIt;
    private T currObj;
    private final int numOfSwipes;
    private int currentSwipe;

    public MultipleSwipesIterator(DataIterable<T> iterable, int numOfSwipes) {
        this.iterable = iterable;
        this.numOfSwipes = numOfSwipes;
        currentSwipe = 0;
        currIt = iterable.iterator();
        currObj = findNext();
    }

    private T findNext() {
        while (currentSwipe < numOfSwipes) {
            if (currIt.hasNext()) {
                return currIt.next();
            } else {
                UI.write("Finished swipe " + currentSwipe + " of " + numOfSwipes);
                currentSwipe++;
                if (currentSwipe < numOfSwipes) {
                    currIt = iterable.iterator();
                }
            }
        }
        return null;
    }

    public int getCurrentSwipe() {
        return currentSwipe;
    }

    @Override
    public boolean hasNext() {
        return currObj != null;
    }

    @Override
    public T next() {
        T result = currObj;
        currObj = findNext();
        return result;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
