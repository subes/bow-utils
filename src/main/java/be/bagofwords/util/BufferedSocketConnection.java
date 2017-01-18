package be.bagofwords.util;

import java.io.*;
import java.net.Socket;

/**
 * Created by koen on 01.11.16.
 */
public class BufferedSocketConnection extends SocketConnection {
    public BufferedSocketConnection(String host, int port) throws IOException {
        this(host, port, false, false);
    }

    public BufferedSocketConnection(String host, int port, boolean useLargeOutputBuffer, boolean useLargeInputBuffer) throws IOException {
        this(new Socket(host, port), useLargeOutputBuffer, useLargeInputBuffer);
    }

    public BufferedSocketConnection(Socket socket) throws IOException {
        this(socket, false, false);
    }

    public BufferedSocketConnection(Socket socket, boolean useLargeOutputBuffer, boolean useLargeInputBuffer) throws IOException {
        super(socket, new DataInputStream(new BufferedInputStream(socket.getInputStream(), useLargeInputBuffer ? 1024 * 1024 : 32 * 1024)), new DataOutputStream(new BufferedOutputStream(socket.getOutputStream(), useLargeOutputBuffer ? 1024 * 1024 : 32 * 1024)));
    }
}
