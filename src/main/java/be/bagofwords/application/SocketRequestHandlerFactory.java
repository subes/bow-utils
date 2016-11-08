package be.bagofwords.application;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by koen on 01.11.16.
 */
public interface SocketRequestHandlerFactory {

    public String getName();
    public SocketRequestHandler createSocketRequestHandler(Socket socket) throws IOException;

}
