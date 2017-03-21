package be.bagofwords.util;

import be.bagofwords.ui.UI;
import org.xerial.snappy.Snappy;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class SocketConnection implements Closeable {

    private Socket socket;
    private DataInputStream is;
    private DataOutputStream os;
    private boolean debug;
    private final boolean autoFlush;
    private final boolean autoErrorDetection;
    private boolean sendDataSinceLastFlush = false;
    private boolean outputStreamBuffered = false;
    private boolean inputStreamBuffered = false;

    /**
     * Client connections
     */

    public SocketConnection(String host, int port) throws IOException {
        this(host, port, true, true);
    }

    public SocketConnection(String host, int port, String handler) throws IOException {
        this(host, port, true, true);
        writeString(handler);
        flush();
    }

    public SocketConnection(String host, int port, boolean autoFlush, boolean autoErrorDetection) throws IOException {
        this.socket = new Socket(host, port);
        this.is = new DataInputStream(socket.getInputStream());
        this.os = new DataOutputStream(socket.getOutputStream());
        this.autoFlush = autoFlush;
        this.autoErrorDetection = autoErrorDetection;
        this.os.writeBoolean(autoFlush);
        this.os.writeBoolean(autoErrorDetection);
    }

    /**
     * Server connections
     */

    public SocketConnection(Socket socket) throws IOException {
        this.socket = socket;
        this.is = new DataInputStream(socket.getInputStream());
        this.os = new DataOutputStream(socket.getOutputStream());
        this.autoFlush = is.readBoolean();
        this.autoErrorDetection = is.readBoolean();
    }

    public void useLargeOutputBuffer() throws IOException {
        bufferOutputStream(1024 * 1024);
    }

    private void bufferOutputStream(int size) throws IOException {
        if (outputStreamBuffered) {
            throw new RuntimeException("The output stream has already been buffered. Can not buffer again because we might lose bytes");
        }
        this.os = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream(), size));
        outputStreamBuffered = true;
    }

    public void useLargeInputBuffer() throws IOException {
        bufferInputStream(1024 * 1024);
    }

    private void bufferInputStream(int size) throws IOException {
        if (inputStreamBuffered) {
            throw new RuntimeException("The input stream has already been buffered. Can not buffer again because we might lose bytes");
        }
        this.is = new DataInputStream(new BufferedInputStream(socket.getInputStream(), size));
        inputStreamBuffered = true;
    }

    public void ensureBuffered() throws IOException {
        if (!inputStreamBuffered) {
            bufferInputStream(1024 * 32);
        }
        if (!outputStreamBuffered) {
            bufferOutputStream(1024 * 32);
        }
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
        checkFlush();
        socket.close();
    }

    public void writeLong(long value) throws IOException {
        actionsBeforeWrite();
        if (debug) {
            UI.write("RI --> " + value);
        }
        os.writeLong(value);
        actionsAfterWrite();
    }

    private void actionsBeforeWrite() throws IOException {
        if (autoErrorDetection) {
            os.writeBoolean(false);
        }
    }

    private void actionsAfterWrite() {
        sendDataSinceLastFlush = true;
    }

    public void writeInt(int value) throws IOException {
        actionsBeforeWrite();
        if (debug) {
            UI.write("RI --> " + value);
        }
        os.writeInt(value);
        actionsAfterWrite();
    }

    public long readLong() throws IOException {
        checksBeforeRead();
        long value = is.readLong();
        if (debug) {
            UI.write("RI <-- " + value);
        }
        return value;
    }

    private void checksBeforeRead() throws IOException {
        checkFlush();
        if (autoErrorDetection) {
            checkForError();
        }
    }

    private void checkFlush() throws IOException {
        if (sendDataSinceLastFlush && autoFlush) {
            flush();
        }
    }

    public int readInt() throws IOException {
        checksBeforeRead();
        int value = is.readInt();
        if (debug) {
            UI.write("RI <-- " + value);
        }
        return value;
    }

    public double readDouble() throws IOException {
        checksBeforeRead();
        double value = is.readDouble();
        if (debug) {
            UI.write("RI <-- " + value);
        }
        return value;
    }

    public void writeDouble(double value) throws IOException {
        actionsBeforeWrite();
        if (debug) {
            UI.write("RI --> " + value);
        }
        os.writeDouble(value);
        actionsAfterWrite();
    }

    public void writeByte(byte value) throws IOException {
        actionsBeforeWrite();
        if (debug) {
            UI.write("RI --> " + value);
        }
        os.writeByte(value);
        actionsAfterWrite();
    }

    public byte readByte() throws IOException {
        checksBeforeRead();
        byte value = is.readByte();
        if (debug) {
            UI.write("RI <-- " + value);
        }
        return value;
    }

    public void writeFloat(float value) throws IOException {
        actionsBeforeWrite();
        if (debug) {
            UI.write("RI --> " + value);
        }
        os.writeFloat(value);
        actionsAfterWrite();
    }

    public float readFloat() throws IOException {
        checksBeforeRead();
        float value = is.readFloat();
        if (debug) {
            UI.write("RI <-- " + value);
        }
        return value;
    }

    public <T> T readValue(Class<T> objectClass) throws IOException {
        checksBeforeRead();
        int length = SerializationUtils.getWidth(objectClass);
        boolean isCompressed = false;
        if (length < 0) {
            length = readInt();
            if (length < 0) {
                //large objects are automatically compressed
                isCompressed = true;
                length = -length;
            }
        }
        byte[] objectAsBytes = new byte[length];
        int numOfBytesRead = is.read(objectAsBytes);
        while (numOfBytesRead < length) {
            int extraBytesRead = is.read(objectAsBytes, numOfBytesRead, objectAsBytes.length - numOfBytesRead);
            if (extraBytesRead == -1) {
                throw new RuntimeException("Expected to read " + length + " bytes and received " + numOfBytesRead + " bytes");
            }
            numOfBytesRead += extraBytesRead;
        }
        if (debug) {
            UI.write("RI <-- " + objectAsBytes.length + " bytes");
        }
        if (isCompressed) {
            objectAsBytes = Snappy.uncompress(objectAsBytes);
        }
        T result = SerializationUtils.bytesToObjectCheckForNull(objectAsBytes, objectClass);
        return result;
    }

    public void flush() throws IOException {
        os.flush();
        sendDataSinceLastFlush = false;
    }

    public <T> void writeValue(T value, Class<T> objectClass) throws IOException {
        actionsBeforeWrite();
        byte[] objectAsBytes = SerializationUtils.objectToBytesCheckForNull(value, objectClass);
        int width = SerializationUtils.getWidth(objectClass);
        if (width == -1) {
            //not a fixed length object
            if (objectAsBytes.length > 1024 * 1024) {
                //compress large object
                objectAsBytes = Snappy.compress(objectAsBytes);
                writeInt(-objectAsBytes.length);
            } else {
                writeInt(objectAsBytes.length);
            }
        }
        if (debug) {
            UI.write("RI --> " + objectAsBytes.length + " bytes");
        }
        os.write(objectAsBytes);
        actionsAfterWrite();
    }

    public byte[] readByteArray() throws IOException {
        return readByteArrayImpl(true);
    }

    private byte[] readByteArrayImpl(boolean doChecksBeforeRead) throws IOException {
        if (doChecksBeforeRead) {
            checksBeforeRead();
        }
        int length = is.readInt();
        if (length > 50000000) {
            UI.write("About to read a byte array of length " + length);
        }
        byte[] bytes = new byte[length];
        is.readFully(bytes);
        if (debug) {
            String message = new String(bytes, "UTF-8");
            UI.write("RI <-- " + message.substring(0, Math.min(message.length(), 200)).replaceAll("\\W", "."));
        }
        return bytes;
    }

    public String readString() throws IOException {
        return readStringImpl(true);
    }

    private String readStringImpl(boolean doChecksBeforeRead) throws IOException {
        return new String(readByteArrayImpl(doChecksBeforeRead), "UTF-8");
    }

    public void writeByteArray(byte[] bytes) throws IOException {
        writeByteArrayImpl(bytes, true);
    }

    public void writeByteArrayImpl(byte[] bytes, boolean doActionsBeforeWrite) throws IOException {
        if (doActionsBeforeWrite) {
            actionsBeforeWrite();
        }
        if (debug) {
            String message = new String(bytes, "UTF-8");
            UI.write("RI --> " + message.substring(0, Math.min(message.length(), 200)).replaceAll("\\W", "."));
        }
        if (bytes.length > 1e9) {
            throw new RuntimeException("Currently objects larger then 1 GB are not supported...");
        }
        os.writeInt(bytes.length);
        os.write(bytes);
        actionsAfterWrite();
    }

    public void writeString(String message) throws IOException {
        writeStringImpl(message, true);
    }

    private void writeStringImpl(String message, boolean doActionsBeforeWrite) throws IOException {
        byte[] bytes = message.getBytes("UTF-8");
        writeByteArrayImpl(bytes, doActionsBeforeWrite);
    }

    public boolean readBoolean() throws IOException {
        checksBeforeRead();
        boolean result = is.readBoolean();
        if (debug) {
            UI.write("RI <-- " + result);
        }
        return result;
    }

    public void writeBoolean(boolean value) throws IOException {
        actionsBeforeWrite();
        if (debug) {
            UI.write("RI --> " + value);
        }
        os.writeBoolean(value);
        actionsAfterWrite();
    }

    public void writeError(String error) throws IOException {
        writeError(error, null);
    }

    public void writeError(String errorMessage, Exception exp) throws IOException {
        if (!autoErrorDetection) {
            throw new IOException("Sorry this function is only available if autoErrorDetection is set to true");
        }
        if (exp != null) {
            String[] lines = Utils.getStackTrace(exp).split("\n");
            for (String line : lines) {
                errorMessage += "\n\t" + line;
            }
        }
        os.writeBoolean(true);
        writeStringImpl(errorMessage, false);
        flush();
    }

    public void checkForError() throws IOException {
        boolean hasError = is.readBoolean();
        if (hasError) {
            String errorMessage = readStringImpl(false);
            throw new ReceivedException(errorMessage);
        }
    }

    public InetAddress getInetAddress() {
        return socket.getInetAddress();
    }

    public int getRemotePort() {
        return socket.getPort();
    }

    public static class ReceivedException extends IOException {
        public ReceivedException(String message) {
            super(message);
        }
    }
}
