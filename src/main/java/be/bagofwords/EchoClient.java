package be.bagofwords;

import net.nutss.Control;
import net.nutss.stunt.STUNTEndpoint;
import net.nutss.stunt.STUNTEventAdapter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.URI;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Semaphore;

class EchoClient {

    static Semaphore semaphore = new Semaphore(0);
    static Socket socket;
    static URI uri;

    static class Handler extends STUNTEventAdapter {

        public void connectHandler(SocketChannel sock) {
            socket = sock.socket();
            semaphore.release();
        }

        public void errorHandler(Exception e) {
            System.err.println("An error occurred. If this is unexpected, please run with '-d'\n" +
                    "as the first argument and email the log created in EchoClientLog.txt\n" +
                    "to Saikat Guha <saikat@cs.cornell.edu>. Thanks\n");
            e.printStackTrace();
            System.exit(1);
        }

    }

    public static void main(String[] arg) throws Exception {
        try {
            uri = new URI(arg[0].equals("-d") ? arg[1] : arg[0]);
        } catch (Exception e) {
            System.out.printf("Usage:\n\tjava EchoClient [-d] you@your.domain.com\n");
            System.exit(1);
        }
        try {
            if (arg[0].equals("-d")) Control.logToFile("EchoClientLog.txt");
            STUNTEndpoint sock = new STUNTEndpoint();
            sock.connect(uri, new Handler());
            semaphore.acquire();
            BufferedReader uin = new BufferedReader(new InputStreamReader(System.in));
            BufferedReader sin = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintStream uout = System.out;
            PrintStream sout = new PrintStream(socket.getOutputStream());
            while (true) {
                String s;
                if ((s = sin.readLine()) == null) break;
                uout.println(s);
                if ((s = uin.readLine()) == null) break;
                sout.println(s);
            }
        } catch (Exception e) {
            System.err.println("An error occurred. If this is unexpected, please run with '-d'\n" +
                    "as the first argument and email the log created in EchoServerLog.txt\n" +
                    "to Saikat Guha <saikat@cs.cornell.edu>. Thanks\n");
            e.printStackTrace();
            System.exit(1);
        }
    }

}
