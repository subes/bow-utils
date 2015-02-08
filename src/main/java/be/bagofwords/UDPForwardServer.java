package be.bagofwords;

import be.bagofwords.util.SerializationUtils;
import org.springframework.util.DigestUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Koen Deschacht (koendeschacht@gmail.com) on 01/02/15.
 */
public class UDPForwardServer {

    public static void main(String[] args) throws SocketException {
        new UDPForwardServer().run();

    }

    private Map<Address, Server> servers = new HashMap<>();
    private Map<String, Address> routeToAddress = new HashMap<>();
    private Map<Address, String> addressToRoute = new HashMap<>();


    public void run() throws SocketException {
        DatagramSocket socket = new DatagramSocket(443);

        new Thread(this::cleanUpOldServers).start();

        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                while (true) {
                    byte[] buffer = new byte[1500];
                    DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
                    try {
                        socket.receive(receivePacket);
                        if (receivePacket.getLength() != buffer.length) {
                            buffer = Arrays.copyOf(buffer, receivePacket.getLength());
                        }
                        Address clientAddress = new Address(receivePacket.getAddress(), receivePacket.getPort());
                        String message = new String(buffer, "UTF-8");
                        if (message.contains("udp_forward_action")) {
                            UdpForwardAction action = SerializationUtils.bytesToObject(buffer, UdpForwardAction.class);
                            if (action.udp_forward_action.equals("register_server")) {
                                Server server = servers.get(clientAddress);
                                if (server != null) {
                                    server.seen();
                                } else {
                                    servers.put(clientAddress, new Server(clientAddress));
                                }
                                UdpForwardAction actionToSend = new UdpForwardAction();
                                actionToSend.udp_forward_action = "register_success";
                                actionToSend.remote_port = clientAddress.port;
                                actionToSend.remote_ip = clientAddress.address.getHostAddress();
                                byte[] bufferToSend = SerializationUtils.objectToBytes(actionToSend, UdpForwardAction.class);
                                socket.send(new DatagramPacket(bufferToSend, bufferToSend.length, clientAddress.address, clientAddress.port));
                                System.out.println("Registered server " + clientAddress);
                            } else if (action.udp_forward_action.equals("register_forwarder")) {
                                String sender = action.sender_ip + ":" + action.sender_port;
                                String receiver = action.receiver_ip + ":" + action.receiver_port;
                                String route = sender + ">" + receiver;
                                addressToRoute.put(clientAddress, route);
                                routeToAddress.put(route, clientAddress);
                                System.out.println("Registered route " + route + " for client " + clientAddress);

                                String reverseRoute = receiver + ">" + sender;
                                if (!routeToAddress.containsKey(reverseRoute)) {
                                    Address receiverAddress = new Address(InetAddress.getByName(action.receiver_ip), action.receiver_port);
                                    if (servers.containsKey(receiverAddress)) {
                                        System.out.println("Sending packet to " + receiverAddress + " to set up reverse route " + reverseRoute);
                                        UdpForwardAction actionToSend = new UdpForwardAction();
                                        actionToSend.udp_forward_action = "add_route";
                                        actionToSend.sender_ip = action.sender_ip;
                                        actionToSend.sender_port = action.sender_port;
                                        byte[] bufferToSend = SerializationUtils.objectToBytes(actionToSend, UdpForwardAction.class);
                                        socket.send(new DatagramPacket(bufferToSend, bufferToSend.length, receiverAddress.address, receiverAddress.port));
                                    } else {
                                        System.err.println("Could not set up reverse route " + reverseRoute + ", your connection will fail!");
                                    }
                                }
                            } else {
                                System.err.println("Unexpected action " + action.udp_forward_action);
                            }
                        } else {
                            //raw packet
                            String route = addressToRoute.get(clientAddress);
                            if (route != null) {
                                String[] routeParts = route.split(">");
                                String reverseRoute = routeParts[1] + ">" + routeParts[0];
                                Address receiverAddress = routeToAddress.get(reverseRoute);
                                if (receiverAddress != null) {
                                    socket.send(new DatagramPacket(buffer, buffer.length, receiverAddress.address, receiverAddress.port));
                                    //System.out.println("Forwarding packet " + DigestUtils.md5DigestAsHex(buffer) + " on route " + route);
                                } else {
                                    System.err.println("Failed to find address for reverse route " + reverseRoute + ", dropping packet");
                                }
                            } else {
                                System.err.println("Failed to find route for address " + clientAddress + ", dropping packet");
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

    }

    public void cleanUpOldServers() {
        //TODO
    }

    public static class Address {
        private InetAddress address;
        private int port;

        public Address(InetAddress address, int port) {
            this.address = address;
            this.port = port;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Address address1 = (Address) o;

            if (port != address1.port) return false;
            if (!address.equals(address1.address)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = address.hashCode();
            result = 31 * result + port;
            return result;
        }

        @Override
        public String toString() {
            return address.getHostAddress() + ":" + port;
        }
    }

    public static class UdpForwardAction {
        public String udp_forward_action;
        public String remote_ip;
        public int remote_port;
        public String sender_ip;
        public int sender_port;
        public String receiver_ip;
        public int receiver_port;
    }

    public static class Server {
        private Address address;
        private long lastSeen;

        public Server(Address address) {
            this.address = address;
            this.lastSeen = System.currentTimeMillis();
        }

        public void seen() {
            this.lastSeen = System.currentTimeMillis();
        }

        public Address getAddress() {
            return address;
        }

        public long getLastSeen() {
            return lastSeen;
        }
    }

}
