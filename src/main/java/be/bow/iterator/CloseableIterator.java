package be.bow.iterator;

import be.bow.ui.UI;
import be.bow.util.Utils;

import java.io.Closeable;
import java.util.Iterator;

public abstract class CloseableIterator<T extends Object> implements Iterator<T>, Closeable {

    private boolean wasClosed = false;
    private final String stackTrace;

    public CloseableIterator() {
        stackTrace = Utils.getStackTrace(new RuntimeException());
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
            UI.writeError("CloseableIterator was not closed!");
            close();
        }
    }

}
