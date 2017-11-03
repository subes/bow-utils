package be.bagofwords.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

/**
 * Created by Koen Deschacht (koendeschacht@gmail.com) on 9/19/14.
 */
public class SerializationUtilsTest {

    @Test
    public void testSimplePrimitiveValues() {
        checkConversion(1, Integer.class);
        checkConversion(-1, Integer.class);
        checkConversion(1l, Long.class);
        checkConversion(-1l, Long.class);
        checkConversion(1f, Float.class);
        checkConversion(-1f, Float.class);
        checkConversion(1d, Double.class);
        checkConversion(-1d, Double.class);
    }

    @Test
    public void testNullValues() {
        checkConversion(null, Long.class);
        checkConversion(null, Double.class);
        checkConversion(null, Float.class);
        checkConversion(null, Integer.class);
        checkConversion(null, String.class);
        checkConversion("null", String.class);
        checkConversion(null, Date.class);
    }

    @Test
    public void testEnums() {
        checkConversion(TestEnum.FIRST_VALUE, TestEnum.class);
        TestEnum converted = SerializationUtils.bytesToObject(SerializationUtils.objectToBytes(TestEnum.SECOND_VALUE, TestEnum.class), TestEnum.class);
        Assert.assertEquals(TestEnum.SECOND_VALUE, converted);
        Assert.assertEquals(4, converted.someMethod());
        checkConversion(null, TestEnum.class);
    }

    @Test
    public void testCompression() {
        String objectToCompress = "0";
        for (int i = 0; i < 10; i++) {
            objectToCompress = objectToCompress + objectToCompress;
        }
        byte[] bytes = SerializationUtils.objectToCompressedBytes(objectToCompress, String.class);
        Assert.assertTrue(bytes.length < SerializationUtils.objectToBytes(objectToCompress, String.class).length / 5);
        String decompressedString = SerializationUtils.compressedBytesToObject(bytes, String.class);
        Assert.assertEquals(objectToCompress, decompressedString);
    }

    private void checkConversion(Object obj, Class objectClass) {
        Assert.assertEquals(obj, SerializationUtils.bytesToObjectCheckForNull(SerializationUtils.objectToBytesCheckForNull(obj, objectClass), objectClass));
    }

    private enum TestEnum {
        FIRST_VALUE(1) {
            @Override
            public int someMethod() {
                return 3;
            }
        }, SECOND_VALUE(2) {
            @Override
            public int someMethod() {
                return 4;
            }
        };

        private final int value;

        TestEnum(int value) {
            this.value = value;
        }

        public abstract int someMethod();
    }
}
