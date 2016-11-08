package be.bagofwords.application;

import be.bagofwords.ui.UI;
import be.bagofwords.util.SafeThread;
import be.bagofwords.util.SocketConnection;
import be.bagofwords.util.UnbufferedSocketConnection;
import be.bagofwords.util.Utils;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by koen on 01.11.16.
 */
public class SocketServer extends SafeThread {

    public static final String ENCODING = "UTF-8";
    public static final long LONG_ERROR = Long.MAX_VALUE;
    public static final long LONG_OK = Long.MAX_VALUE - 1;
    public static final long LONG_END = Long.MAX_VALUE - 2;

    private ServerSocket serverSocket;
    private Map<String, SocketRequestHandlerFactory> socketRequestHandlerFactories;
    private final List<SocketRequestHandlerThread> runningRequestHandlers;
    private final int scpPort;
    private int totalNumberOfConnections;

    public SocketServer(int port) {
        super("socket_server", false);
        this.runningRequestHandlers = new ArrayList<>();
        this.scpPort = port;
        this.totalNumberOfConnections = 0;
        this.socketRequestHandlerFactories = new HashMap<>();
        try {
            this.serverSocket = new ServerSocket(scpPort);
        } catch (IOException exp) {
            throw new RuntimeException("Failed to initialize server " + getName() + " on port " + scpPort, exp);
        }
    }

    public synchronized void registerSocketRequestHandlerFactory(SocketRequestHandlerFactory factory) {
        if (socketRequestHandlerFactories.containsKey(factory.getName())) {
            throw new RuntimeException("A SocketRequestHandlerFactory was already registered with name " + factory.getName());
        }
        socketRequestHandlerFactories.put(factory.getName(), factory);
    }

    @Override
    protected void runInt() throws Exception {
        UI.write("Started server " + getName() + " on port " + scpPort);
        Utils.threadSleep(500); //Make sure socket has had time to bind successfully (this does not yet work very well))
        while (!serverSocket.isClosed() && !isTerminateRequested()) {
            try {
                Socket acceptedSocket = serverSocket.accept();
                SocketConnection connection = new UnbufferedSocketConnection(acceptedSocket);
                String factoryName = connection.readString();
                SocketRequestHandlerFactory factory = socketRequestHandlerFactories.get(factoryName);
                if (factory == null) {
                    connection.writeLong(SocketServer.LONG_ERROR);
                    connection.writeString("No SocketRequestHandlerFactory registered for name " + factoryName);
                    continue;
                }
                SocketRequestHandler handler = factory.createSocketRequestHandler(acceptedSocket);
                SocketRequestHandlerThread thread = new SocketRequestHandlerThread(factoryName, handler, acceptedSocket);
                if (handler != null) {
                    synchronized (runningRequestHandlers) {
                        runningRequestHandlers.add(thread);
                    }
                    thread.start();
                    totalNumberOfConnections++;
                } else {
                    UI.writeWarning("Factory " + factoryName + " failed to create a socket handler. Closing socket...");
                    acceptedSocket.close();
                }
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
                for (SocketRequestHandlerThread requestHandler : runningRequestHandlers) {
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

    public List<SocketRequestHandlerThread> getRunningRequestHandlers() {
        return runningRequestHandlers;
    }


    public class SocketRequestHandlerThread extends SafeThread {

        private final SocketRequestHandler socketRequestHandler;
        private Socket socket;

        public SocketRequestHandlerThread(String factoryName, SocketRequestHandler socketRequestHandler, Socket socket) {
            super(factoryName + "_request_handler", true);
            this.socketRequestHandler = socketRequestHandler;
            this.socket = socket;
        }

        @Override
        protected void runInt() throws Exception {
            try {
                socketRequestHandler.handleRequests();
            } catch (Exception ex) {
                if (isUnexpectedError(ex)) {
                    socketRequestHandler.reportUnexpectedError(ex);
                }
            }
            IOUtils.closeQuietly(socket);
            synchronized (runningRequestHandlers) {
                runningRequestHandlers.remove(this);
            }
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


    }

}
