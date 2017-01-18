package be.bagofwords.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by koen on 01.11.16.
 */
public class UnbufferedSocketConnection extends SocketConnection {

    public UnbufferedSocketConnection(String host, int port) throws IOException {
        this(new Socket(host, port));
    }

    public UnbufferedSocketConnection(Socket socket) throws IOException {
        super(socket, new DataInputStream(socket.getInputStream()), new DataOutputStream(socket.getOutputStream()));
    }
}
