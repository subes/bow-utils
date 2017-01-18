package be.bagofwords.application;

import be.bagofwords.application.status.StatusViewable;
import be.bagofwords.ui.UI;
import be.bagofwords.util.*;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;

/**
 * Created by koen on 01.11.16.
 */
public class SocketServer extends SafeThread implements StatusViewable {

    public static final String ENCODING = "UTF-8";
    public static final long LONG_ERROR = Long.MAX_VALUE;
    public static final long LONG_OK = Long.MAX_VALUE - 1;
    public static final long LONG_END = Long.MAX_VALUE - 2;

    private ServerSocket serverSocket;
    private Map<String, SocketRequestHandlerFactory> socketRequestHandlerFactories;
    private final List<SocketRequestHandler> runningRequestHandlers;
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
            this.serverSocket = null;
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
        while (!serverSocket.isClosed() && !isTerminateRequested()) {
            try {
                Socket acceptedSocket = serverSocket.accept();
                SocketConnection connection = new UnbufferedSocketConnection(acceptedSocket);
                String factoryName = connection.readString();
                if(factoryName==null || StringUtils.isEmpty(factoryName.trim())) {
                    connection.writeLong(SocketServer.LONG_ERROR);
                    connection.writeString("No name specified for the requested SocketRequestHandlerFactory");
                    continue;
                }
                SocketRequestHandlerFactory factory = socketRequestHandlerFactories.get(factoryName);
                if (factory == null) {
                    UI.writeWarning("No SocketRequestHandlerFactory registered for name " + factoryName);
                    connection.writeLong(SocketServer.LONG_ERROR);
                    connection.writeString("No SocketRequestHandlerFactory registered for name " + factoryName);
                    continue;
                }
                SocketRequestHandler handler = factory.createSocketRequestHandler(acceptedSocket);
                handler.setSocketServer(this);
                if (handler != null) {
                    synchronized (runningRequestHandlers) {
                        runningRequestHandlers.add(handler);
                    }
                    handler.start();
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


    public void removeHandler(SocketRequestHandler handler) {
        synchronized (runningRequestHandlers) {
            runningRequestHandlers.remove(handler);
        }
    }

    @Override
    public void printHtmlStatus(StringBuilder sb) {
        sb.append("<h1>Printing database server statistics</h1>");
        ln(sb, "<table>");
        ln(sb, "<tr><td>Used memory is </td><td>" + UI.getMemoryUsage() + "</td></tr>");
        ln(sb, "<tr><td>Total number of connections </td><td>" + getTotalNumberOfConnections() + "</td></tr>");
        List<SocketRequestHandler> runningRequestHandlers = getRunningRequestHandlers();
        ln(sb, "<tr><td>Current number of handlers </td><td>" + runningRequestHandlers.size() + "</td></tr>");
        List<SocketRequestHandler> sortedRequestHandlers;
        synchronized (runningRequestHandlers) {
            sortedRequestHandlers = new ArrayList<>(runningRequestHandlers);
        }
        Collections.sort(sortedRequestHandlers, (o1, o2) -> -Double.compare(o1.getTotalNumberOfRequests(), o2.getTotalNumberOfRequests()));
        for (int i = 0; i < sortedRequestHandlers.size(); i++) {
            SocketRequestHandler handler = sortedRequestHandlers.get(i);
            ln(sb, "<tr><td>" + i + " Name </td><td>" + handler.getName() + "</td></tr>");
            ln(sb, "<tr><td>" + i + " Started at </td><td>" + new Date(handler.getStartTime()) + "</td></tr>");
            ln(sb, "<tr><td>" + i + " Total number of requests </td><td>" + handler.getTotalNumberOfRequests() + "</td></tr>");
            double requestsPerSec = handler.getTotalNumberOfRequests() * 1000.0 / (System.currentTimeMillis() - handler.getStartTime());
            ln(sb, "<tr><td>" + i + " Average requests/s</td><td>" + NumUtils.fmt(requestsPerSec) + "</td></tr>");
        }
        ln(sb, "</table>");
    }

    private void ln(StringBuilder sb, String s) {
        sb.append(s);
        sb.append("\n");
    }
}
