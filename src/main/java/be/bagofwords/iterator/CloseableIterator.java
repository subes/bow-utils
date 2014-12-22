package be.bagofwords.iterator;

import be.bagofwords.ui.UI;
import be.bagofwords.util.Utils;

import java.io.Closeable;
import java.util.Iterator;

public abstract class CloseableIterator<T extends Object> implements Iterator<T>, Closeable {

    private boolean wasClosed = false;
    private final StackTraceElement[] creatingStackTrace;

    public CloseableIterator() {
        creatingStackTrace = Thread.currentThread().getStackTrace(); //we want to keep track what methods do not close the iterator
    }

    @Override
    public void close() {
        if (!wasClosed) {
            closeInt();
            wasClosed = true;
        }
    }

    public boolean wasClosed() {
        return wasClosed;
    }

    protected abstract void closeInt();

    public void remove() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (!wasClosed) {
            UI.writeError("CloseableIterator was not closed! Was created in " + Utils.getStackTrace(creatingStackTrace));
            close();
        }
    }

}
