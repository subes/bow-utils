package be.bagofwords;

import be.bagofwords.ui.UI;
import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;

/**
 * Created by Koen Deschacht (koendeschacht@gmail.com) on 14/01/15.
 */
public class KoenStunClient {

    private static final int my_local_port = 9055;

    public static void main(String[] args) throws IOException {
        DatagramSocket socket = new DatagramSocket(my_local_port);
        Random random = new Random();
        byte[] myData = new byte[]{(byte) random.nextInt(127)};

        UI.write("Sending UDP packet to stun server");
        StunInfo stunInfo = getStunInfo(socket, args[0], Integer.parseInt(args[1]));
        String myRemoteAddress = stunInfo.getAddress();
        int myRemotePort = stunInfo.getPort();
        UI.write("My remote address is " + myRemoteAddress + " and port is " + myRemotePort);

        String otherRemoteAddress = UI.read("Please give the address of the other client");
        int otherRemotePort = UI.readInt("Please give the port of the other client");
        InetAddress otherAddress = InetAddress.getByName(otherRemoteAddress);

        byte[] receivedData = new byte[1];

        UI.write("Will try to send my data to the other client. Hold on your bretels!");
        int numOfSuccessfullyReceived = 0;

        while (true) {
            UI.write("Sending " + myData[0] + " to " + otherRemoteAddress + ":" + otherRemotePort);
            socket.send(new DatagramPacket(myData, myData.length, otherAddress, otherRemotePort));
            int timeout = 500 + random.nextInt(1000);
            socket.setSoTimeout(timeout);
            UI.write("Trying to receive data from " + otherRemoteAddress + ":" + otherRemotePort + " timout=" + timeout);
            try {
                DatagramPacket receivingPacket = new DatagramPacket(receivedData, receivedData.length);
                socket.receive(receivingPacket);
                byte[] actualReceivedData = receivingPacket.getData();
                if (actualReceivedData.length != 1) {
                    UI.write("Received " + actualReceivedData.length + " bytes?");
                } else {
                    numOfSuccessfullyReceived++;
                    UI.write("Received data from other client! " + actualReceivedData[0] + "(already " + numOfSuccessfullyReceived + " times)");
                }
            } catch (Exception e) {
                UI.writeError("Received error while trying to receive data", e);
            }
        }
    }

    private static StunInfo getStunInfo(DatagramSocket socket, String stunServerAddress, int stunServerPort) throws IOException {
        socket.setSoTimeout(1000);
        while (true) {
            byte[] myData = {1};
            socket.send(new DatagramPacket(myData, myData.length, InetAddress.getByName(stunServerAddress), stunServerPort));
            try {
                byte[] stunData = new byte[100];
                DatagramPacket receivePacket = new DatagramPacket(stunData, stunData.length);
                socket.receive(receivePacket);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(stunData)));
                String myRemoteAddress = bufferedReader.readLine();
                int myRemotePort = Integer.parseInt(bufferedReader.readLine());
                IOUtils.closeQuietly(bufferedReader);
                return new StunInfo(myRemoteAddress, myRemotePort);
            } catch (Exception exp) {
                UI.writeError("Received exception while trying to get data from stun server " + exp.getMessage());
            }
        }

    }

    private static class StunInfo {
        private String address;
        private int port;

        public StunInfo(String address, int port) {
            this.address = address;
            this.port = port;
        }

        public String getAddress() {
            return address;
        }

        public int getPort() {
            return port;
        }
    }

}
