package be.bagofwords.application;

import be.bagofwords.util.SafeThread;
import be.bagofwords.util.SocketConnection;
import org.apache.commons.io.IOUtils;

/**
 * Created by koen on 01.11.16.
 */
public abstract class SocketRequestHandler extends SafeThread {

    protected SocketConnection connection;
    private SocketServer socketServer;
    private long startTime;

    public SocketRequestHandler(String name, SocketConnection connection) {
        super(name, true);
        this.connection = connection;
    }

    public void setSocketServer(SocketServer socketServer) {
        this.socketServer = socketServer;
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

}
