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
import java.util.LinkedList;
import java.util.concurrent.Semaphore;


class EchoServer {

    static Semaphore semaphore = new Semaphore(0);

    static LinkedList<Socket> sockets = new LinkedList<Socket>();
    static LinkedList<URI> uris = new LinkedList<URI>();
    static URI i;

    static class Handler extends STUNTEventAdapter {

        public void acceptHandler(SocketChannel sock, URI remoteAddress) {
            synchronized (semaphore) {
                sockets.addLast(sock.socket());
                uris.addLast(remoteAddress);
                semaphore.release();
            }
        }

        public void errorHandler(Exception e) {
            System.err.println("An error occurred. If this is unexpected, please run with '-d'\n" +
                    "as the first argument and email the log created in EchoServerLog.txt\n" +
                    "to Saikat Guha <saikat@cs.cornell.edu>. Thanks\n");
            e.printStackTrace();
            System.exit(1);
        }
    }

    static class Responder extends Thread {

        Socket s;
        BufferedReader in;
        PrintStream out;

        void close() {
            try {
                if (out != null) out.close();
                if (in != null) in.close();
                if (s != null) s.close();
            } catch (Exception e) {
            }
        }

        Responder(Socket s, URI u) {
            try {
                in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                out = new PrintStream(s.getOutputStream());
                out.println("Greetings " + u + ", this is the EchoServer at " + i + ". Now you say something.");
                System.out.println("Accepted " + u);
            } catch (Exception e) {
                close();
            }
        }

        public void run() {
            try {
                String s;
                while ((s = in.readLine()) != null) out.println(s);
            } catch (Exception e) {
            } finally {
                close();
            }
        }
    }

    public static void main(String[] arg) throws Exception {
        try {
            i = new URI(arg[0].equals("-d") ? arg[1] : arg[0]);
        } catch (Exception e) {
            System.out.printf("Usage:\n\tjava EchoServer [-d] you@your.domain.com\n");
            System.exit(1);
        }
        try {
            if (arg[0].equals("-d")) Control.logToFile("EchoServerLog.txt");
            STUNTEndpoint sock = new STUNTEndpoint(i);
            sock.listen(new Handler());
            while (true) {
                semaphore.acquire();
                synchronized (semaphore) {
                    new Responder(sockets.removeFirst(), uris.removeFirst()).start();
                }
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

