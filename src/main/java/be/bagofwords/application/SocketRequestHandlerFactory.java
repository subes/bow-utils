package be.bagofwords.application;

import be.bagofwords.util.SocketConnection;

import java.io.IOException;

/**
 * Created by koen on 01.11.16.
 */
public interface SocketRequestHandlerFactory {

    String getName();

    SocketRequestHandler createSocketRequestHandler(SocketConnection socketConnection) throws IOException;

}
