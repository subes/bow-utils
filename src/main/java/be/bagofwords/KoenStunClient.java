package be.bagofwords;

import be.bagofwords.ui.UI;
import be.bagofwords.util.WrappedSocketConnection;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Random;

/**
 * Created by Koen Deschacht (koendeschacht@gmail.com) on 14/01/15.
 */
public class KoenStunClient {

    private static final int my_local_port = 9055;

    public static void main(String[] args) throws IOException {
        Socket stunSocket = new Socket(args[0], Integer.parseInt(args[1]), null, my_local_port);
        WrappedSocketConnection wrappedStunSocket = new WrappedSocketConnection(stunSocket);
        String myRemoteAddress = wrappedStunSocket.readString();
        int myRemotePort = wrappedStunSocket.readInt();
        UI.write("My remote address is " + myRemoteAddress + " and port is " + myRemotePort);
        wrappedStunSocket.writeBoolean(true);
        wrappedStunSocket.flush();
        wrappedStunSocket.close();

        String otherRemoteAddress = UI.read("Please give the address of the other client");
        int otherRemotePort = UI.readInt("Please give the port of the other client");
        InetAddress otherAddress = InetAddress.getByName(otherRemoteAddress);

        Random random = new Random();
        byte[] myData = new byte[]{(byte) random.nextInt(127)};
        byte[] receivedData = new byte[1];

        UI.write("Will try to send my data to the other client. Hold on your bretels!");
        int numOfSuccesfulReceived = 0;
        DatagramSocket outgoingSocket = new DatagramSocket(my_local_port);
        while (true) {
            UI.write("Sending " + myData[0] + " to " + otherRemoteAddress + ":" + otherRemotePort);
            outgoingSocket.send(new DatagramPacket(myData, myData.length, otherAddress, otherRemotePort));
            int timeout = 500 + random.nextInt(1000);
            outgoingSocket.setSoTimeout(timeout);
            UI.write("Trying to receive data from " + otherRemoteAddress + ":" + otherRemotePort + " timout=" + timeout);
            try {
                DatagramPacket receivingPacket = new DatagramPacket(receivedData, receivedData.length);
                outgoingSocket.receive(receivingPacket);
                byte[] actualReceivedData = receivingPacket.getData();
                if (actualReceivedData.length != 1) {
                    UI.write("Received " + actualReceivedData.length + " bytes?");
                } else {
                    numOfSuccesfulReceived++;
                    UI.write("Received data from other client! " + actualReceivedData[0] + "(already " + numOfSuccesfulReceived + " times)");
                }
            } catch (Exception e) {
                UI.writeError("Received error while trying to receive data", e);
            }
        }
    }

}
