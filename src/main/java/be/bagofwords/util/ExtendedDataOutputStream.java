package be.bagofwords.util;

import java.io.*;
import java.nio.charset.Charset;

public class ExtendedDataOutputStream implements Closeable, Flushable {

    private static Charset standardCharset;

    static {

        standardCharset = Charset.forName("UTF-8");
    }

    private DataOutputStream dos;

    public ExtendedDataOutputStream(OutputStream os) {
        dos = new DataOutputStream(os);
    }

    public void write(boolean value) throws IOException {
        dos.writeBoolean(value);
    }

    public void write(byte value) throws IOException {
        dos.writeByte(value);
    }

    public void write(short value) throws IOException {
        dos.writeShort(value);
    }

    public void write(int value) throws IOException {
        dos.writeInt(value);
    }

    public void write(long value) throws IOException {
        dos.writeLong(value);
    }

    public void write(float value) throws IOException {
        dos.writeFloat(value);
    }

    public void write(double value) throws IOException {
        dos.writeDouble(value);
    }

    public void write(boolean[] values) throws IOException {
        dos.writeInt(values.length);
        for (boolean value : values) {
            write(value);
        }
    }

    public void write(byte[] values) throws IOException {
        dos.writeInt(values.length);
        dos.write(values);
    }

    public void write(short[] values) throws IOException {
        dos.writeInt(values.length);
        for (short value : values) {
            write(value);
        }
    }

    public void write(int[] values) throws IOException {
        dos.writeInt(values.length);
        for (int value : values) {
            write(value);
        }
    }

    public void write(long[] values) throws IOException {
        dos.writeInt(values.length);
        for (long value : values) {
            write(value);
        }
    }

    public void write(float[] values) throws IOException {
        dos.writeInt(values.length);
        for (float value : values) {
            write(value);
        }
    }

    public void write(double[] values) throws IOException {
        dos.writeInt(values.length);
        for (double value : values) {
            write(value);
        }
    }

    public void write(String value) throws IOException {
        byte[] bytes = value.getBytes(standardCharset);
        write(bytes);
    }


    @Override
    public void close() throws IOException {
        dos.close();
    }

    @Override
    public void flush() throws IOException {
        dos.flush();
    }
}
