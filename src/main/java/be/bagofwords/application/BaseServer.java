package be.bagofwords.application;


import be.bagofwords.ui.UI;
import be.bagofwords.util.SafeThread;
import be.bagofwords.util.WrappedSocketConnection;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseServer extends SafeThread {

    public static final String ENCODING = "UTF-8";
    public static final long LONG_ERROR = Long.MAX_VALUE;
    public static final long LONG_OK = Long.MAX_VALUE - 1;
    public static final long LONG_END = Long.MAX_VALUE - 2;
    public static final long LONG_NULL = Long.MAX_VALUE - 3;
    public static final double DOUBLE_NULL = Double.MAX_VALUE;
    public static final int INT_NULL = Integer.MAX_VALUE;
    public static final float FLOAT_NULL = Float.MAX_VALUE;

    private ServerSocket serverSocket;
    private final List<SocketRequestHandler> runningRequestHandlers;
    private final int scpPort;
    private int totalNumberOfConnections;

    public BaseServer(String name, int port) {
        super(name, false);
        this.runningRequestHandlers = new ArrayList<>();
        this.scpPort = port;
        this.totalNumberOfConnections = 0;
        try {
            this.serverSocket = new ServerSocket(scpPort);
            UI.write("Started server " + getName() + " on port " + scpPort);
        } catch (IOException exp) {
            throw new RuntimeException("Failed to initialize server " + getName() + " on port " + scpPort, exp);
        }
    }

    protected abstract SocketRequestHandler createSocketRequestHandler(WrappedSocketConnection wrappedSocketConnection) throws IOException;

    @Override
    protected void runInt() throws Exception {
        while (!serverSocket.isClosed() && !isTerminateRequested()) {
            try {
                WrappedSocketConnection connection = new WrappedSocketConnection(serverSocket.accept());
                SocketRequestHandler handler = createSocketRequestHandler(connection);
                if (handler != null) {
                    synchronized (runningRequestHandlers) {
                        runningRequestHandlers.add(handler);
                    }
                    handler.start();
                }
                totalNumberOfConnections++;
            } catch (IOException e) {
                if (!(e instanceof SocketException || isTerminateRequested())) {
                    UI.writeError(e);
                }
            }
        }
    }

    @Override
    public void doTerminate() {
        synchronized (runningRequestHandlers) {
            for (SocketRequestHandler requestHandler : runningRequestHandlers) {
                requestHandler.close();
            }
        }
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        UI.write("Server " + getName() + " has been terminated.");
    }

    public int getTotalNumberOfConnections() {
        return totalNumberOfConnections;
    }

    public List<SocketRequestHandler> getRunningRequestHandlers() {
        return runningRequestHandlers;
    }

    public abstract class SocketRequestHandler extends SafeThread {

        protected WrappedSocketConnection connection;

        public SocketRequestHandler(WrappedSocketConnection connection) throws IOException {
            super(BaseServer.this.getName() + "RequestHandler", false);
            this.connection = connection;
        }

        protected abstract void reportUnexpectedError(Exception ex);

        @Override
        protected void runInt() {
            try {
                handleRequests();
            } catch (Exception ex) {
                if (isUnexpectedError(ex)) {
                    reportUnexpectedError(ex);
                }
            }
            IOUtils.closeQuietly(connection);
            synchronized (runningRequestHandlers) {
                runningRequestHandlers.remove(this);
            }
        }

        protected abstract void handleRequests() throws Exception;

        @Override
        public void doTerminate() {
            IOUtils.closeQuietly(connection);
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


        public abstract long getTotalNumberOfRequests();
    }

}
