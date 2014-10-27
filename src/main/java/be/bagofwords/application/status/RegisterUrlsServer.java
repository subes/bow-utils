package be.bagofwords.application.status;

import be.bagofwords.application.BaseServer;
import be.bagofwords.ui.UI;
import be.bagofwords.util.WrappedSocketConnection;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by Koen Deschacht (koendeschacht@gmail.com) on 07/10/14.
 */
public class RegisterUrlsServer extends BaseServer {

    public static byte SEND_URL = 1;

    private ListUrlsController listUrlsController;

    public RegisterUrlsServer(int port, ListUrlsController listUrlsController) {
        super("RegisterUrlServer", port);
        this.listUrlsController = listUrlsController;
    }

    @Override
    protected SocketRequestHandler createSocketRequestHandler(Socket socket) throws IOException {
        WrappedSocketConnection connection = new WrappedSocketConnection(socket);
        return new SocketRequestHandler(connection) {
            @Override
            protected void reportUnexpectedError(Exception ex) {
                UI.writeError("Unexpected error in RegisterPathServer", ex);
            }

            @Override
            protected void handleRequests() throws Exception {
                byte action = connection.readByte();
                if (action == SEND_URL) {
                    String name = connection.readString();
                    String url = connection.readString();
                    listUrlsController.registerUrl(name, url);
                    connection.writeLong(LONG_OK);
                } else {
                    connection.writeLong(LONG_ERROR);
                }
                connection.flush();
            }

            @Override
            public long getTotalNumberOfRequests() {
                return 1;
            }
        };
    }

}
