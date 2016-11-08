package be.bagofwords.application;

/**
 * Created by koen on 01.11.16.
 */
public interface SocketRequestHandler {

    void handleRequests() throws Exception;

    void reportUnexpectedError(Exception ex);

}
