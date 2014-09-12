package be.bow.util;

import be.bow.application.BaseServer;
import be.bow.ui.UI;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class WrappedSocketConnection implements Closeable {

    private Socket socket;
    private DataInputStream is;
    private DataOutputStream os;
    private boolean debug;

    public WrappedSocketConnection(String host, int port) throws IOException {
        this(host, port, false);
    }

    public WrappedSocketConnection(String host, int port, boolean useLargeOutputBuffer) throws IOException {
        this(new Socket(host, port), useLargeOutputBuffer);
    }

    public WrappedSocketConnection(Socket socket) throws IOException {
        this(socket, false);
    }

    public WrappedSocketConnection(Socket socket, boolean useLargeOutputBuffer) throws IOException {
        this.socket = socket;
        this.is = new DataInputStream(new BufferedInputStream(socket.getInputStream(), 32 * 1024));
        this.os = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream(), useLargeOutputBuffer ? 1024 * 1024 : 32 * 1024));
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public DataInputStream getIs() {
        return is;
    }

    public DataOutputStream getOs() {
        return os;
    }

    public Socket getSocket() {
        return socket;
    }

    public boolean isOpen() {
        return !socket.isClosed();
    }

    public void close() throws IOException {
        os.flush();
        socket.close();
    }

    public void writeLong(long value) throws IOException {
        if (debug) {
            UI.write("RI --> " + value);
        }
        os.writeLong(value);
    }

    public void writeInt(int value) throws IOException {
        if (debug) {
            UI.write("RI --> " + value);
        }
        os.writeInt(value);
    }

    public long readLong() throws IOException {
        long value = is.readLong();
        if (debug) {
            UI.write("RI <-- " + value);
        }
        return value;
    }

    public int readInt() throws IOException {
        int value = is.readInt();
        if (debug) {
            UI.write("RI <-- " + value);
        }
        return value;
    }

    public double readDouble() throws IOException {
        double value = is.readDouble();
        if (debug) {
            UI.write("RI <-- " + value);
        }
        return value;
    }

    public void writeDouble(double value) throws IOException {
        if (debug) {
            UI.write("RI --> " + value);
        }
        os.writeDouble(value);
    }

    public void writeByte(byte value) throws IOException {
        if (debug) {
            UI.write("RI --> " + value);
        }
        os.writeByte(value);
    }

    public byte readByte() throws IOException {
        byte value = is.readByte();
        if (debug) {
            UI.write("RI <-- " + value);
        }
        return value;
    }

    public <T> T readValue(Class<T> objectClass) throws IOException {
        if (objectClass == Long.class) {
            long response = readLong();
            if (response != BaseServer.LONG_NULL) {
                return (T) new Long(response);
            } else {
                return null;
            }
        } else if (objectClass == Double.class) {
            double response = readDouble();
            if (response != BaseServer.DOUBLE_NULL) {
                return (T) new Double(response);
            } else {
                return null;
            }
        } else {
            byte[] objectAsBytes = readByteArray();
            return SerializationUtils.bytesToObject(objectAsBytes, objectClass);
        }
    }

    public void flush() throws IOException {
        os.flush();
    }

    public <T> void writeValue(T value, Class<T> objectClass) throws IOException {
        if (objectClass == Long.class) {
            if (value == null) {
                writeLong(BaseServer.LONG_NULL);
            } else {
                writeLong((Long) value);
            }
        } else if (objectClass == Double.class) {
            if (value == null) {
                writeDouble(BaseServer.DOUBLE_NULL);
            } else {
                writeDouble((Double) value);
            }
        } else {
            byte[] objectAsBytes = SerializationUtils.objectToBytes(value);
            writeByteArray(objectAsBytes);
        }
    }

    public byte[] readByteArray() throws IOException {
        int length = is.readInt();
        if (length > 50000000) {
            UI.write("About to read a byte array of length " + length);
        }
        byte[] bytes = new byte[length];
        is.readFully(bytes);
        if (debug) {
            String message = new String(bytes, BaseServer.ENCODING);
            UI.write("RI <-- " + message.substring(0, Math.min(message.length(), 200)).replaceAll("\\W", "."));
        }
        return bytes;
    }

    public String readString() throws IOException {
        return new String(readByteArray(), BaseServer.ENCODING);
    }

    public void writeByteArray(byte[] bytes) throws IOException {
        if (debug) {
            String message = new String(bytes, BaseServer.ENCODING);
            UI.write("RI --> " + message.substring(0, Math.min(message.length(), 200)).replaceAll("\\W", "."));
        }
        if (bytes.length > 1e9) {
            throw new RuntimeException("Currently objects larger then 1 GB are not supported...");
        }
        os.writeInt(bytes.length);
        os.write(bytes);
    }

    public void writeString(String message) throws IOException {
        byte[] bytes = message.getBytes(BaseServer.ENCODING);
        writeByteArray(bytes);
    }

    public boolean readBoolean() throws IOException {
        boolean result = is.readBoolean();
        if (debug) {
            UI.write("RI <-- " + result);
        }
        return result;
    }

    public void writeBoolean(boolean value) throws IOException {
        if (debug) {
            UI.write("RI --> " + value);
        }
        os.writeBoolean(value);
    }

    public InetAddress getInetAddress() {
        return socket.getInetAddress();
    }
}
