package be.bagofwords.util;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class ExtendedDataInputStream implements Closeable {

    private static Charset standardCharset;

    static {
        standardCharset = Charset.forName("UTF-8");
    }

    private DataInputStream dis;

    public ExtendedDataInputStream(InputStream is) {
        dis = new DataInputStream(is);
    }

    public boolean readBoolean() throws IOException {
        return dis.readBoolean();
    }

    public byte readByte() throws IOException {
        return dis.readByte();
    }

    public short readShort() throws IOException {
        return dis.readShort();
    }

    public int readInt() throws IOException {
        return dis.readInt();
    }

    public long readLong() throws IOException {
        return dis.readLong();
    }

    public float readFloat() throws IOException {
        return dis.readFloat();
    }

    public double readDouble() throws IOException {
        return dis.readDouble();
    }

    public boolean[] readBooleanArray() throws IOException {
        boolean[] result = new boolean[dis.readInt()];
        for (int i = 0; i < result.length; i++) {
            result[i] = dis.readBoolean();
        }
        return result;
    }

    public byte[] readByteArray() throws IOException {
        byte[] result = new byte[dis.readInt()];
        dis.readFully(result);
        return result;
    }

    public short[] readShortArray() throws IOException {
        short[] result = new short[dis.readInt()];
        for (int i = 0; i < result.length; i++) {
            result[i] = dis.readShort();
        }
        return result;
    }

    public int[] readIntArray() throws IOException {
        int[] result = new int[dis.readInt()];
        for (int i = 0; i < result.length; i++) {
            result[i] = dis.readInt();
        }
        return result;
    }

    public long[] readLongArray() throws IOException {
        long[] result = new long[dis.readInt()];
        for (int i = 0; i < result.length; i++) {
            result[i] = dis.readLong();
        }
        return result;
    }

    public float[] readFloatArray() throws IOException {
        float[] result = new float[dis.readInt()];
        for (int i = 0; i < result.length; i++) {
            result[i] = dis.readFloat();
        }
        return result;
    }

    public double[] readDoubleArray() throws IOException {
        double[] result = new double[dis.readInt()];
        for (int i = 0; i < result.length; i++) {
            result[i] = dis.readDouble();
        }
        return result;
    }

    public String readString() throws IOException {
        byte[] bytes = readByteArray();
        return new String(bytes, standardCharset);
    }

    @Override
    public void close() throws IOException {
        dis.close();
    }

}
