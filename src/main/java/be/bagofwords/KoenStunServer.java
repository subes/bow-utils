package be.bagofwords;

import be.bagofwords.ui.UI;
import be.bagofwords.util.WrappedSocketConnection;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Koen Deschacht (koendeschacht@gmail.com) on 14/01/15.
 */
public class KoenStunServer {

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(Integer.parseInt(args[0]));
        while (true) {
            Socket clientSocket = serverSocket.accept();
            WrappedSocketConnection wrappedSocketConnection = new WrappedSocketConnection(clientSocket);
            InetAddress inetAddress = clientSocket.getInetAddress();
            UI.write("Client address=" + inetAddress.getHostAddress() + " port=" + clientSocket.getPort() + " local_port=" + clientSocket.getLocalPort());
            wrappedSocketConnection.writeString(inetAddress.getHostAddress());
            wrappedSocketConnection.writeInt(clientSocket.getPort());
            wrappedSocketConnection.flush();
            wrappedSocketConnection.readBoolean();
            wrappedSocketConnection.close();
        }
    }

}
