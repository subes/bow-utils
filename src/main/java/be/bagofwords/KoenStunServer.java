package be.bagofwords;

import be.bagofwords.ui.UI;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by Koen Deschacht (koendeschacht@gmail.com) on 14/01/15.
 */
public class KoenStunServer {

    public static void main(String[] args) throws IOException {
        DatagramSocket socket = new DatagramSocket(Integer.parseInt(args[0]));
        while (true) {
            try {
                byte[] stunData = new byte[100];
                DatagramPacket receivePacket = new DatagramPacket(stunData, stunData.length);
                socket.receive(receivePacket);
                InetAddress clientInetAddress = receivePacket.getAddress();
                String clientAddress = clientInetAddress.getHostAddress();
                int clientPort = receivePacket.getPort();
                UI.write("Client address=" + clientAddress + " port=" + clientPort);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(byteArrayOutputStream));
                writer.write(clientAddress);
                writer.write("\n");
                writer.write(Integer.toString(clientPort));
                writer.write("\n");
                writer.close();
                byte[] clientData = byteArrayOutputStream.toByteArray();
                DatagramPacket sendPacket = new DatagramPacket(clientData, 0, clientData.length, clientInetAddress, clientPort);
                socket.send(sendPacket);
            } catch (Exception exp) {
                UI.writeError("Received exception", exp);
            }
        }
    }

}
