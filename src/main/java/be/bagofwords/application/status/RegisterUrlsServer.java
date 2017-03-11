package be.bagofwords.application.status;

import be.bagofwords.application.SocketRequestHandler;
import be.bagofwords.application.SocketRequestHandlerFactory;
import be.bagofwords.application.SocketServer;
import be.bagofwords.minidepi.ApplicationContext;
import be.bagofwords.ui.UI;
import be.bagofwords.util.BufferedSocketConnection;
import be.bagofwords.util.SocketConnection;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by Koen Deschacht (koendeschacht@gmail.com) on 07/10/14.
 */
public class RegisterUrlsServer implements SocketRequestHandlerFactory {

    public static byte SEND_URL = 1;

    private ListUrlsController listUrlsController;

    public RegisterUrlsServer(ListUrlsController listUrlsController, ApplicationContext context) {
        this.listUrlsController = listUrlsController;
        SocketServer socketServer = context.getBean(SocketServer.class);
        socketServer.registerSocketRequestHandlerFactory(this);
    }

    @Override
    public String getName() {
        return "RegisterUrlServer";
    }

    @Override
    public SocketRequestHandler createSocketRequestHandler(Socket socket) throws IOException {
        SocketConnection connection = new BufferedSocketConnection(socket);
        return new SocketRequestHandler("register_url_handler", connection) {
            @Override
            public void reportUnexpectedError(Exception ex) {
                UI.writeError("Unexpected error in RegisterPathServer", ex);
            }

            @Override
            public void handleRequests() throws Exception {
                byte action = connection.readByte();
                if (action == SEND_URL) {
                    String name = connection.readString();
                    String url = connection.readString();
                    listUrlsController.registerUrl(name, url);
                    connection.writeLong(SocketServer.LONG_OK);
                } else {
                    connection.writeLong(SocketServer.LONG_ERROR);
                }
                connection.flush();
            }

        };
    }

}
