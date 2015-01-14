package be.bagofwords;

import be.bagofwords.util.WrappedSocketConnection;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by Koen Deschacht (koendeschacht@gmail.com) on 14/01/15.
 */
public class KoenStunClient {

    public static void main(String[] args) throws IOException {
        Socket clientSocket = new Socket(args[0], Integer.parseInt(args[1]));
        WrappedSocketConnection wrappedSocketConnection = new WrappedSocketConnection(clientSocket);
        System.out.println("My remote address is " + wrappedSocketConnection.readString() + " and port is " + wrappedSocketConnection.readInt());
        clientSocket.close();
    }

}
