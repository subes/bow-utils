package be.bagofwords.util;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;

import static org.junit.Assert.*;

public class ExtendedDataOutputInputStreamTest {

    @Test
    public void doTest() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ExtendedDataOutputStream os = new ExtendedDataOutputStream(bos);
        boolean booleanValue = true;
        byte byteValue = 1;
        short shortValue = 2;
        int intValue = 3;
        long longValue = 4;
        float floatValue = 5.0f;
        double doubleValue = 6.0;
        boolean[] booleanArray = {false, true, false};
        byte[] byteArray = {7, 8, 9};
        short[] shortArray = {10, 11, 12};
        int[] intArray = {13, 14, 15, 16};
        long[] longArray = {};
        float[] floatArray = {17, 18, 19};
        double[] doubleArray = {20, 21, 22};
        String string = "This is a test!";
        os.write(booleanValue);
        os.write(byteValue);
        os.write(shortValue);
        os.write(intValue);
        os.write(longValue);
        os.write(floatValue);
        os.write(doubleValue);
        os.write(booleanArray);
        os.write(byteArray);
        os.write(shortArray);
        os.write(intArray);
        os.write(longArray);
        os.write(floatArray);
        os.write(doubleArray);
        os.write(string);
        os.close();
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ExtendedDataInputStream is = new ExtendedDataInputStream(bis);
        assertEquals(booleanValue, is.readBoolean());
        assertEquals(byteValue, is.readByte());
        assertEquals(shortValue, is.readShort());
        assertEquals(intValue, is.readInt());
        assertEquals(longValue, is.readLong());
        assertEquals(floatValue, is.readFloat(), 0.00001f);
        assertEquals(doubleValue, is.readDouble(), 0.00001);
        assertArrayEquals(booleanArray, is.readBooleanArray());
        assertArrayEquals(byteArray, is.readByteArray());
        assertArrayEquals(shortArray, is.readShortArray());
        assertArrayEquals(intArray, is.readIntArray());
        assertArrayEquals(longArray, is.readLongArray());
        assertArrayEquals(floatArray, is.readFloatArray(), 0.0001f);
        assertArrayEquals(doubleArray, is.readDoubleArray(), 0.0001);
        assertEquals(string, is.readString());
        assertTrue(isEndOfStream(is));
    }

    private boolean isEndOfStream(ExtendedDataInputStream is) throws IOException {
        try {
            is.readByte();
            return false;
        } catch (EOFException exp) {
            return true;
        }
    }

}