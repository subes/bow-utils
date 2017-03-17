package be.bagofwords.application;

import be.bagofwords.minidepi.annotations.Inject;
import be.bagofwords.ui.UI;
import be.bagofwords.util.SafeThread;
import be.bagofwords.util.SocketConnection;
import org.apache.commons.io.IOUtils;

import java.io.IOException;

/**
 * Created by koen on 01.11.16.
 */
public abstract class SocketRequestHandler extends SafeThread {

    protected SocketConnection connection;
    private long startTime;

    @Inject
    private SocketServer socketServer;

    public SocketRequestHandler(String name, SocketConnection connection) {
        super(name, true);
        this.connection = connection;
    }

    public long getStartTime() {
        return startTime;
    }

    @Override
    protected void runInt() throws Exception {
        try {
            startTime = System.currentTimeMillis();
            handleRequests();
        } catch (Exception ex) {
            if (isUnexpectedError(ex)) {
                reportUnexpectedError(ex);
            }
        }
        IOUtils.closeQuietly(connection);
        socketServer.removeHandler(this);
    }

    protected boolean isUnexpectedError(Exception ex) {
        if (ex.getMessage() != null && ex.getMessage().contains("Connection reset")) {
            return false;
        }
        for (StackTraceElement el : ex.getStackTrace()) {
            if (el.getMethodName().equals("readNextAction")) {
                return false;
            }
        }
        return true;
    }

    public abstract void handleRequests() throws Exception;

    public abstract void reportUnexpectedError(Exception ex);

    public long getTotalNumberOfRequests() {
        return -1; //Should be overridden in subclasses
    }

    @Override
    protected void doTerminate() {
        try {
            connection.close();
        } catch (IOException e) {
            UI.writeError("Failed to close connection in handler " + getName(), e);
        }
    }
}
