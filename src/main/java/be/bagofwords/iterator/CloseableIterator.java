package be.bagofwords.iterator;

import be.bagofwords.ui.UI;

import java.io.Closeable;
import java.util.Iterator;

public abstract class CloseableIterator<T extends Object> implements Iterator<T>, Closeable {

    private boolean wasClosed = false;
    private final String creatingMethod;

    public CloseableIterator() {
        StackTraceElement callingMethod = Thread.currentThread().getStackTrace()[2];
        creatingMethod = callingMethod.getFileName() + ":" + callingMethod.getLineNumber();
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
            UI.writeError("CloseableIterator was not closed! Was created in " + creatingMethod);
            close();
        }
    }

}
