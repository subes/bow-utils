package be.bagofwords.application;


import be.bagofwords.ui.UI;
import be.bagofwords.util.SafeThread;
import be.bagofwords.util.Utils;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
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





}
