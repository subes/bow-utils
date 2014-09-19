package be.bow.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Koen Deschacht (koendeschacht@gmail.com) on 9/19/14.
 */
public class SerializationUtilsTest {


    @Test
    public void testToFromBytesPrimitiveValues() {
        checkConversion(1);
        checkConversion(Integer.MIN_VALUE);
        checkConversion(Integer.MAX_VALUE);
        checkConversion(1l);
        checkConversion(Long.MAX_VALUE);
        checkConversion(Long.MIN_VALUE);
        checkConversion(1f);
        checkConversion(Float.MAX_VALUE);
        checkConversion(Float.MIN_VALUE);
        checkConversion(1d);
        checkConversion(Double.MAX_VALUE);
        checkConversion(Double.MIN_VALUE);
    }

    private void checkConversion(Object obj) {
        Assert.assertEquals(obj, SerializationUtils.bytesToObject(SerializationUtils.objectToBytes(obj), obj.getClass()));
    }
}
