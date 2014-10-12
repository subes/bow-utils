package be.bagofwords.application;


import be.bagofwords.ui.UI;
import be.bagofwords.util.SafeThread;
import be.bagofwords.util.Utils;
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
        } catch (IOException exp) {
            throw new RuntimeException("Failed to initialize server " + getName() + " on port " + scpPort, exp);
        }
    }

    protected abstract SocketRequestHandler createSocketRequestHandler(WrappedSocketConnection wrappedSocketConnection) throws IOException;

    @Override
    protected void runInt() throws Exception {
        UI.write("Started server " + getName() + " on port " + scpPort);
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
        IOUtils.closeQuietly(serverSocket);
        //once a request handler is finished, it removes itself from the list of requestHandlers, so we just wait until this list is empty
        while (!runningRequestHandlers.isEmpty()) {
            synchronized (runningRequestHandlers) {
                for (SocketRequestHandler requestHandler : runningRequestHandlers) {
                    if (!requestHandler.isTerminateRequested()) {
                        requestHandler.terminate(); //we can not call terminateAndWaitForFinish() here since to finish the request handler needs access to the runningRequestHandlers list
                    }
                }
            }
            Utils.threadSleep(10);
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
