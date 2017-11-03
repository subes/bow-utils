package be.bagofwords.util;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * Created by koen on 8/04/17.
 */
public class IOUtils {

    public static String readString(DataInputStream dis) throws IOException {
        if (dis.readBoolean()) {
            int length = dis.readInt();
            byte[] stringBytes = new byte[length];
            dis.readFully(stringBytes);
            return new String(stringBytes, StandardCharsets.UTF_8);
        } else {
            return null;
        }
    }

    public static void writeString(DataOutputStream dos, String string) throws IOException {
        if (string == null) {
            dos.writeBoolean(false);
        } else {
            dos.writeBoolean(true);
            byte[] stringBytes = string.getBytes(StandardCharsets.UTF_8);
            dos.writeInt(stringBytes.length);
            dos.write(stringBytes);
        }
    }

    public static byte[] compressBytes(byte[] input) {
        Deflater compressor = new Deflater();
        compressor.setLevel(Deflater.BEST_SPEED);

        compressor.setInput(input);
        compressor.finish();

        ByteArrayOutputStream bos = new ByteArrayOutputStream(input.length);

        byte[] buf = new byte[1024 * 1024];
        while (!compressor.finished()) {
            int count = compressor.deflate(buf);
            bos.write(buf, 0, count);
        }
        try {
            bos.close();
        } catch (IOException e) {
            throw new RuntimeException("Unexpected exception while closing compressed array stream", e);
        }

        return bos.toByteArray();
    }

    public static byte[] uncompressBytes(byte[] input) {
        Inflater compressor = new Inflater();
        compressor.setInput(input);
        ByteArrayOutputStream bos = new ByteArrayOutputStream(input.length);
        byte[] buf = new byte[1024 * 1024];
        try {
            while (!compressor.finished()) {
                int count = compressor.inflate(buf);
                bos.write(buf, 0, count);
            }
            bos.close();
        } catch (IOException | DataFormatException e) {
            throw new RuntimeException("Failed to decompress object", e);
        }
        return bos.toByteArray();
    }

}
