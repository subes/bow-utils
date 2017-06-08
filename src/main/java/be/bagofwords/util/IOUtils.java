package be.bagofwords.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

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
}
